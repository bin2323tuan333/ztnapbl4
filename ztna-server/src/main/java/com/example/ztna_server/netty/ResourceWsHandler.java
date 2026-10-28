package com.example.ztna_server.netty;

import com.example.ztna_server.core.*;
import com.example.ztna_server.protocol.FrameType;
import com.example.ztna_server.protocol.frames.CloseFrame;
import com.example.ztna_server.protocol.frames.HttpAccessFrame;
import com.example.ztna_server.protocol.frames.HttpResFrame;
import com.example.ztna_server.protocol.frames.ResourceAccessFrame;
import com.example.ztna_server.protocol.frames.ResourceResFrame;
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
        String jwt = req.getJwt();

        String uid;
        try {
            if (jwtService.isTokenExpired(jwt)) {
                sendHttpFail(ctx, reqId, HttpResFrame.STATUS_AUTH_FAILED, "JWT hết hạn");
                return;
            }
            uid = jwtService.extractUserId(jwt).toString();
        } catch (Exception e) {
            sendHttpFail(ctx, reqId, HttpResFrame.STATUS_AUTH_FAILED, "JWT không hợp lệ");
            return;
        }

        if (this.userId == null) {
            this.userId = uid;
            userManager.register(uid, ctx.channel());
        } else if (!this.userId.equals(uid)) {
            sendHttpFail(ctx, reqId, HttpResFrame.STATUS_AUTH_FAILED, "User mismatch");
            return;
        }

        HttpResFrame res = HttpResFrame.ok(reqId,"");
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(res.encode())));
        log.info("[Gateway] HTTP_ACCESS OK cho reqId={}, uid={}", reqId, uid);
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
            sendResourceFail(ctx, reqId, "App không tồn tại hoặc Connector offline");
            return;
        }
        log.info("[Gateway] RESOURCE_ACCESS cho appId={}, reqId={}, streamId={}", appId, reqId, session.getStreamId());
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
}