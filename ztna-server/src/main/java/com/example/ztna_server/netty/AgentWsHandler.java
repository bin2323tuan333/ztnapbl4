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
import com.example.ztna_server.runtime.connection.agent.AccessTicketRegistry;
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
import io.jsonwebtoken.ExpiredJwtException;
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
    private final AccessTicketRegistry accessTicketRegistry;
    // Không lưu "userId của kết nối": một Agent có thể được nhiều người dùng chung.
    // Danh tính đi theo từng vé (AccessTicketRegistry) do HTTP_ACCESS cấp.


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
                case FrameType.DATA -> handleData(ctx, buf);
                case FrameType.CLOSE -> handleClose(ctx, buf);
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


        String uid;
        try {
            // Chỉ nhận access token (có claim typ=access). Refresh token hạn 7 ngày bị từ chối.
            uid = jwtService.parseAccessClaims(jwt).getSubject();
        } catch (ExpiredJwtException e) {
            UUID expiredUid = null;
            try { expiredUid = UUID.fromString(e.getClaims().getSubject()); } catch (Exception ignored) { }
            User user = expiredUid == null ? null : userRepository.findById(expiredUid).orElse(null);
            log.warn("[Lifecycle] HTTP_ACCESS bị từ chối: JWT hết hạn, reqId={}, uid={}, appId={}, ip={}", reqId, expiredUid, resourceId, ip);
            accessLogService.log(
                    expiredUid,
                    ip,
                    user == null ? "Unknown" : user.getFullName(),
                    null,
                    resourceId,
                    null,
                    null,
                    null,
                    Decision.DENY,
                    "JWT hết hạn"
            );
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_AUTH_FAILED,
                    "Phiên đăng nhập đã hết, hãy đăng nhập lại."
            );
            return;
        } catch (Exception e) {
            log.warn("[Lifecycle] HTTP_ACCESS bị từ chối: JWT không hợp lệ, reqId={}, appId={}, ip={}, err={}",
                    reqId, resourceId, ip, e.getMessage());
            accessLogService.log(
                    null,
                    ip,
                    "Unknown",
                    null,
                    resourceId,
                    null,
                    null,
                    null,
                    Decision.DENY,
                    "JWT không hợp lệ"
            );
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_AUTH_FAILED,
                    "Phiên đăng nhập đã hết, hãy đăng nhập lại."
            );
            return;
        }

        UUID userId = UUID.fromString(uid);
        User authed = userRepository.findById(userId).orElse(null);

        if (authed == null || !authed.getEnabled()) {
            log.warn("[Lifecycle] User not found or disabled: uid={}, reqId={}", uid, reqId);
            accessLogService.log(
                    userId,
                    ip,
                    authed == null ? "Unknown" : authed.getFullName(),
                    null,
                    resourceId,
                    null,
                    null,
                    null,
                    Decision.DENY,
                    "User không tồn tại hoặc đã bị khóa"
            );
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_AUTH_FAILED,
                    "User không tồn tại hoặc đã bị khóa"
            );

            return;
        }

        // Ung dung dang off hoac khong ton tai
        RouteTarget target = routingRegistry.getByResourceId(resourceId);
        if (target == null) {
            log.warn("[Lifecycle] Resource not found: reqId={}, resourceId={}, ip={}", reqId, resourceId, ip);
            accessLogService.log(
                    userId,
                    ip,
                    authed.getFullName(),
                    null,
                    resourceId,
                    null,
                    null,
                    null,
                    Decision.DENY,
                    "Resource không tồn tại hoặc không được phép truy cập"
            );
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_APP_NOT_FOUND,
                    "Ứng dụng đang tạm dừng hoạt động."
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
            accessLogService.log(
                    userId,
                    ip,
                    authed.getFullName(),
                    resource != null ? resource.getId() : null,
                    resource != null ? resource.getName() : resourceId,
                    resource != null ? resource.getHost() : null,
                    connector != null ? connector.getId() : null,
                    connector != null ? connector.getName() : "Unknown",
                    Decision.DENY,
                    resource == null
                            ? "Resource không tồn tại trong database"
                            : "Connector không tồn tại trong database"
            );
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_INTERNAL_ERROR,
                    "Ứng dụng hoặc vùng kết nối không khả dụng"
            );
            return;
        }

        // Kiểm rule ngay ở HTTP_ACCESS: không có quyền thì không cấp vé, Agent không mở cổng / trình duyệt,
        // và log ghi DENY (trước đây ghi ALLOW "Xác thực thành công" làm thống kê sai).
        // Kiểm trước trạng thái connector để người không có quyền không biết được connector đang online hay không.
        if (!ruleService.hasAccess(userId, resource.getId())) {
            log.warn("[Lifecycle] HTTP_ACCESS bị từ chối: không có quyền, reqId={}, user={}, resource={}, ip={}",
                    reqId, uid, resource.getName(), ip);
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
                    "Không có quyền truy cập Resource"
            );
            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_FORBIDDEN,
                    "Bạn không có quyền truy cập ứng dụng này"
            );
            return;
        }

        if (!connectorConnectionRegistry.isOnline(target.connectorId())) {
            log.warn("[Lifecycle] Connector offline: reqId={}, connector={}, resource={}",
                    reqId, target.connectorId(), resourceId);

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
                    "Connector đang không hoạt động."
            );

            sendHttpFail(
                    ctx,
                    reqId,
                    HttpResFrame.STATUS_CONNECTOR_OFFLINE,
                    "Connector không hoạt động."
            );
            return;
        }




        // Cấp vé gắn với đúng người dùng + ứng dụng + kênh Agent này.
        // Agent gửi lại vé trong mỗi RESOURCE_ACCESS để Server biết ai đang xin mở kết nối.
        AccessTicketRegistry.AccessTicket ticket = accessTicketRegistry.issue(uid, resource.getId().toString(), ctx.channel());

        log.info("[Lifecycle] HTTP_ACCESS OK: reqId={}, user={}, resource={}, connector={}, ip={}", reqId, uid, resource.getName(), connector.getName(), ip);

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

        // Trường streamId của HTTP_RESPONSE trước đây bỏ trống, nay dùng để gửi vé về Agent
        HttpResFrame response = HttpResFrame.ok(reqId, ticket.ticket());
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

        // Lấy người dùng từ vé, không dùng trạng thái chung của kết nối.
        // Vé phải do chính kênh này nhận và đúng ứng dụng đang xin truy cập.
        AccessTicketRegistry.AccessTicket ticket = accessTicketRegistry.get(req.getTicket());
        if (ticket == null || ticket.agentChannel() != ctx.channel() || !ticket.resourceId().equals(resourceId)) {
            log.warn("[Gateway] Vé truy cập không hợp lệ: reqId={}, appId={}, ip={}", reqId, resourceId, ip);
            sendResourceFail(ctx, reqId, "Vé truy cập không hợp lệ hoặc đã hết hạn, hãy mở lại ứng dụng từ Portal");
            return;
        }
        String uid = ticket.userId();

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
                .agentChannel(ctx.channel())
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

    private void handleData(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        DataFrame frame = new DataFrame();
        frame.decode(data);

        RelayResult result = relayEngine.forwardToConnector(frame, ctx.channel());
    }

    public void handleClose(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        CloseFrame frame = new CloseFrame();
        frame.decode(data);
        String streamId = frame.getStreamId();
        log.info("[User] CLOSE stream={} reason={}", streamId, frame.getReasonText());
        sessionLifecycleService.closeSessionFrom(ctx.channel(), streamId, "CLIENT_CLOSED: " + frame.getReasonText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("[User] Agent disconnected: {}", clientIp(ctx));
        userManager.removeByChannel(ctx.channel());
        accessTicketRegistry.removeByChannel(ctx.channel());
        sessionLifecycleService.onAgentChannelClosed(ctx.channel());
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
