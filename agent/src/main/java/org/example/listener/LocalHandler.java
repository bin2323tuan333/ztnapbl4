package org.example.listener;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.stream.StreamRegistry;
import org.example.tunnel.TunnelClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final String resourceId;
    private final TunnelClient tunnelClient;
    private final StreamRegistry streamRegistry;
    private final String jwt;

    private ChannelHandlerContext ctx;
    private String streamId; // Nhận từ Server, Agent không tự sinh

    // Đã bổ sung 2 biến bị thiếu
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final List<ByteBuf> pendingBuf = new ArrayList<>();

    public LocalHandler(String resourceId, TunnelClient tunnelClient, StreamRegistry streamRegistry, String jwt) {
        this.resourceId = resourceId;
        this.tunnelClient = tunnelClient;
        this.streamRegistry = streamRegistry;
        this.jwt = jwt;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        System.out.println("\n[LocalHandler] ✓ Client local kết nối (resourceId=" + resourceId + ")");

        streamRegistry.push(ctx.channel());

        System.out.println("[LocalHandler] → Gửi OPEN_REQ lên Server...");
        tunnelClient.sendOpenReq(resourceId, jwt);

        ctx.channel().config().setAutoRead(true);
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        int len = buf.readableBytes();
        if (len == 0) return;

        byte[] data = new byte[len];
        buf.readBytes(data);

        if (!ready.get()) {
            System.out.println("[LocalHandler] ⏳ Đệm " + len + " bytes (chờ Server cấp ID)");
            pendingBuf.add(ctx.alloc().buffer(len).writeBytes(data));
            return;
        }

        tunnelClient.sendStreamData(streamId, data);
    }

    public void onOpenResponse(String streamId, String errorReason) {
        if (streamId == null) {
            System.err.println("[LocalHandler] ❌ Server từ chối mở app: " + errorReason);
            ctx.close();
            return;
        }

        this.streamId = streamId;
        this.ready.set(true);

        System.out.println("[LocalHandler] ✅ Đã nhận streamId từ Server=" + streamId);

        if (!pendingBuf.isEmpty()) {
            System.out.println("  └─ Flush " + pendingBuf.size() + " chunk dữ liệu đang đệm...");
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
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[LocalHandler] Client local đóng kết nối.");

        if (ready.get() && streamId != null) {
            streamRegistry.remove(streamId);
            tunnelClient.sendStreamClose(streamId);
        } else {
            streamRegistry.removeQueued(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[LocalHandler] Lỗi: " + cause.getMessage());
        ctx.close();
    }
}