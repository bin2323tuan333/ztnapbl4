package org.example.listener;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.stream.StreamRegistry;
import org.example.tunnel.TunnelClient;

public class LocalHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final String resourceId;
    private final String streamId;
    private final TunnelClient tunnelClient;
    private final StreamRegistry streamRegistry;

    public LocalHandler(String resourceId, String streamId, TunnelClient tunnelClient, StreamRegistry streamRegistry) {
        this.resourceId = resourceId;
        this.streamId = streamId;
        this.tunnelClient = tunnelClient;
        this.streamRegistry = streamRegistry;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("\n[LocalHandler] ✓ " + resourceId + " đã kết nối vào port nội bộ!");

        streamRegistry.register(streamId, ctx.channel());

        ctx.channel().config().setAutoRead(true);
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        int len = buf.readableBytes();
        if (len == 0) return;

        byte[] data = new byte[len];
        buf.readBytes(data);
        System.out.println("[LocalHandler] ← Chrome gửi " + len + " bytes (sẽ đẩy lên Gateway)");
        tunnelClient.sendStreamData(streamId, data);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("[LocalHandler] Client local đóng kết nối.");
        streamRegistry.remove(streamId);
//        tunnelClient.sendStreamClose(streamId);
    }
}