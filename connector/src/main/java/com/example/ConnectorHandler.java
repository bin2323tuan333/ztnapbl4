package com.example;



import com.example.protocol.ConnectFrame;
import com.example.protocol.DataFrame;
import com.example.protocol.Frame;
import com.example.protocol.FrameType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Slf4j
public class ConnectorHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final ConnectorConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    public ConnectorHandler(ConnectorConfig config) {
        this.config = config;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            log.info("WS handshake complete");
            List<String> services = List.copyOf(config.getServices().keySet());
            String json = mapper.writeValueAsString(services);
            ctx.writeAndFlush(new TextWebSocketFrame(json));
            log.info("Registered services: {}", services);
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (!(frame instanceof BinaryWebSocketFrame bin))
            return;

        ByteBuf buf = bin.content();

        byte type = Frame.peekType(buf);   // 0x02 = DATA

        if (type == FrameType.DATA) {
            DataFrame df = Frame.decodeData(buf);

        }

        if (type == FrameType.CONNECT) {
            ConnectFrame cf = Frame.decodeConnect(buf);

            String service = cf.getService();
            log.info("CONNECT:, service={}", service);
            String target = config.getServices().get(service);

            if (target == null) {
                log.warn("Unknown service: {}", service);
                sendConnectAck(ctx, streamId,
                        FrameType.STATUS_SERVICE_NOT_FOUND,
                        "Unknown service");
                return;
            }

        }
    }

    private void handleConnect(ChannelHandlerContext ctx, ByteBuf buf) {

    }

    private void handleData(ByteBuf buf) {

    }

    private void handleClose(ByteBuf buf) {

    }

    private void sendClose(ChannelHandlerContext ctx, long streamId) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

    }
}