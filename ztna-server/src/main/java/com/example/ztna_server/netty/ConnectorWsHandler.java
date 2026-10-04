package com.example.ztna_server.netty;

import com.example.ztna_server.core.ConnectorSession;
import com.example.ztna_server.core.ServiceRegistry;
import com.example.ztna_server.core.StreamManager;
import com.example.ztna_server.protocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ConnectorWsHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final AttributeKey<ConnectorSession> SESSION =
            AttributeKey.valueOf("ztna.connector.session");

    private final ServiceRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();
    private final StreamManager streamManager;

    public ConnectorWsHandler(ServiceRegistry registry, StreamManager streamManager) {
        this.registry = registry;
        this.streamManager = streamManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // ① Ping → Pong
        if (frame instanceof PingWebSocketFrame ping) {
            ctx.writeAndFlush(new PongWebSocketFrame(ping.content().retain()));
            return;
        }

        // ② Close
        if (frame instanceof CloseWebSocketFrame) {
            log.info("Connector sent Close frame");
            ctx.close();
            return;
        }

        // ③ Chỉ xử lý Binary
        if (!(frame instanceof BinaryWebSocketFrame bin)) {
            log.warn("Unsupported frame: {}", frame.getClass().getSimpleName());
            return;
        }

        ByteBuf buf = bin.content();
        if (buf.readableBytes() < 1) return;

        // ④ Chưa đăng ký → frame đầu tiên = JSON services list
        if (!ctx.channel().hasAttr(SESSION)) {
            handleRegistration(ctx, buf);
            return;
        }

        // ⑤ Đã đăng ký → ZTNA frame (CONNECT_ACK, DATA, CLOSE)
        byte type = Frame.peekType(buf);
        log.debug("ZTNA frame from connector: 0x{}", String.format("%02X", type));

        switch (type) {
            case FrameType.CONNECT_ACK -> handleConnectAck(ctx, buf);
            case FrameType.DATA        -> handleData(ctx, buf);
            case FrameType.CLOSE       -> handleClose(ctx, buf);
            default -> log.warn("Unknown frame type: 0x{}", String.format("%02X", type));
        }
    }

    private void handleRegistration(ChannelHandlerContext ctx, ByteBuf buf) {
        try {
            // ① Đọc bytes
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);

            // ② Decode → String JSON
            String json = new String(bytes, StandardCharsets.UTF_8);

            // ③ Parse JSON → List<String>
            List<String> services = mapper.readValue(
                    json, new TypeReference<List<String>>() {});

            // ④ Tạo session
            String id = UUID.randomUUID().toString().substring(0, 8);
            ConnectorSession session = new ConnectorSession(
                    id, services, ctx.channel()
            );

            // ⑤ Đăng ký
            registry.register(session);
            ctx.channel().attr(SESSION).set(session);

            log.info("Connector {} registered with services: {}", id, services);

        } catch (Exception e) {
            log.error("Failed to parse connector services from bytes", e);
            ctx.close();
        }
    }

    // CONNECT_ACK — Forward về user
    private void handleConnectAck(ChannelHandlerContext ctx, ByteBuf buf) {
        ConnectAckFrame ack = Frame.decodeConnectAck(buf);
        log.info("CONNECT_ACK from connector: streamId={}, status={}",
                ack.getStreamId(), ack.getStatus());

        Channel user = streamManager.getUser(ack.getStreamId());
        if (user == null || !user.isActive()) {
            log.warn("No user for streamId={}", ack.getStreamId());
            return;
        }

        ByteBuf out = Frame.encodeConnectAck(
                ctx.alloc(), ack.getStreamId(), ack.getStatus(), ack.getReason()
        );
        user.writeAndFlush(new BinaryWebSocketFrame(out));
    }

    // DATA — Connector → User
    private void handleData(ChannelHandlerContext ctx, ByteBuf buf) {
        DataFrame df = Frame.decodeData(buf);

        Channel user = streamManager.getUser(df.getStreamId());
        if (user == null || !user.isActive()) {
            log.warn("No user for streamId={}", df.getStreamId());
            return;
        }

        ByteBuf out = Frame.encodeData(ctx.alloc(), df.getStreamId(), df.getData());
        user.writeAndFlush(new BinaryWebSocketFrame(out));

        log.debug("Connector → User: {}B, stream={}", df.getDataLen(), df.getStreamId());
    }

    // CLOSE — Connector → User
    private void handleClose(ChannelHandlerContext ctx, ByteBuf buf) {
        CloseFrame cf = Frame.decodeClose(buf);
        log.info("CLOSE from connector: streamId={}", cf.getStreamId());

        Channel user = streamManager.getUser(cf.getStreamId());
        streamManager.remove(cf.getStreamId());

        if (user != null && user.isActive()) {
            ByteBuf out = Frame.encodeClose(ctx.alloc(), cf.getStreamId(), cf.getReason());
            user.writeAndFlush(new BinaryWebSocketFrame(out));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ConnectorSession s = ctx.channel().attr(SESSION).get();
        if (s != null) {
            registry.unregister(s);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Connector handler error", cause);
        ctx.close();
    }
}