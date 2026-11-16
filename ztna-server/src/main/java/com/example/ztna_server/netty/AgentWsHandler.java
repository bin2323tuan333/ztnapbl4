package com.example.ztna_server.netty;

import com.example.ztna_server.core.*;
import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.Decision;
import com.example.ztna_server.protocol.FrameType;
import com.example.ztna_server.protocol.frames.*;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.runtime.connection.agent.AgentConnection;
import com.example.ztna_server.runtime.connection.agent.AgentConnectionRegistry;
import com.example.ztna_server.runtime.connection.agent.PendingConnect;
import com.example.ztna_server.runtime.connection.agent.PendingConnectRegistry;
import com.example.ztna_server.runtime.connection.connector.ConnectorConnection;
import com.example.ztna_server.runtime.connection.connector.ConnectorConnectionRegistry;
import com.example.ztna_server.runtime.relay.RelayEngine;
import com.example.ztna_server.runtime.relay.RelayResult;
import com.example.ztna_server.runtime.routing.RouteTarget;
import com.example.ztna_server.runtime.routing.RoutingRegistry;
import com.example.ztna_server.service.AccessLogService;
import com.example.ztna_server.service.JwtService;
import com.example.ztna_server.service.RuleService;
import com.example.ztna_server.service.SessionLifecycleService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class AgentWsHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final JwtService jwtService;
    private final UserManager userManager;
    private final SessionLifecycleService sessionLifecycleService;
    private final AccessLogService accessLogService;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ConnectorRepository connectorRepository;
    private final RoutingRegistry routingRegistry;
    private final ConnectorConnectionRegistry connectorConnectionRegistry;
    private final AgentConnectionRegistry agentConnectionRegistry;
    private final PendingConnectRegistry pendingConnectRegistry;
    private final RuleService ruleService;
    private final RelayEngine relayEngine;
    private volatile String userId;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof PingWebSocketFrame ping) {
            ctx.writeAndFlush(new PongWebSocketFrame(ping.content().retain()));
            return;
        }
        if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
            return;
        }
        if (!(frame instanceof BinaryWebSocketFrame bin)) return;

        ByteBuf buf = bin.content();
        if (buf.readableBytes() < 1) return;

        byte type = buf.getByte(0);

        try {
            switch (type) {
                case FrameType.HTTP_ACCESS -> handleHttpAccess(ctx, buf);
                case FrameType.RESOURCE_ACCESS -> handleResourceAccess(ctx, buf);
                case FrameType.DATA -> handleData(buf);
                case FrameType.CLOSE -> handleClose(buf);
                default -> log.warn("[User] Unknown frame: 0x{}", String.format("%02X", type));
            }
        } catch (Exception e) {
            log.error("[User] Lỗi xử lý frame 0x{}", String.format("%02X", type), e);
        }
    }

    private void handleHttpAccess(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        HttpAccessFrame req = new HttpAccessFrame();
        req.decode(data);

        String reqId = req.getRequestId();
        String resourceId = req.getAppId();
        String jwt = req.getJwt();
        String ip = clientIp(ctx);
        if (reqId == null || reqId.isBlank()
                || resourceId == null || resourceId.isBlank()
                || jwt == null || jwt.isBlank()) {
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_AUTH_FAILED,
                    "Invalid request"
            );
            return;
        }

        // Ung dung dang off hoac khong ton tai
        RouteTarget target = routingRegistry.getByResourceId(resourceId);
        if (target == null) {
            log.warn("[Lifecycle] Resource not found: reqId={}, resourceId={}, ip={}", reqId, resourceId, ip);
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_APP_NOT_FOUND,
                    "Resource not found"
            );
            return;
        }


        Resource resource = resourceRepository.findById(UUID.fromString(target.resourceId()))
                .orElse(null);
        Connector connector = connectorRepository.findById(UUID.fromString(target.connectorId()))
                .orElse(null);

        if (resource == null || connector == null) {
            log.error("[Lifecycle] Routing references missing DB entity: resource={}, connector={}",
                    resource.getId(), connector.getId());

            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_INTERNAL_ERROR,
                    "Resource or Connector not found"
            );
            return;
        }

        if (!connectorConnectionRegistry.isOnline(target.connectorId())) {
            log.warn("[Lifecycle] Connector offline: reqId={}, connector={}, resource={}",
                    reqId, target.connectorId(), resourceId);

            accessLogService.log(
                    null,
                    ip,
                    "Unknown",
                    resource.getId(),
                    resource.getName(),
                    resource.getHost(),
                    connector.getId(),
                    connector.getName(),
                    Decision.DENY,
                    "Connector offline"
            );

            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_CONNECTOR_OFFLINE,
                    "Connector offline"
            );
            return;
        }

        String uid;
        try {
            if (jwtService.isTokenExpired(jwt)) {
                UUID Uid = jwtService.extractUserId(jwt);
                User user = userRepository.findById(Uid).orElse(null);
                log.warn("[Lifecycle] HTTP_ACCESS bị từ chối: JWT hết hạn, reqId={}, uid={}, appId={}, ip={}",
                        reqId, Uid, resourceId, ip);
                accessLogService.log(
                        Uid,
                        ip,
                        user == null ? "Unknown" : user.getFullName(),
                        resource.getId(),
                        resource.getName(),
                        resource.getHost(),
                        connector.getId(),
                        connector.getName(),
                        Decision.DENY,
                        "JWT hết hạn"
                );
                sendHttpFail(
                        ctx,
                        reqId,
                        HttpResFrame.STATUS_AUTH_FAILED,
                        "JWT hết hạn"
                );
                return;
            }
            uid = jwtService.extractUserId(jwt).toString();
        } catch (Exception e) {
            log.warn("[Lifecycle] HTTP_ACCESS bị từ chối: JWT không hợp lệ, reqId={}, appId={}, ip={}, err={}",
                    reqId, resourceId, ip, e.getMessage());
            accessLogService.log(
                    null,
                    ip,
                    "Unknown",
                    resource.getId(),
                    resource.getName(),
                    resource.getHost(),
                    connector.getId(),
                    connector.getName(),
                    Decision.DENY,
                    "JWT không hợp lệ"
            );
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_AUTH_FAILED,
                    "JWT không hợp lệ"
            );
            return;
        }

        UUID userId = UUID.fromString(uid);
        User authed = userRepository.findById(userId)
                .orElse(null);

        if (authed == null || !authed.getEnabled()) {
            log.warn("[Lifecycle] User not found or disabled: uid={}, reqId={}",
                    uid, reqId);
            accessLogService.log(
                    userId,
                    ip,
                    authed == null ? "Unknown" : authed.getFullName(),
                    resource.getId(),
                    resource.getName(),
                    resource.getHost(),
                    connector.getId(),
                    connector.getName(),
                    Decision.DENY,
                    "User không tồn tại hoặc đã bị khóa"
            );
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_AUTH_FAILED,
                    "User not found or disabled"
            );

            return;
        }


        if (this.userId == null) {
            AgentConnection current = agentConnectionRegistry.getByUserId(uid);
            if (current != null && current.getChannel() != ctx.channel()) {
                log.warn("[Lifecycle] User already connected: uid={}, reqId={}",
                        uid, reqId);

                accessLogService.log(
                        userId,
                        ip,
                        authed.getFullName(),
                        resource.getId(),
                        resource.getName(),
                        resource.getHost(),
                        connector.getId(),
                        connector.getName(),
                        Decision.DENY,
                        "User already connected"
                );

                sendHttpFail(
                        ctx,
                        reqId,
                        HttpResFrame.STATUS_AUTH_FAILED,
                        "User already connected"
                );
                return;
            }
            if (current == null) {
                AgentConnection registered = agentConnectionRegistry.register(uid, ctx.channel());
                log.info("[Lifecycle] Agent registered: user={}, connection={}",
                        uid, registered.getConnectionId());
            }
            this.userId = uid;
        } else if (!this.userId.equals(uid)) {
            log.warn("[Lifecycle] User mismatch: expected={}, received={}, reqId={}",
                    this.userId, uid, reqId);

            accessLogService.log(
                    userId,
                    ip,
                    authed.getFullName(),
                    resource.getId(),
                    resource.getName(),
                    resource.getHost(),
                    connector.getId(),
                    connector.getName(),
                    Decision.DENY,
                    "User mismatch"
            );

            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_AUTH_FAILED,
                    "User mismatch"
            );
            return;
        }

        log.info(
                "[Lifecycle] HTTP_ACCESS OK: reqId={}, user={}, resource={}, connector={}, ip={}",
                reqId,
                uid,
                resource.getName(),
                connector.getName(),
                ip
        );

        accessLogService.log(
                userId,
                ip,
                authed.getFullName(),
                resource.getId(),
                resource.getName(),
                resource.getHost(),
                connector.getId(),
                connector.getName(),
                Decision.ALLOW,
                "Xác thực thành công"
        );

        HttpResFrame response = HttpResFrame.ok(reqId, "");

        ctx.writeAndFlush(
                new BinaryWebSocketFrame(
                        Unpooled.wrappedBuffer(response.encode())
                )
        );
    }

    private void handleResourceAccess(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        ResourceAccessFrame req = new ResourceAccessFrame();
        req.decode(data);

        String reqId = req.getRequestId();
        String resourceId = req.getAppId();
        String ip = clientIp(ctx);

        if (reqId == null || reqId.isBlank()
                || resourceId == null || resourceId.isBlank()) {

            sendResourceFail(ctx, reqId, "Invalid request");
            return;
        }

        String uid = this.userId;
        AgentConnection agent = agentConnectionRegistry.getByUserId(uid);
        if (agent == null || agent.getChannel() != ctx.channel() || !agent.isActive()) {
            sendResourceFail(ctx, reqId, "Agent connection không hợp lệ hoặc đã hết hạn");
            return;
        }

        RouteTarget target = routingRegistry.getByResourceId(resourceId);
        if (target == null) {
            log.warn("[Gateway] Resource not found: reqId={}, resourceId={}, ip={}", reqId, resourceId, ip);
            sendResourceFail(ctx, reqId, "Resource không tồn tại");
            return;
        }

        Resource resource = resourceRepository.findById(UUID.fromString(target.resourceId())).orElse(null);
        Connector connector = connectorRepository.findById(UUID.fromString(target.connectorId())).orElse(null);
        if (resource == null || connector == null) {
            log.error("[Gateway] Resource or Connector missing in database: resourceId={}", resourceId);
            sendResourceFail(ctx, reqId, "Resource hoặc Connector không tồn tại");
            return;
        }

        UUID userId = UUID.fromString(uid);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            sendResourceFail(ctx, reqId, "User không tồn tại hoặc đã bị khóa");
            return;
        }

        boolean hasPermission = ruleService.hasAccess(userId, resource.getId());
        if (!hasPermission) {
            log.warn("[Gateway] Access denied: user={}, appId={}, ip={}", userId, resourceId, ip);
            accessLogService.log(
                    userId,
                    ip,
                    user.getFullName(),
                    resource.getId(),
                    resource.getName(),
                    resource.getHost(),
                    connector.getId(),
                    connector.getName(),
                    Decision.DENY,
                    "Không có quyền truy cập Resource"
            );
            sendResourceFail(ctx, reqId, "Bạn không có quyền truy cập ứng dụng này");
            return;
        }

        ConnectorConnection connectorConnection = connectorConnectionRegistry.getByConnectorId(target.connectorId());
        if (connectorConnection == null || !connectorConnection.isActive()) {
            log.warn("[Gateway] Connector offline: connector={}, appId={}", target.connectorId(), resourceId);
            accessLogService.log(
                    userId,
                    ip,
                    user.getFullName(),
                    resource.getId(),
                    resource.getName(),
                    resource.getHost(),
                    connector.getId(),
                    connector.getName(),
                    Decision.DENY,
                    "Connector offline"
            );
            sendResourceFail(ctx, reqId, "Connector offline");
            return;
        }

        String streamId = UUID.randomUUID().toString();
        PendingConnect pending = PendingConnect.builder()
                .userId(uid)
                .resourceId(resourceId)
                .requestId(reqId)
                .streamId(streamId)
                .connectorId(connectorConnection.getConnectorId())
                .build();
        pendingConnectRegistry.add(pending);
        ConnectFrame connectFrame = new ConnectFrame(streamId, resourceId);
        connectorConnection.getChannel().writeAndFlush(new BinaryWebSocketFrame(
                        Unpooled.wrappedBuffer(connectFrame.encode()))
        ).addListener(future -> {
            if (!future.isSuccess()) {
                pendingConnectRegistry.remove(streamId);
                log.error("[Gateway] Cannot send CONNECT: streamId={}, error={}", streamId, future.cause().getMessage());
                sendResourceFail(ctx, reqId, "Không thể gửi yêu cầu đến Connector");
            }
        });

        log.info("[Gateway] RESOURCE_ACCESS accepted: user={}, appId={}, reqId={}, streamId={}, connector={}, ip={}",
                userId, resourceId, reqId, streamId, target.connectorId(), ip);
    }

    private void sendHttpFail(ChannelHandlerContext ctx, String reqId, byte status, String reason) {
        HttpResFrame resp = HttpResFrame.failed(reqId, status, reason);
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(resp.encode())));
    }

    private void sendResourceFail(ChannelHandlerContext ctx, String reqId, String reason) {
        ResourceResFrame resp = ResourceResFrame.failed(reqId,ResourceResFrame.STATUS_APP_NOT_FOUND ,reason);
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(resp.encode())));
    }

    private void handleData(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        DataFrame frame = new DataFrame();
        frame.decode(data);

        RelayResult result = relayEngine.forwardToConnector(frame);
    }

    public void handleClose(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        CloseFrame frame = new CloseFrame();
        frame.decode(data);
        String streamId = frame.getStreamId();
        log.info("[User] CLOSE stream={} reason={}", streamId, frame.getReasonText());
        sessionLifecycleService.closeSession(streamId, "CLIENT_CLOSED: " + frame.getReasonText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (userId == null) return;
        log.info("[User] {} disconnected", userId);
        userManager.removeByChannel(ctx.channel());
        sessionLifecycleService.onAgentDisconnect(userId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[User] Lỗi xử lý frame (bỏ qua): {}", cause.getMessage());
    }

    private static String clientIp(ChannelHandlerContext ctx) {
        SocketAddress addr = ctx.channel().remoteAddress();
        if (addr instanceof InetSocketAddress isa && isa.getAddress() != null) {
            return isa.getAddress().getHostAddress();
        }
        return "unknown";
    }

}
