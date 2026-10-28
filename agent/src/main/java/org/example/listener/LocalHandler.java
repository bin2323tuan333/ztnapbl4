package org.example.listener;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.Factory;
import org.example.cache.ClientOpenRegistry;
import org.example.cache.StreamRegistry;
import org.example.tunnel.TunnelClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final String resourceId;
    private final String reqId;
    private final TunnelClient tunnelClient = Factory.tunnelClient;
    private final StreamRegistry streamRegistry = Factory.streamRegistry;
    private final ClientOpenRegistry clientOpenRegistry = Factory.clientOpenRegistry;

    private String streamId;
    private ChannelHandlerContext ctx;
    private final List<ByteBuf> pendingBuf = new ArrayList<>();

    public LocalHandler(String resourceId) {
        this.resourceId = resourceId;
        this.reqId = UUID.randomUUID().toString();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        System.out.println("\n[LocalHandler] đã kết nối vào port nội bộ!");
        System.out.println("[LocalHandler] Chuẩn bị gửi ResourceAccess với appId: " + resourceId);
        clientOpenRegistry.put(reqId, ctx.channel());
        tunnelClient.sendResourceAccess(reqId, resourceId);

        ctx.channel().config().setAutoRead(true);
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        if (!buf.isReadable()) return;

        if (streamId == null) {
            System.out.println("[LocalHandler] ⏳ Đệm " + buf.readableBytes() + " bytes (chờ streamId)");
            pendingBuf.add(buf.retain());
            return;
        }

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        tunnelClient.sendStreamData(streamId, data);
    }

    public void onAccessSuccess(String streamId) {
        this.streamId = streamId;
        System.out.println("[LocalHandler] ✅ Nhận streamId=" + streamId + " → Đăng ký và xả đệm.");
        streamRegistry.register(streamId, ctx.channel());
        for (ByteBuf buf : pendingBuf) {
            try {
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                tunnelClient.sendStreamData(streamId, data);
            } finally {
                buf.release();
            }
        }
        pendingBuf.clear();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("[LocalHandler] Client local đóng kết nối.");
        if (streamId != null) {
            Factory.streamRegistry.remove(streamId);
            Factory.tunnelClient.sendStreamClose(streamId);
        }
    }

    public String getReqId() {
        return reqId;
    }
}