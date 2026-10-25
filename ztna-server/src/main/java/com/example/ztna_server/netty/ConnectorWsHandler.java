package com.example.ztna_server.netty;

import com.example.ztna_server.core.ServiceRegistry;
import com.example.ztna_server.dto.App;
import com.example.ztna_server.protocol.*;
import com.example.ztna_server.protocol.frames.*;
import com.example.ztna_server.routes.ConnectionRegister;
import com.example.ztna_server.service.RoutingService;
import com.example.ztna_server.routes.Session;
import com.example.ztna_server.routes.SessionManager;
import com.example.ztna_server.service.ConnectionLifecycleService;
import com.example.ztna_server.service.SessionLifecycleService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ConnectorWsHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final RoutingService routingService;
    private final ConnectionLifecycleService connectionLifecycleService;
    private final SessionLifecycleService sessionLifecycleService;

    private volatile String connectorId;
    private volatile boolean registered = false;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            log.info("[Server] WS handshake complete");
        }
        super.userEventTriggered(ctx, evt);
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
            log.info("BinaryWebSocketFrame unreceived");
            return;
        }

        ByteBuf data = bin.content();
        byte type = data.getByte(0);
        try {
            switch (type) {
                case FrameType.ROUTING_REQUEST -> handleRoutingReq(ctx, data);
                case FrameType.HEARTBEAT -> handleHeartbeat(ctx, data);
                case FrameType.CONNECT_ACK -> handleConnectAck(ctx, data);
                case FrameType.DATA        -> handleData(data);
                case FrameType.CLOSE       -> handleClose(ctx, data);
                default -> log.warn("[Connector] Unknown frame: 0x{}", String.format("%02X", type));
            }
        } catch (Exception e) {
            log.error("[Connector] Lỗi xử lý frame 0x{}", String.format("%02X", type), e);
        }

    }


    private void handleRoutingReq(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        RoutingReqFrame frame = new RoutingReqFrame();
        frame.decode(data);

        String connectorId = frame.getConnectorId();
        this.connectorId = connectorId;

        connectionLifecycleService.register(connectorId, ctx.channel());
        this.registered = true;

        log.info("[Connector] {} registered", connectorId);

        List<App> apps = routingService.reload(connectorId);
        log.info("[Connector] {} has {} apps", connectorId, apps.size());

        RoutingResFrame res = new RoutingResFrame();
        res.setAppCount(apps.size());
        res.setApps(apps);

        ctx.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(res.encode())
        ));
    }

    private void handleHeartbeat(ChannelHandlerContext ctx, ByteBuf buf) {
        if (buf.readableBytes() < 8) return;
        long clientTs = buf.readLong();

        connectionLifecycleService.markAlive(connectorId);

        HeartBeatAckFrame ack = new HeartBeatAckFrame(clientTs);
        ctx.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(ack.encode())
        ));

        log.debug("[Connector] Heartbeat from {}", connectorId);
    }

    private  void handleConnectAck(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        ConnectAckFrame frame = new ConnectAckFrame();
        frame.decode(data);

        int streamId = frame.getStreamId();
        byte status = frame.getStatus();

        if (status == 0x00) {
            sessionLifecycleService.onConnectAckOk(streamId);
        } else {
            sessionLifecycleService.onConnectAckFailed(streamId, frame.getReason());
        }
    }

    private void handleData(ByteBuf buf) {
        sessionLifecycleService.forwardToClient(buf);
    }

    private void handleClose(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        CloseFrame frame = new CloseFrame();
        frame.decode(data);

        int streamId = frame.getStreamId();
        String reason = frame.getReasonText();

        log.info("[Connector] CLOSE stream={} reason={}", streamId, reason);
        sessionLifecycleService.sessionDisconnect(streamId, "APP_CLOSED: " + reason);
    }



    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (connectorId == null || !registered) return;

        log.info("[Connector] {} disconnected", connectorId);
        connectionLifecycleService.disconnect(connectorId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[Connector] Lỗi", cause);
        ctx.close();
    }
}
