package com.example.ws;

import com.example.protocol.frames.*;
import com.example.routes.RoutingTable;
import com.example.model.App;
import com.example.protocol.FrameType;
import com.example.tcp.TcpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConnectorHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final AttributeKey<Boolean> REGISTERED =
            AttributeKey.valueOf("connector.registered");

    private final ConnectorConfig config;
    private final TcpClient tcpClient;
    private ScheduledFuture<?> pullRoutingTask;

    public ConnectorHandler(ConnectorConfig config, TcpClient tcpClient) {
        this.config = config;
        this.tcpClient = tcpClient;
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            log.info("[Connector] WS handshake complete");

            pullRoutingTask = ctx.executor().scheduleAtFixedRate(() -> {
                if (ctx.channel().isActive()) {
                    RoutingReqFrame req = new RoutingReqFrame(config.getToken());
                    ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(req.encode())));
                    log.info("[Connector] Đã gửi RoutingReq định kỳ cho {}", config.getToken());
                }
            }, 0, 30, TimeUnit.SECONDS);

            log.info("[Connector] Đã gửi RoutingReq cho {}", config.getToken());
        }
        super.userEventTriggered(ctx, evt);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (pullRoutingTask != null && !pullRoutingTask.isCancelled()) {
            pullRoutingTask.cancel(true);
        }
        log.info("[Connector] Mất kết nối tới Gateway.");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (!(frame instanceof BinaryWebSocketFrame bin)) {
            log.error("WebSocketFrame received is not a BinaryWebSocketFrame");
            return;
        }

        ByteBuf buf = bin.content();
        if (buf.readableBytes() < 1) return;

        byte type = buf.getByte(buf.readerIndex());
        log.info("Nhan: {}", FrameType.getName(type));
        try {
            switch (type) {
                case FrameType.CONNECT      -> handleConnect(ctx, buf);
                case FrameType.DATA         -> handleData(buf);
                case FrameType.CLOSE        -> handleClose(buf);
                case FrameType.ROUTING_RESPONSE -> handleRoutingRes(buf);
                default -> log.warn("Unknown frame type: 0x{}",
                        String.format("%02X", type));
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý frame type=0x{}", String.format("%02X", type), e);
        }
    }


    private void handleRoutingRes(ByteBuf buf) {
        log.info("Nhan: ROUTING_RESPONSE");
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        RoutingResFrame resFrame = new RoutingResFrame();
        resFrame.decode(data);

        try {
            List<App> apps = resFrame.getApps();

            if (apps != null) {
                Map<String, App> newRoutes = new HashMap<>();
                for (App app : apps) {
                    newRoutes.put(app.getId(), app);
                    log.info(" -> Đã nhận App: id={}, name={}, target={}:{}",
                            app.getId(), app.getName(), app.getHost(), app.getPort());
                }

                RoutingTable.getInstance().replaceAll(newRoutes);

                log.info("[Connector] ✅ Cập nhật Routing Table thành công. Tổng số App: {}",
                        RoutingTable.getInstance().size());
            } else {
                log.warn("[Connector] ⚠ ROUTING_RESPONSE không chứa danh sách App!");
            }
        } catch (Exception e) {
            log.error("[Connector] Lỗi khi xử lý ROUTING_RESPONSE", e);
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        ConnectFrame connectFrame = new ConnectFrame();
        connectFrame.decode(data);

        String streamId = connectFrame.getStreamId();
        RoutingTable routingTable = RoutingTable.getInstance();
        App app = routingTable.get(connectFrame.getAppId());

        if (app == null) {
            log.error("[Connector] ❌ Không tìm thấy App {} trong RoutingTable!", connectFrame.getAppId());

            ConnectAckFrame ackFrame = new ConnectAckFrame(streamId, ConnectAckFrame.STATUS_APP_NOT_FOUND, "App not found on connector");
            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(ackFrame.encode())));
            return;
        }

        log.info("[Connector] Yêu cầu CONNECT tới App: {} ({}:{})", app.getName(), app.getHost(), app.getPort());

        tcpClient.connect(streamId, app.getHost(), app.getPort(), ctx.channel())
                .addListener(future -> {
                    if (future.isSuccess()) {
                        ConnectAckFrame ackFrame = new ConnectAckFrame(streamId, ConnectAckFrame.STATUS_OK, "");
                        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(ackFrame.encode())));
                        log.info("[Connector] ✅ Đã kết nối TCP thành công tới {}:{}", app.getHost(), app.getPort());
                    } else {
                        ConnectAckFrame ackFrame = new ConnectAckFrame(streamId, ConnectAckFrame.STATUS_CONNECT_FAILED, "TCP connect failed");
                        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(ackFrame.encode())));
                        log.error("[Connector] ❌ Kết nối TCP thất bại tới {}:{}", app.getHost(), app.getPort());
                    }
                });
    }

    // DATA
    private void handleData(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        DataFrame frame = new DataFrame();
        frame.decode(data);

        String streamId = frame.getStreamId();
        byte[] payload = frame.getData();

        if (payload == null || payload.length == 0) {
            log.debug("[Connector] DATA rỗng cho stream={}", streamId);
            return;
        }
        tcpClient.sendData(streamId, payload);

        log.info("[Connector] Nhận DATA cho stream={}, len={}", streamId, payload == null ? "null" : payload.length);
    }

    // CLOSE
    private void handleClose(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        CloseFrame frame = new CloseFrame();
        frame.decode(data);

        String streamId = frame.getStreamId();
        String reason = frame.getReasonText();

        log.info("CLOSE: streamId={}, reason: {}", streamId, reason);
        tcpClient.close(streamId);
    }

    // HELPER
    private void sendConnectAck(ChannelHandlerContext ctx, int streamId, byte status, String reason) {
        byte[] reasonBytes = reason == null ? new byte[0]
                : reason.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = ctx.alloc().buffer(1 + 4 + 1 + 2 + reasonBytes.length);
        buf.writeByte(0x04);
        buf.writeInt(streamId);
        buf.writeByte(status);
        buf.writeShort(reasonBytes.length);
        buf.writeBytes(reasonBytes);

        ctx.writeAndFlush(new BinaryWebSocketFrame(buf));
    }
}