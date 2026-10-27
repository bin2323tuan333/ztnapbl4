package com.example.tcp;

import com.example.protocol.frames.DataFrame;
import com.example.routes.TcpManager;
import com.example.ws.ConnectorClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class TcpToWsHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final String streamId;
    private final Consumer<byte[]> frameSender;

    public TcpToWsHandler(String streamId, Consumer<byte[]> frameSender) {
        this.streamId = streamId;
        this.frameSender = frameSender;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TcpManager tcpManager = TcpManager.getInstance();
        tcpManager.add(streamId, ctx.channel());
        log.info("[stream={}] TCP active", streamId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if (!msg.isReadable()) return;

        int len = msg.readableBytes();

        byte[] payload = new byte[msg.readableBytes()];
        msg.readBytes(payload);

        DataFrame dataFrame = new DataFrame(streamId, payload);

        frameSender.accept(dataFrame.encode());
        log.info("[TcpToWsHandler] ← Nginx trả về {} bytes cho stream={}", len, streamId);
        log.debug("[stream={}] App → WS: {} bytes", streamId, payload.length);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        TcpManager.getInstance().remove(streamId);
        log.info("[TcpToWsHandler] [stream={}] Nginx đã đóng kết nối TCP", streamId);
//        TcpManager tcpManager = TcpManager.getInstance();
//        tcpManager.remove(streamId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[stream={}] Lỗi TCP", streamId, cause);
        TcpManager.getInstance().remove(streamId);
        ctx.close();
    }
}