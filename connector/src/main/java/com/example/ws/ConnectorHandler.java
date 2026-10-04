package com.example.ws;

import com.example.tcp.TcpClient;
import com.example.tcp.TcpSession;
import com.example.tcp.TcpSessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class ConnectorHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final AttributeKey<Boolean> REGISTERED =
            AttributeKey.valueOf("connector.registered");

    private final ConnectorConfig config;
    private final TcpClient tcpClient;
    private final TcpSessionManager sessionManager;

    public ConnectorHandler(ConnectorConfig config,
                            TcpClient tcpClient,
                            TcpSessionManager sessionManager) {
        this.config = config;
        this.tcpClient = tcpClient;
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("WS connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("WS disconnected, closing {} TCP sessions", sessionManager.size());
        sessionManager.closeAll();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Handler error", cause);
        ctx.close();
    }

    // USER EVENT
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            log.info("WS handshake complete");
            if (!ctx.channel().hasAttr(REGISTERED)) {
                ctx.channel().attr(REGISTERED).set(true);
            }
            return;
        }

        if (evt instanceof IdleStateEvent idle) {
            if (idle.state() == IdleState.WRITER_IDLE) {
                log.debug("Sending PING");
                ctx.writeAndFlush(new PingWebSocketFrame());
            } else if (idle.state() == IdleState.READER_IDLE) {
                log.warn("Read idle 60s, closing");
                ctx.close();
            }
            return;
        }

        ctx.fireUserEventTriggered(evt);
    }


    // READ FRAME
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof PingWebSocketFrame ping) {
            ctx.writeAndFlush(new PongWebSocketFrame(ping.content().retain()));
            return;
        }

        if (frame instanceof PongWebSocketFrame) {
            log.debug("PONG received");
            return;
        }

        if (frame instanceof CloseWebSocketFrame) {
            log.info("Server sent Close");
            ctx.close();
            return;
        }

        if (!(frame instanceof BinaryWebSocketFrame bin)) {
            log.warn("Unsupported frame: {}", frame.getClass().getSimpleName());
            return;
        }

        ByteBuf buf = bin.content();
        if (buf.readableBytes() < 1) return;

        byte type = buf.getByte(buf.readerIndex());

        switch (type) {
            case 0x01 -> handleConnect(ctx, buf);
            case 0x02 -> handleData(buf);
            case 0x03 -> handleClose(buf);
            default -> log.warn("Unknown frame type: 0x{}", String.format("%02X", type));
        }
    }

    // CONNECT
    private void handleConnect(ChannelHandlerContext ctx, ByteBuf buf) {
        buf.readByte();
        int streamId = buf.readInt();

        int svcLen = buf.readUnsignedShort();
        byte[] svcBytes = new byte[svcLen];
        buf.readBytes(svcBytes);
        String service = new String(svcBytes, StandardCharsets.UTF_8);

        int jwtLen = buf.readUnsignedShort();
        byte[] jwtBytes = new byte[jwtLen];
        buf.readBytes(jwtBytes);

        log.info("CONNECT: streamId={}, service={}", streamId, service);

        String target = config.getServices().get(service);
        if (target == null) {
            log.warn("Unknown service: {}", service);
            sendConnectAck(ctx, streamId, (byte) 0x02, "Unknown service");
            return;
        }

        String host = target.split(":")[0];
        int port = Integer.parseInt(target.split(":")[1]);

        tcpClient.connect(streamId, service, host, port, ctx.channel());
    }

    // DATA
    private void handleData(ByteBuf buf) {
        buf.readByte();
        int streamId = buf.readInt();
        int dataLen = buf.readInt();
        byte[] data = new byte[dataLen];
        buf.readBytes(data);

        TcpSession session = sessionManager.get(streamId);
        if (session == null) {
            log.warn("DATA for unknown streamId={}", streamId);
            return;
        }

        session.getTcpChannel().writeAndFlush(Unpooled.wrappedBuffer(data));
        log.debug("Server → App: {}B, stream={}", dataLen, streamId);
    }

    // CLOSE
    private void handleClose(ByteBuf buf) {
        buf.readByte();
        int streamId = buf.readInt();
        buf.readByte();

        log.info("CLOSE: streamId={}", streamId);
        tcpClient.close(streamId);
    }

    // HELPER
    private void sendConnectAck(ChannelHandlerContext ctx, int streamId,
                                byte status, String reason) {
        byte[] reasonBytes = reason == null ? new byte[0]
                : reason.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = ctx.alloc().buffer(1 + 4 + 1 + 2 + reasonBytes.length);
        buf.writeByte(0x04);
        buf.writeInt(streamId);
        buf.writeByte(status);
        buf.writeShort(reasonBytes.length);
        buf.writeBytes(reasonBytes);

        ctx.writeAndFlush(new BinaryWebSocketFrame(buf));
    }
}