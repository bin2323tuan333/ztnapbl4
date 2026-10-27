package org.example.tunnel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.example.protocol.FrameType;
import org.example.protocol.frames.*;
import org.example.stream.StreamRegistry;

public class TunnelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final StreamRegistry streamRegistry;

    public TunnelHandler(StreamRegistry streamRegistry) {
        this.streamRegistry = streamRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof PingWebSocketFrame ping) {
            ctx.writeAndFlush(new PongWebSocketFrame(ping.content().retain()));
            return;
        }
        if (!(frame instanceof BinaryWebSocketFrame bin)) return;

        ByteBuf buf = bin.content();
        if (!buf.isReadable()) return;

        byte type = buf.getByte(buf.readerIndex());

        try {
            switch (type) {
                case FrameType.OPEN_RESPONSE -> handleOpenResponse(buf);
                case FrameType.DATA          -> handleData(buf);
                case FrameType.CLOSE         -> handleClose(buf);
                default -> System.err.println("[Tunnel] Frame type lạ: 0x" + String.format("%02X", type));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOpenResponse(ByteBuf buf) {
        OpenResFrame resFrame = new OpenResFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        resFrame.decode(data);

        System.out.println("[Tunnel] ← Nhận OPEN_RESPONSE: streamId=" + resFrame.getStreamId() + " (status=" + resFrame.getStatus() + ")");

        // Ném vào Queue để HTTP Thread đang chờ lấy ra
        streamRegistry.putOpenResponse(resFrame);
    }

    private void handleData(ByteBuf buf) {
        DataFrame dataFrame = new DataFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        dataFrame.decode(data);

        Channel channel = streamRegistry.get(dataFrame.getStreamId());
        if (channel != null && channel.isActive()) {
            System.out.println("[Tunnel] → Gateway trả về " + dataFrame.getData().length + " bytes (đẩy xuống Chrome)");
            channel.writeAndFlush(Unpooled.wrappedBuffer(dataFrame.getData()));
        } else {
            // THÊM DÒNG LOG NÀY
            System.err.println("[Tunnel] ❌ Không tìm thấy Channel cho streamId=" + dataFrame.getStreamId());
        }
    }

    private void handleClose(ByteBuf buf) {
        CloseFrame closeFrame = new CloseFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        closeFrame.decode(data);

        Channel channel = streamRegistry.remove(closeFrame.getStreamId());
        if (channel != null) channel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}