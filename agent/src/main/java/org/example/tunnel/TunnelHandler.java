package org.example.tunnel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.example.Factory;
import org.example.cache.ClientOpenRegistry;
import org.example.cache.PendingHttpRequest;
import org.example.launcher.MysqlLauncher;
import org.example.launcher.WebLauncher;
import org.example.listener.LocalHandler;
import org.example.listener.LocalServer;
import org.example.protocol.FrameType;
import org.example.protocol.frames.*;
import org.example.cache.StreamRegistry;

public class TunnelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final StreamRegistry streamRegistry = Factory.streamRegistry;
    private final PendingHttpRequest pendingHttpRequest = Factory.pendingHttpRequest;
    private final ClientOpenRegistry clientOpenRegistry = Factory.clientOpenRegistry;


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
                case FrameType.RESOURCE_RESPONSE -> handleResourceRes(buf);
                case FrameType.HTTP_RESPONSE -> handleHttpRes(buf);
                case FrameType.DATA          -> handleData(buf);
                case FrameType.CLOSE         -> handleClose(buf);
                default -> System.err.println("[Tunnel] Frame type lạ: 0x" + String.format("%02X", type));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleResourceRes(ByteBuf buf) {
        ResourceResFrame resFrame = new ResourceResFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        resFrame.decode(data);

        String reqId = resFrame.getRequestId();
        Channel localChannel = clientOpenRegistry.get(reqId);

        if (localChannel == null || !localChannel.isActive()) {
            System.err.println("[Tunnel] ❌ Không tìm thấy Channel của Chrome cho reqId=" + reqId);
            return;
        }

        if (resFrame.isOk()) {
            LocalHandler localHandler = localChannel.pipeline().get(LocalHandler.class);

            if (localHandler != null) {
                localHandler.onAccessSuccess(resFrame.getStreamId());
            } else {
                System.err.println("[Tunnel] ❌ Không tìm thấy LocalHandler trong pipeline!");
            }
        } else {
            System.err.println("[Tunnel] ❌ Gateway từ chối cấp stream: " + resFrame.getReason());
            localChannel.close();
        }
    }

    private void handleHttpRes(ByteBuf buf) {
        HttpResFrame resFrame = new HttpResFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        resFrame.decode(data);

        Channel channelHttp = pendingHttpRequest.getChannel(resFrame.getRequestId());
        String resourceId = pendingHttpRequest.getResource(resFrame.getRequestId());
        System.out.println("[Tunnel] DEBUG - resourceId lấy từ Cache: " + resourceId);

        if (channelHttp == null || !channelHttp.isActive()) {
            return;
        }

        HttpResponseStatus status;
        String jsonBody;

        if (resFrame.isOk()) {
            LocalServer server = new LocalServer(resourceId);
            int port = server.start();



            String type = pendingHttpRequest.getTypes(resFrame.getRequestId());
            try {
                if (type != null) {
                    switch (type) {
                        case "web" -> new WebLauncher().launch(port);
                        case "mysql" -> new MysqlLauncher().launch(port);
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi mở app: " + e.getMessage());
            }



            status = HttpResponseStatus.OK;
            jsonBody = "{\"status\":\"success\",\"port\":" + port + "}";

        } else {
            status = (resFrame.getStatus() == HttpResFrame.STATUS_AUTH_FAILED)
                    ? HttpResponseStatus.UNAUTHORIZED
                    : HttpResponseStatus.INTERNAL_SERVER_ERROR;
            jsonBody = "{\"error\":\"" + resFrame.getReason() + "\"}";
        }

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(jsonBody, CharsetUtil.UTF_8));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type");

        channelHttp.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleData(ByteBuf buf) {
        DataFrame dataFrame = new DataFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        dataFrame.decode(data);

        Channel channel = streamRegistry.get(dataFrame.getStreamId());
        if (channel != null && channel.isActive()) {
            System.out.println("[Tunnel] [Server -> Agent] " + dataFrame.getData().length + " bytes");

            String content = new String(dataFrame.getData(), java.nio.charset.StandardCharsets.UTF_8);
            // In ra 500 ký tự đầu tiên (để tránh log bị quá dài)
            String preview = content.length() > 500 ? content.substring(0, 500) + "..." : content;

            // Thay thế \r và \n bằng ký tự hiển thị để dễ nhìn
            preview = preview.replace("\r", "\\r").replace("\n", "\\n\n");

            System.out.println("[Byte-Content]: \n" + preview);


            channel.writeAndFlush(Unpooled.copiedBuffer(dataFrame.getData()));



        } else {
            System.err.println("[Tunnel] Không tìm thấy Channel cho streamId=" + dataFrame.getStreamId());
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