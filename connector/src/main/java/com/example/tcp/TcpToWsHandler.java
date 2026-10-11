package com.example.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class TcpToWsHandler extends ChannelInboundHandlerAdapter {

    private final int streamId;
    private final Channel wsChannel;
    private final Consumer<Integer> onClose;

    public TcpToWsHandler(int streamId, Channel wsChannel, Consumer<Integer> onClose) {
        this.streamId = streamId;
        this.wsChannel = wsChannel;
        this.onClose = onClose;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ByteBuf data)) return;

        int len = data.readableBytes();

        ByteBuf frame = ctx.alloc().buffer(1 + 4 + 4 + len);
        frame.writeByte(0x02);
        frame.writeInt(streamId);
        frame.writeInt(len);
        frame.writeBytes(data);

        if (wsChannel.isActive()) {
            wsChannel.writeAndFlush(new BinaryWebSocketFrame(frame));
            log.debug("App → Server: {}B, stream={}", len, streamId);
        }

        data.release();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("TCP closed: stream={}", streamId);

        if (wsChannel.isActive()) {
            ByteBuf close = ctx.alloc().buffer(6);
            close.writeByte(0x03);
            close.writeInt(streamId);
            close.writeByte(0);

            wsChannel.writeAndFlush(new BinaryWebSocketFrame(close));
        }

        if (onClose != null) {
            onClose.accept(streamId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("TCP error: stream={}", streamId, cause);
        ctx.close();
    }
}