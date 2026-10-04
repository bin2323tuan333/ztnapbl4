package com.example.ztna_connector;
import com.example.ztna_connector.protocol.FrameCodec;
import com.example.ztna_connector.protocol.FrameType;
import com.example.ztna_connector.tcp.TcpSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectorHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(ConnectorHandler.class);

    private final ConnectorConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Long, TcpSession> sessions = new ConcurrentHashMap<>();

    public ConnectorHandler(ConnectorConfig config) {
        this.config = config;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            log.info("WS handshake complete");
            List<String> services = List.copyOf(config.services().keySet());
            String json = mapper.writeValueAsString(services);
            ctx.writeAndFlush(new TextWebSocketFrame(json));
            log.info("Registered services: {}", services);
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame t) {
            log.debug("Text frame from server: {}", t.text());
            return;
        }
        if (!(frame instanceof BinaryWebSocketFrame bin)) return;

        ByteBuf buf = bin.content();
        byte type = FrameCodec.peekType(buf);
        switch (type) {
            case FrameType.CONNECT -> handleConnect(ctx, buf);
            case FrameType.DATA    -> handleData(buf);
            case FrameType.CLOSE   -> handleClose(buf);
            default -> log.warn("Unknown frame type: {}", type);
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, ByteBuf buf) {
        FrameCodec.ConnectHeader header = FrameCodec.decodeConnect(buf);
        long streamId = header.streamId();
        String service = header.service();
        log.info("CONNECT: streamId={} service={}", streamId, service);

        String target = config.services().get(service);
        if (target == null) {
            log.warn("Unknown service: {}", service);
            sendClose(ctx, streamId);
            return;
        }

        String[] parts = target.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel tcp) {
                        tcp.pipeline().addLast(new TcpToWsHandler(streamId, ctx.channel()));
                    }
                });

        b.connect(host, port).addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                Channel tcpChannel = f.channel();
                sessions.put(streamId, new TcpSession(streamId, service, tcpChannel, ctx.channel()));
                log.info("TCP connected: streamId={} -> {}:{}", streamId, host, port);
            } else {
                log.warn("TCP connect failed: streamId={} -> {}:{} err={}",
                        streamId, host, port, f.cause().getMessage());
                sendClose(ctx, streamId);
            }
        });
    }

    private void handleData(ByteBuf buf) {
        buf.readByte();
        long streamId = buf.readUnsignedInt();
        TcpSession session = sessions.get(streamId);
        if (session == null) {
            log.warn("DATA for unknown streamId={}", streamId);
            return;
        }
        ByteBuf payload = buf.retainedSlice(buf.readerIndex(), buf.readableBytes());
        session.getTcpChannel().writeAndFlush(payload);
    }

    private void handleClose(ByteBuf buf) {
        buf.readByte();
        long streamId = buf.readUnsignedInt();
        TcpSession session = sessions.remove(streamId);
        if (session != null) {
            log.info("CLOSE: streamId={}", streamId);
            session.getTcpChannel().close();
        }
    }

    private void sendClose(ChannelHandlerContext ctx, long streamId) {
        ByteBuf close = FrameCodec.encodeClose(ctx.alloc(), streamId);
        ctx.writeAndFlush(new BinaryWebSocketFrame(close));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("WS closed. Cleaning {} sessions", sessions.size());
        sessions.values().forEach(s -> s.getTcpChannel().close());
        sessions.clear();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Connector error: {}", cause.getMessage());
        ctx.close();
    }

    private class TcpToWsHandler extends ChannelInboundHandlerAdapter {
        private final long streamId;
        private final Channel wsChannel;

        TcpToWsHandler(long streamId, Channel wsChannel) {
            this.streamId = streamId;
            this.wsChannel = wsChannel;
        }

        @Override
        public void channelRead(ChannelHandlerContext tcpCtx, Object msg) {
            if (!(msg instanceof ByteBuf data)) return;
            ByteBuf out = FrameCodec.encodeData(wsChannel.alloc(), streamId, data);
            wsChannel.writeAndFlush(new BinaryWebSocketFrame(out));
            data.release();
        }

        @Override
        public void channelInactive(ChannelHandlerContext tcpCtx) {
            log.info("TCP closed: streamId={}", streamId);
            sessions.remove(streamId);
            if (wsChannel.isActive()) {
                ByteBuf close = FrameCodec.encodeClose(wsChannel.alloc(), streamId);
                wsChannel.writeAndFlush(new BinaryWebSocketFrame(close));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext tcpCtx, Throwable cause) {
            log.warn("TCP error: streamId={} err={}", streamId, cause.getMessage());
            tcpCtx.close();
        }
    }
}