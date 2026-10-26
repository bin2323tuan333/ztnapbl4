package com.example.ztna_server.netty;

import com.example.ztna_server.core.*;
import com.example.ztna_server.protocol.*;
import com.example.ztna_server.protocol.frames.CloseFrame;
import com.example.ztna_server.protocol.frames.OpenReqFrame;
import com.example.ztna_server.protocol.frames.OpenResFrame;
import com.example.ztna_server.routes.Session;
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

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ResourceWsHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final JwtService jwtService;
    private final UserManager userManager;
    private final SessionLifecycleService sessionLifecycleService;

    private volatile String userId;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete hs) {
            log.info("[App] ✓ WS handshake complete from {} | uri={}",
                    ctx.channel().remoteAddress(), hs.requestUri());
        }
        super.userEventTriggered(ctx, evt);
    }


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

        if (!(frame instanceof BinaryWebSocketFrame bin)) {
            log.warn("[User] Unsupported frame: {}", frame.getClass().getSimpleName());
            return;
        }

        ByteBuf buf = bin.content();
        if (buf.readableBytes() < 1) return;

        byte type = buf.getByte(0);

        try {
            switch (type) {
                case FrameType.OPEN_REQUEST -> handleOpenRequest(ctx, buf);
                case FrameType.DATA  -> handleData(buf);
                case FrameType.CLOSE -> handleClose(buf);
                default -> log.warn("[User] Unknown frame: 0x{}", String.format("%02X", type));
            }
        } catch (Exception e) {
            log.error("[User] Lỗi xử lý frame 0x{}", String.format("%02X", type), e);
        }
    }
    private void handleOpenRequest(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        OpenReqFrame req = new OpenReqFrame();
        req.decode(data);

        String resourceId = req.getAppId();
        String jwt = req.getJwt();

        log.info("[App] OPEN_REQUEST appId={} jwtLen={}", resourceId, jwt == null ? 0 : jwt.length());
        String uid = "admin1";
//        String uid = jwtService.verifyAndGetUserId(jwt);
//        if (uid == null) {
//            log.warn("[App] JWT invalid hoặc hết hạn → reject");
//            sendOpenFailed(ctx, OpenResFrame.STATUS_AUTH_FAILED,
//                    "JWT invalid or expired");
//            return;
//        }

//        // 3. Set userId (lần đầu)
//        if (this.userId == null) {
//            this.userId = uid;
//            userManager.add(uid, ctx.channel());
//            log.info("[App] ✓ {} authenticated", uid);
//        } else if (!this.userId.equals(uid)) {
//            // Cùng WS nhưng JWT khác user → từ chối
//            log.warn("[App] User mismatch: {} vs {}", this.userId, uid);
//            sendOpenFailed(ctx, OpenResFrame.STATUS_AUTH_FAILED,
//                    "User mismatch on same connection");
//            return;
//        }

        Session session = sessionLifecycleService.openSession(uid, resourceId, ctx.channel());
        if (session == null) {
            sendOpenFailed(ctx, OpenResFrame.STATUS_APP_NOT_FOUND,
                    "App không tồn tại hoặc Connector offline");
            return;
        }

        OpenResFrame resp = OpenResFrame.ok(session.getStreamId());
        ctx.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(resp.encode())
        ));

        log.info("[App] OPEN_OK streamId={} user={} app={}",
                session.getStreamId(), uid, resourceId);
    }

    private void sendOpenFailed(ChannelHandlerContext ctx, byte status, String reason) {
        OpenResFrame resp = OpenResFrame.failed("", status, reason);
        ctx.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(resp.encode())
        ));
    }


    public void handleClose(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        CloseFrame frame = new CloseFrame();
        frame.decode(data);

        String streamId = frame.getStreamId();
        String reason = frame.getReasonText();

        log.info("[User] CLOSE stream={} reason={}", streamId, reason);
        sessionLifecycleService.sessionDisconnect(streamId, "CLIENT_CLOSED: " + reason);
    }

    public void handleData(ByteBuf buf) {
        sessionLifecycleService.forwardToConnector(buf);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("[User] WS connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (userId == null) return;

        log.info("[User] {} disconnected", userId);

        UserSession session = userManager.removeByChannel(ctx.channel());
        if (session != null) {
            long online = userManager.getOnlineSeconds(session.getUserId());
            log.info("[User] {} was online for {}s", userId, online);
        }

        sessionLifecycleService.onClientDisconnect(userId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[User] Lỗi: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    private String extractToken(String uri) {
        if (uri == null) return null;
        int q = uri.indexOf('?');
        if (q == -1) return null;
        for (String pair : uri.substring(q + 1).split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    private String verifyJwt(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payload = new String(
                    java.util.Base64.getUrlDecoder().decode(parts[1]),
                    java.nio.charset.StandardCharsets.UTF_8
            );

            int start = payload.indexOf("\"userId\":\"");
            if (start < 0) return null;
            start += 10;
            int end = payload.indexOf("\"", start);
            if (end < 0) return null;

            return payload.substring(start, end);
        } catch (Exception e) {
            log.warn("[User] Invalid JWT: {}", e.getMessage());
            return null;
        }
    }

}