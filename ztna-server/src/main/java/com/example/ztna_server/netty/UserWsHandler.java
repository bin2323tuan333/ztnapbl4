package com.example.ztna_server.netty;

import com.example.ztna_server.core.*;
import com.example.ztna_server.protocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class UserWsHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final ServiceRegistry registry;
    private final UserManager userManager;
    private final StreamManager streamManager;
    private final StreamIdGenerator idGenerator;

    public UserWsHandler(ServiceRegistry registry,
                         UserManager userManager,
                         StreamManager streamManager,
                         StreamIdGenerator idGenerator) {
        this.registry = registry;
        this.userManager = userManager;
        this.streamManager = streamManager;
        this.idGenerator = idGenerator;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("User WS connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        UserSession session = userManager.removeByChannel(ctx.channel());
        if (session != null) {
            long online = userManager.getOnlineSeconds(session.getUserId());
            log.info("User {} disconnected (online {}s)", session.getUserId(), online);
        }
        streamManager.removeByUser(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("UserWsHandler error: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof PingWebSocketFrame ping) {
            ctx.writeAndFlush(new PongWebSocketFrame(ping.content().retain()));
            return;
        }

        if (frame instanceof CloseWebSocketFrame) {
            log.info("User sent Close frame");
            ctx.close();
            return;
        }

        if (!(frame instanceof BinaryWebSocketFrame bin)) {
            log.warn("Unsupported frame: {}", frame.getClass().getSimpleName());
            return;
        }

        ByteBuf buf = bin.content();
        if (buf.readableBytes() < 1) return;

        byte type = Frame.peekType(buf);

        switch (type) {
            case FrameType.CONNECT -> handleConnect(ctx, buf);
            case FrameType.DATA    -> handleData(ctx, buf);
            case FrameType.CLOSE   -> handleClose(ctx, buf);
            default -> log.warn("Unknown frame type: 0x{}", String.format("%02X", type));
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, ByteBuf buf) {
        if (userManager.containsChannel(ctx.channel())) {
            log.warn("User already has session, ignoring CONNECT");
            sendConnectAck(ctx, 0, FrameType.STATUS_REFUSED, "Already connected");
            return;
        }

        ConnectFrame cf;
        try {
            cf = Frame.decodeConnect(buf);
        } catch (Exception e) {
            log.error("Malformed CONNECT frame", e);
            ctx.close();
            return;
        }

        log.info("CONNECT: service={}, jwt={}",
                cf.getService(), truncate(cf.getJwt()));

        String userId = verifyJwt(cf.getJwt());
        if (userId == null) {
            log.warn("Invalid JWT");
            sendConnectAck(ctx, 0, FrameType.STATUS_AUTH_FAIL, "Invalid token");
            return;
        }

        userManager.register(userId, ctx.channel());

        ConnectorSession connector = registry
                .findConnectorByService(cf.getService())
                .orElse(null);

        if (connector == null) {
            log.warn("No connector for service: {}", cf.getService());
            sendConnectAck(ctx, 0, FrameType.STATUS_SERVICE_NOT_FOUND, "Service not found");
            return;
        }

        int streamId = idGenerator.next();
        log.info("Server assigned streamId={} for user {} service {}",
                streamId, userId, cf.getService());

        streamManager.register(
                streamId,
                ctx.channel(),
                connector.getChannel(),
                cf.getService()
        );

        sendConnectAck(ctx, streamId, FrameType.STATUS_OK, null);

        ByteBuf out = Frame.encodeConnect(ctx.alloc(), cf.getService(), cf.getJwt());
        connector.getChannel().writeAndFlush(new BinaryWebSocketFrame(out));

        log.info("CONNECT forwarded to connector {}", connector.getId());
    }

    private void handleData(ChannelHandlerContext ctx, ByteBuf buf) {
        UserSession session = userManager.getByChannel(ctx.channel());
        if (session == null) {
            log.warn("DATA before CONNECT");
            ctx.close();
            return;
        }

        DataFrame df;
        try {
            df = Frame.decodeData(buf);
        } catch (Exception e) {
            log.error("Malformed DATA frame", e);
            ctx.close();
            return;
        }

        log.debug("DATA: streamId={}, dataLen={}", df.getStreamId(), df.getDataLen());

        Channel connector = streamManager.getConnector(df.getStreamId());
        if (connector == null || !connector.isActive()) {
            log.warn("No connector for streamId={}", df.getStreamId());
            return;
        }

        ByteBuf out = Frame.encodeData(ctx.alloc(), df.getStreamId(), df.getData());
        connector.writeAndFlush(new BinaryWebSocketFrame(out));
    }

    private void handleClose(ChannelHandlerContext ctx, ByteBuf buf) {
        CloseFrame cf;
        try {
            cf = Frame.decodeClose(buf);
        } catch (Exception e) {
            log.error("Malformed CLOSE frame", e);
            return;
        }

        log.info("CLOSE: streamId={}", cf.getStreamId());

        Channel connector = streamManager.getConnector(cf.getStreamId());
        streamManager.remove(cf.getStreamId());

        if (connector != null && connector.isActive()) {
            ByteBuf out = Frame.encodeClose(ctx.alloc(), cf.getStreamId(), cf.getReason());
            connector.writeAndFlush(new BinaryWebSocketFrame(out));
        }
    }

    private void sendConnectAck(ChannelHandlerContext ctx,
                                int streamId, byte status, String reason) {
        ByteBuf out = Frame.encodeConnectAck(ctx.alloc(), streamId, status, reason);
        ctx.writeAndFlush(new BinaryWebSocketFrame(out));
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
            log.warn("Invalid JWT: {}", e.getMessage());
            return null;
        }
    }

    private String truncate(String s) {
        if (s == null) return "null";
        return s.length() > 20 ? s.substring(0, 20) + "..." : s;
    }
}