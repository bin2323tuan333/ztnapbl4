
package org.example.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.Factory;
import org.example.cache.ClientOpenRegistry;
import org.example.cache.StreamRegistry;
import org.example.tunnel.TunnelClient;

import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final String resourceId;
    private final String reqId;
    private final String ticket;

    private final TunnelClient tunnelClient = Factory.tunnelClient;
    private final StreamRegistry streamRegistry = Factory.streamRegistry;
    private final ClientOpenRegistry clientOpenRegistry = Factory.clientOpenRegistry;

    private String streamId;
    private ChannelHandlerContext ctx;

    private final List<ByteBuf> pendingBuf = new ArrayList<>();

    public LocalHandler(String resourceId, String ticket) {
        this.resourceId = resourceId;
        this.ticket = ticket;
        this.reqId = UUID.randomUUID().toString();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        System.out.println("\n[LocalHandler] Đã kết nối vào port nội bộ!");
        System.out.println("[LocalHandler] Gửi ResourceAccess với appId: "+ resourceId);

        clientOpenRegistry.put(reqId, ctx.channel());
        // Nếu tunnel mất, TunnelClient sẽ reconnect rồi gửi request.
        tunnelClient.sendResourceAccess(reqId, resourceId, ticket);

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

    /**
     * Server/Connector từ chối mở stream (không có quyền, app nội bộ không chạy, connector offline...).
     * Nếu client local là trình duyệt (đã gửi HTTP request) thì trả trang lỗi 502 có lý do
     * để người dùng thấy thông báo; client khác (ssh, mysql) thì đóng kết nối ngay để client tự báo lỗi.
     */
    public void onAccessDenied(String reason) {
        String msg = (reason == null || reason.isBlank()) ? "Không thể kết nối tới ứng dụng" : reason;
        if (ctx == null || !ctx.channel().isActive()) {
            releasePendingBuffers();
            return;
        }
        if (looksLikeHttpRequest()) {
            String html = "<!doctype html><html><head><meta charset=\"utf-8\"><title>ZTNA - Không truy cập được</title></head>"
                    + "<body style=\"font-family:sans-serif;max-width:560px;margin:60px auto\">"
                    + "<h2>Không truy cập được ứng dụng</h2><p>" + escapeHtml(msg) + "</p>"
                    + "<p style=\"color:#666\">Hãy đóng tab này và thử lại từ Portal, hoặc liên hệ quản trị viên.</p></body></html>";
            byte[] body = html.getBytes(StandardCharsets.UTF_8);
            String head = "HTTP/1.1 502 Bad Gateway\r\nContent-Type: text/html; charset=utf-8\r\n"
                    + "Content-Length: " + body.length + "\r\nConnection: close\r\n\r\n";
            releasePendingBuffers();
            ctx.writeAndFlush(Unpooled.wrappedBuffer(head.getBytes(StandardCharsets.US_ASCII), body))
                    .addListener(ChannelFutureListener.CLOSE);
        } else {
            releasePendingBuffers();
            ctx.close();
        }
    }

    /** Dữ liệu client gửi trước (đang được đệm) có bắt đầu bằng một phương thức HTTP không. */
    private boolean looksLikeHttpRequest() {
        if (pendingBuf.isEmpty()) return false;
        ByteBuf first = pendingBuf.get(0);
        int n = Math.min(first.readableBytes(), 8);
        String start = first.toString(first.readerIndex(), n, StandardCharsets.US_ASCII);
        for (String m : new String[]{"GET ", "POST ", "PUT ", "HEAD ", "DELETE ", "OPTIONS ", "PATCH "}) {
            if (start.startsWith(m)) return true;
        }
        return false;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
