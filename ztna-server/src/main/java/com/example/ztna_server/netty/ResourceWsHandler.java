package com.example.ztna_server.netty;

import com.example.ztna_server.core.*;
import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.Decision;
import com.example.ztna_server.protocol.FrameType;
import com.example.ztna_server.protocol.frames.CloseFrame;
import com.example.ztna_server.protocol.frames.HttpAccessFrame;
import com.example.ztna_server.protocol.frames.HttpResFrame;
import com.example.ztna_server.protocol.frames.ResourceAccessFrame;
import com.example.ztna_server.protocol.frames.ResourceResFrame;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.routes.App;
import com.example.ztna_server.routes.AppRoute;
import com.example.ztna_server.routes.RoutingTable;
import com.example.ztna_server.routes.Session;
import com.example.ztna_server.service.AccessLogService;
import com.example.ztna_server.service.JwtService;
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
public class ResourceWsHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final JwtService jwtService;
    private final UserManager userManager;
    private final SessionLifecycleService sessionLifecycleService;
    private final AccessLogService accessLogService;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ConnectorRepository connectorRepository;
    private final RoutingTable routingTable;
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
                case FrameType.DATA -> sessionLifecycleService.forwardToConnector(buf);
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

        AppRoute route = (resourceId != null) ? routingTable.getAppRoute(resourceId) : null;
        App app = route != null ? route.getApp() : null;

        UUID appUuid = null;
        String appName = null;
        String appHost = null;
        if (app != null) {
            try {
                appUuid = UUID.fromString(app.getId());
            } catch (Exception ignored) {

            }
            appName = app.getName();
            appHost = app.getHost();
        }

        Connector connector = null;
        if (route != null && route.getConnectorId() != null) {
            try {
                connector = connectorRepository.findById(UUID.fromString(route.getConnectorId())).orElse(null);
            } catch (Exception ignored) { }
        }
        UUID connUuid = connector != null ? connector.getId() : null;
        String connName = connector != null ? connector.getName() : null;


        String uid;
        try {
            if (jwtService.isTokenExpired(jwt)) {
                UUID expiredUid = jwtService.extractUserId(jwt);
                User user = userRepository.findById(expiredUid).orElse(null);
                log.warn("[Lifecycle] HTTP_ACCESS bị từ chối: JWT hết hạn, reqId={}, uid={}, appId={}, ip={}",
                        reqId, expiredUid, resourceId, ip);
                accessLogService.log(
                        expiredUid, ip,
                        user == null ? "Unknown" : user.getFullName(),
                        appUuid,
                        appName,
                        appHost,
                        connUuid,
                        connName,
                        Decision.DENY,
                        "JWT hết hạn"
                );
                sendHttpFail(ctx, reqId, HttpResFrame.STATUS_AUTH_FAILED, "JWT hết hạn");
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
                    appUuid,
                    appName,
                    appHost,
                    connUuid,
                    connName,
                    Decision.DENY,
                    "JWT không hợp lệ"
            );
            sendHttpFail(ctx, reqId, HttpResFrame.STATUS_AUTH_FAILED, "JWT không hợp lệ");
            return;
        }

        if (this.userId == null) {
            this.userId = uid;
            userManager.register(uid, ctx.channel());
        } else if (!this.userId.equals(uid)) {
            User incoming = userRepository.findById(UUID.fromString(uid)).orElse(null);
            log.warn("[Lifecycle] HTTP_ACCESS bị từ chối: User mismatch, reqId={}, expected={}, got={}, appId={}, ip={}",
                    reqId, this.userId, uid, resourceId, ip);
            accessLogService.log(
                    UUID.fromString(uid), ip,
                    incoming != null ? incoming.getFullName() : null,
                    appUuid,
                    appName,
                    appHost,
                    connUuid,
                    connName,
                    Decision.DENY,
                    "User mismatch"
            );
            sendHttpFail(ctx, reqId, HttpResFrame.STATUS_AUTH_FAILED, "User mismatch");
            return;
        }

        User authed = userRepository.findById(UUID.fromString(uid)).orElse(null);
        log.info("[Lifecycle] HTTP_ACCESS OK reqId={} user={} appId={} app={} connector={} ip={}",
                reqId, uid, resourceId, appName, connName, ip);
        accessLogService.log(
                UUID.fromString(uid), ip,
                authed != null ? authed.getFullName() : null,
                appUuid,
                appName,
                appHost,
                connUuid,
                connName,
                Decision.ALLOW,
                "Xác thực thành công"
        );

        HttpResFrame res = HttpResFrame.ok(reqId, "");
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(res.encode())));
    }

    private void handleResourceAccess(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        ResourceAccessFrame req = new ResourceAccessFrame();
        req.decode(data);

        String reqId = req.getRequestId();
        String appId = req.getAppId();


        String uid = this.userId;
        if (uid == null) {
            sendResourceFail(ctx, reqId, "Chưa xác thực (Vui lòng gọi HTTP_ACCESS trước)");
            return;
        }

        Session session = sessionLifecycleService.openSession(req.getRequestId(), uid, appId, ctx.channel());
        if (session == null) {
            sendResourceFail(ctx, reqId, "Không thể mở ứng dụng: bạn không có quyền, tài khoản bị khóa, app không tồn tại hoặc connector offline");
            return;
        }
        log.info("[Gateway] RESOURCE_ACCESS cho appId={}, reqId={}, streamId={}", appId, reqId, session.getStreamId());


        String ip = clientIp(ctx);
    }

    private void sendHttpFail(ChannelHandlerContext ctx, String reqId, byte status, String reason) {
        HttpResFrame resp = HttpResFrame.failed(reqId, status, reason);
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(resp.encode())));
    }

    private void sendResourceFail(ChannelHandlerContext ctx, String reqId, String reason) {
        ResourceResFrame resp = ResourceResFrame.failed(reqId,ResourceResFrame.STATUS_APP_NOT_FOUND ,reason);
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(resp.encode())));
    }

    public void handleClose(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        CloseFrame frame = new CloseFrame();
        frame.decode(data);

        String streamId = frame.getStreamId();
        log.info("[User] CLOSE stream={} reason={}", streamId, frame.getReasonText());
        sessionLifecycleService.sessionDisconnect(streamId, "CLIENT_CLOSED: " + frame.getReasonText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (userId == null) return;
        log.info("[User] {} disconnected", userId);
        userManager.removeByChannel(ctx.channel());
        sessionLifecycleService.onClientDisconnect(userId);
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
