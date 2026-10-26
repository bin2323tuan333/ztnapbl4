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

@Slf4j
public class ConnectorHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final AttributeKey<Boolean> REGISTERED =
            AttributeKey.valueOf("connector.registered");

    private final ConnectorConfig config;
    private final TcpClient tcpClient;

    public ConnectorHandler(ConnectorConfig config, TcpClient tcpClient) {
        this.config = config;
        this.tcpClient = tcpClient;
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            log.info("[Connector] WS handshake complete");

            Channel channel = ctx.channel();

            String connectorId = "conn_01";
            RoutingReqFrame req = new RoutingReqFrame(connectorId);

            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(req.encode())));

            log.info("[Connector] Đã gửi RoutingReq cho {}", connectorId);
        }
        super.userEventTriggered(ctx, evt);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
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
//                case FrameType.ROUTING_REQUEST -> handleRoutingReq(ctx, buf);
                case FrameType.CONNECT      -> handleConnect(ctx, buf);
                case FrameType.DATA         -> handleData(buf);
                case FrameType.CLOSE        -> handleClose(buf);
                case FrameType.ROUTING_RESPONSE -> handleRoutingRes(buf);
                case FrameType.HEARTBEAT_ACK    -> handleHeartbeatAck(buf);
//                case FrameType.HEARTBEAT    -> handleHeartbeat(ctx, buf);
                default -> log.warn("Unknown frame type: 0x{}",
                        String.format("%02X", type));
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý frame type=0x{}", String.format("%02X", type), e);
            // Không đóng channel — chỉ log lỗi, cho frame khác tiếp tục
        }
    }

//    private void handleRoutingReq(ChannelHandlerContext ctx, ByteBuf buf) {
//        RoutingReqFrame req = new RoutingReqFrame();
//        req.decode(buf.array());
//        String connectorId = req.getConnectorId();
//        // lay tu db
////        List<App> apps = new ArrayList<>();
//        RoutingResFrame res = new RoutingResFrame();
//
////        res.setAppCount(apps.size());
////        res.setApps(apps);
//        ByteBuf buf1 = res.encode();
//        ctx.writeAndFlush(buf1);
//    }

    private void handleRoutingRes(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        RoutingResFrame response = new RoutingResFrame();
        response.decode(data);

        List<App> apps = response.getApps();
        Map<String, App> appMap = new HashMap<>();
        for (App item : apps) {
            appMap.put(item.getId(), item);
            log.info("{}, {}, {} \n", item.getName(), item.getPort(), item.getHost());
        }
        RoutingTable.getInstance().replaceAll(appMap);
    }

    private void handleHeartbeatAck(ByteBuf buf) {

    }

    private void handleHeartbeat(ChannelHandlerContext ctx, ByteBuf buf) {

    }

    // CONNECT
    private void handleConnect(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        ConnectFrame connectFrame = new ConnectFrame();
        connectFrame.decode(data);

        RoutingTable routingTable = RoutingTable.getInstance();
        App app = routingTable.get(connectFrame.getAppId());
        String streamId = connectFrame.getStreamId();
        log.info("{}, {}", streamId, app.getName());

        tcpClient.connect(streamId, app.getHost(), app.getPort(), ctx.channel());

        ConnectAckFrame connectAckFrame = new ConnectAckFrame(streamId, ConnectAckFrame.STATUS_OK, "");

        ctx.writeAndFlush(
                new BinaryWebSocketFrame(
                        Unpooled.wrappedBuffer(connectAckFrame.encode())
                )
        );
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

        log.debug("[Connector] Server → App: stream={}, {} bytes", streamId, payload.length);
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