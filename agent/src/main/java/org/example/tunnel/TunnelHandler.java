package org.example.tunnel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.example.listener.LocalHandler;
import org.example.protocol.FrameType;
import org.example.protocol.frames.*;
import org.example.stream.StreamRegistry;

public class TunnelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final StreamRegistry streamRegistry;

    public TunnelHandler(StreamRegistry streamRegistry) {
        this.streamRegistry = streamRegistry;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[Tunnel] Đã kết nối thành công tới Gateway!");
        super.channelActive(ctx);
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
            return;
        }

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
            System.err.println("[Tunnel] Lỗi parse frame type=0x"
                    + String.format("%02X", type) + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleOpenResponse(ByteBuf buf) {
        OpenResFrame resFrame = new OpenResFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        resFrame.decode(data);

        String streamId = resFrame.getStreamId();
        System.out.println("[Tunnel] ← Nhận OPEN_RESPONSE: streamId=" + streamId + " (status=" + resFrame.getStatus() + ")");

        Channel channel = streamRegistry.pop();
        if (channel == null || !channel.isActive()) {
            System.err.println("[Tunnel] ❌ Không có channel nào đang chờ (hoặc đã chết)");
            return;
        }

        LocalHandler handler = channel.pipeline().get(LocalHandler.class);
        if (handler == null) {
            System.err.println("[Tunnel] ❌ Không tìm thấy LocalHandler trong pipeline");
            channel.close();
            return;
        }

        if (resFrame.isOk()) {
            streamRegistry.register(streamId, channel);
            handler.onOpenResponse(streamId, null);
        } else {
            handler.onOpenResponse(null, resFrame.getReason());
        }
    }

    private void handleData(ByteBuf buf) {
        DataFrame dataFrame = new DataFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        dataFrame.decode(data);

        String streamId = dataFrame.getStreamId();
        Channel channel = streamRegistry.get(streamId);

        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(Unpooled.wrappedBuffer(dataFrame.getData()));
        } else {
            System.err.println("[Tunnel] ❌ Nhận DATA cho streamId=" + streamId + " nhưng channel không tồn tại!");
        }
    }

    private void handleClose(ByteBuf buf) {
        CloseFrame closeFrame = new CloseFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        closeFrame.decode(data);

        String streamId = closeFrame.getStreamId();

        Channel channel = streamRegistry.remove(streamId);

        if (channel != null) {
            System.out.println("[Tunnel] ← Nhận CLOSE cho streamId=" + streamId + ", đóng channel.");
            channel.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.err.println("[Tunnel] Mất kết nối — đóng tất cả stream");
        streamRegistry.closeAll();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[Tunnel] Lỗi đường truyền: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}