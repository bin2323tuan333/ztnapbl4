
package org.example.listener;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.Factory;
import org.example.cache.ClientOpenRegistry;
import org.example.cache.StreamRegistry;
import org.example.tunnel.TunnelClient;

import java.net.SocketException;
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
        System.out.println("\n[LocalHandler] Đã kết nối vào port nội bộ!");
        System.out.println("[LocalHandler] Gửi ResourceAccess với appId: "+ resourceId);

        clientOpenRegistry.put(reqId, ctx.channel());
        // Nếu tunnel mất, TunnelClient sẽ reconnect rồi gửi request.
        tunnelClient.sendResourceAccess(reqId, resourceId);

        ctx.channel().config().setAutoRead(true);
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        if (!buf.isReadable()) {
            return;
        }

        // Chưa được cấp streamId thì tạm lưu dữ liệu.
        if (streamId == null) {
            System.out.println("[LocalHandler] Đệm " + buf.readableBytes() + " bytes (chờ streamId)");
            pendingBuf.add(buf.retain());
            return;
        }

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        // Stream cũ không thể tự phục hồi sau khi Server restart.
        if (!tunnelClient.sendStreamData(streamId, data)) {
            System.err.println("[LocalHandler] Tunnel đã mất hoặc stream không còn hợp lệ.");

            ctx.close();
        }
    }

    public void onAccessSuccess(String streamId) {
        this.streamId = streamId;
        System.out.println("[LocalHandler] Nhận streamId=" + streamId + " -> Đăng ký và xả buffer.");
        streamRegistry.register(streamId, ctx.channel());
        for (int i = 0; i < pendingBuf.size(); i++) {
            ByteBuf buf = pendingBuf.get(i);

            try {
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);

                if (!tunnelClient.sendStreamData(streamId, data)) {
                    System.err.println("[LocalHandler] Không thể xả buffer.");
                    ctx.close();
                    break;
                }
            } finally {
                buf.release();
            }
        }

        for (int i = 0; i < pendingBuf.size(); i++) {
            ByteBuf buf = pendingBuf.get(i);

            if (buf.refCnt() > 0) {
                buf.release();
            }
        }

        pendingBuf.clear();
    }

    public void closeFromGateway() {
        if (streamId != null) {
            streamRegistry.remove(streamId);
        }

        releasePendingBuffers();

        if (ctx != null && ctx.channel().isOpen()) {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("[LocalHandler] Client local đóng kết nối.");
        clientOpenRegistry.remove(reqId);
        releasePendingBuffers();
        if (streamId != null) {
            streamRegistry.remove(streamId);
            tunnelClient.sendStreamClose(streamId);
        }
    }

    private void releasePendingBuffers() {
        for (ByteBuf buf : pendingBuf) {
            if (buf.refCnt() > 0) {
                buf.release();
            }
        }

        pendingBuf.clear();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException && "Connection reset".equals(cause.getMessage())) {
            System.out.println("[LocalHandler] Client đóng kết nối đột ngột.");
        } else {
            System.err.println("[LocalHandler] Lỗi: " + cause.getMessage());
        }

        ctx.close();
    }

    public String getReqId() {
        return reqId;
    }
}
