
package com.example.ztna_server.netty;

import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.protocol.FrameType;
import com.example.ztna_server.protocol.frames.*;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.dto.internal.App;
import com.example.ztna_server.runtime.connection.agent.AgentConnection;
import com.example.ztna_server.runtime.connection.agent.AgentConnectionRegistry;
import com.example.ztna_server.runtime.connection.agent.PendingConnect;
import com.example.ztna_server.runtime.connection.agent.PendingConnectRegistry;
import com.example.ztna_server.runtime.connection.connector.ConnectorConnection;
import com.example.ztna_server.runtime.connection.connector.ConnectorConnectionRegistry;
import com.example.ztna_server.runtime.routing.RouteTarget;
import com.example.ztna_server.runtime.routing.RoutingRegistry;
import com.example.ztna_server.service.SessionLifecycleService;
import com.example.ztna_server.runtime.relay.RelayEngine;
import com.example.ztna_server.runtime.relay.RelayResult;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ConnectorWsHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final RoutingRegistry routingRegistry;
    private final ConnectorConnectionRegistry connectorConnectionRegistry;
    private final SessionLifecycleService sessionLifecycleService;
    private final ConnectorRepository connectorRepository;
    private final PendingConnectRegistry pendingConnectRegistry;
    private final AgentConnectionRegistry agentConnectionRegistry;
    private final RelayEngine relayEngine;

    private volatile String connectorId;
    private volatile String connectionId;
    private volatile boolean registered = false;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof PingWebSocketFrame ping) {
            ctx.writeAndFlush(new PongWebSocketFrame(
                    ping.content().retain()
            ));
            return;
        }

        if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
            return;
        }

        if (!(frame instanceof BinaryWebSocketFrame bin)) {
            log.warn("[Connector] Unsupported WebSocket frame");
            return;
        }

        ByteBuf data = bin.content();
        if (!data.isReadable()) {
            log.warn("[Connector] Empty binary frame");
            return;
        }

        byte type = data.getByte(data.readerIndex());

        try {
            switch (type) {
                case FrameType.ROUTING_REQUEST -> handleRoutingReq(ctx, data);
                case FrameType.CONNECT_ACK -> handleConnectAck(ctx, data);
                case FrameType.DATA -> handleData(ctx, data);
                case FrameType.CLOSE -> handleClose(ctx, data);
                default -> log.warn("[Connector] Unknown frame type: 0x{}", String.format("%02X", type));
            }

        } catch (Exception e) {
            log.error("[Connector] Error processing frame: 0x{}, {}", String.format("%02X", type), e.getMessage());
        }
    }

    private void handleRoutingReq(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);

        RoutingReqFrame req = new RoutingReqFrame();
        req.decode(data);

        String connectionString = req.getConnectorId();
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("[Connector] Missing connectionString");
            ctx.close();
            return;
        }

        if (registered) {
            if (!isCurrentConnection(ctx.channel())) {
                log.warn("[Connector] Connection is no longer current: {}", connectorId);
                ctx.close();
                return;
            }

            // Kiểm lại trạng thái mỗi lần connector xin routing: đã bị tắt hoặc xoá thì ngắt
            boolean stillEnabled = connectorRepository.findById(java.util.UUID.fromString(connectorId))
                    .map(Connector::isEnabled)
                    .orElse(false);
            if (!stillEnabled) {
                log.warn("[Connector] Connector đã bị tắt hoặc xoá, ngắt kết nối: {}", connectorId);
                connectorConnectionRegistry.removeByConnectorId(connectorId);
                ctx.close();
                return;
            }

            sendRoutingTable(ctx);
            return;
        }

        Connector connector = connectorRepository.findByConnectionString(connectionString).orElse(null);

        if (connector == null || !connector.isEnabled()) {
            log.warn("[Connector] Invalid or disabled connector: {}", connectionString);
            ctx.close();
            return;
        }

        String newConnectorId = connector.getId().toString();
        ConnectorConnection oldConnection = connectorConnectionRegistry.getByConnectorId(newConnectorId);

        if (oldConnection != null && oldConnection.getChannel() != ctx.channel()) {
            log.warn("[Connector] Replacing old connection: connectorId={}, oldConnection={}",
                    newConnectorId, oldConnection.getConnectionId());

            // Phiên đang có của connector này đều thuộc kết nối cũ: đóng hết rồi gỡ kết nối cũ
            // (trước đây truyền nhầm connectionId vào hàm cần connectorId nên không đóng được gì)
            sessionLifecycleService.onConnectorDisconnect(newConnectorId);
            connectorConnectionRegistry.remove(oldConnection.getConnectionId());
        }

        // Đăng ký connection mới.
        ConnectorConnection connection = connectorConnectionRegistry.register(newConnectorId, ctx.channel());

        this.connectorId = newConnectorId;
        this.connectionId = connection.getConnectionId();
        this.registered = true;

        log.info(
                "[Connector] Registered: connectorId={}, connectionId={}",
                connectorId,
                connectionId
        );

        // Gửi routing table lần đầu.
        sendRoutingTable(ctx);
    }

    private void sendRoutingTable(ChannelHandlerContext ctx) {

        if (!isCurrentConnection(ctx.channel())) {
            return;
        }

        List<RouteTarget> targets =
                routingRegistry.getByConnectorId(connectorId);

        List<App> apps = targets.stream()
                .map(target -> new App(
                        target.resourceId(),
                        target.resourceName(),
                        target.host(),
                        target.port()
                ))
                .toList();

        RoutingResFrame response = new RoutingResFrame();

        response.setAppCount(apps.size());
        response.setApps(apps);

        ctx.writeAndFlush(
                new BinaryWebSocketFrame(
                        Unpooled.wrappedBuffer(response.encode())
                )
        );

        log.info(
                "[Connector] Routing table sent: connectorId={}, apps={}",
                connectorId,
                apps.size()
        );
    }

    private void handleConnectAck(ChannelHandlerContext ctx, ByteBuf buf) {
        if (!isCurrentConnection(ctx.channel())) {
            log.warn("[Connector] Ignored CONNECT_ACK from old connection");
            return;
        }

        ConnectAckFrame ack = new ConnectAckFrame();
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);
        ack.decode(data);

        String streamId = ack.getStreamId();
        log.info("[Connector] CONNECT_ACK: streamId={}, ok={}", streamId, ack.isOk());

        PendingConnect pending = pendingConnectRegistry.get(streamId);
        if (pending == null) {
            sendCloseToConnector(ctx.channel(), streamId);
            return;
        }

        if (!ack.isOk()) {
            pendingConnectRegistry.remove(streamId);
            // Gửi về đúng kênh Agent đã xin mở kết nối
            sendResourceFail(pending.getAgentChannel(), pending.getRequestId(), "Connector từ chối kết nối");
            return;
        }

        sessionLifecycleService.openSession(streamId);
    }

    private void handleData(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);

        DataFrame frame = new DataFrame();
        frame.decode(data);

        RelayResult result = relayEngine.forwardToAgent(frame, ctx.channel());

        if (result != RelayResult.SUCCESS) {
            log.warn("[Connector] DATA relay failed: streamId={}, result={}", frame.getStreamId(), result);

//            sessionLifecycleService.onRelayFailure(
//                    ctx.channel(),
//                    frame.getStreamId(),
//                    result
//            );
        }
    }

    private void handleClose(ChannelHandlerContext ctx, ByteBuf buf) {
        if (!isCurrentConnection(ctx.channel())) {
            return;
        }
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);
        CloseFrame frame = new CloseFrame();
        frame.decode(data);
        log.info("[Connector] CLOSE: streamId={}, reason={}", frame.getStreamId(), frame.getReasonText());
        sessionLifecycleService.closeSessionFrom(ctx.channel(), frame.getStreamId(), "Connector close");
    }


    private boolean isCurrentConnection(Channel channel) {
        if (!registered || connectorId == null) {
            return false;
        }
        ConnectorConnection current = connectorConnectionRegistry.getByConnectorId(connectorId);
        return current != null && current.getChannel() == channel && current.getConnectionId().equals(connectionId);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (registered && connectorId != null && connectionId != null) {
            log.info("[Connector] Disconnected: connectorId={}, connectionId={}", connectorId, connectionId);
            ConnectorConnection current = connectorConnectionRegistry.getByConnectorId(connectorId);
            // current == null: admin đã tắt/xoá connector (registry đã gỡ trước khi kênh đóng).
            // current là kênh này: mất kết nối bình thường. Cả hai trường hợp đều phải đóng phiên.
            // current là kênh khác: connector đã nối lại bằng kết nối mới, không đụng phiên của kết nối mới.
            if (current == null || current.getChannel() == ctx.channel()) {
                if (current != null) {
                    connectorConnectionRegistry.remove(current.getConnectionId());
                }
                sessionLifecycleService.onConnectorDisconnect(connectorId);
            }
        }
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause
    ) {

        log.error("[Connector] Connection error", cause);
        ctx.close();
    }

    private void sendCloseToConnector(Channel connectorChannel, String streamId) {
        if (connectorChannel == null || !connectorChannel.isActive()) return;
        try {
            CloseFrame close = new CloseFrame(streamId, CloseFrame.REASON_APP_CLOSED);
            connectorChannel.writeAndFlush(new BinaryWebSocketFrame(
                    Unpooled.wrappedBuffer(close.encode())
            )).addListener(f -> {
                if (!f.isSuccess()) {
                    log.warn("[Connector] Cannot send CLOSE for streamId={}: {}",
                            streamId, f.cause().getMessage());
                }
            });
        } catch (Exception e) {
            log.warn("[Connector] Error building CLOSE for streamId={}: {}", streamId, e.getMessage());
        }
    }

    private void sendResourceFail(Channel agentChannel, String requestId, String reason) {
        if (agentChannel == null || !agentChannel.isActive()) return;
        try {
            // Phải là RESOURCE_RESPONSE (0x06): Agent đang chờ đúng loại frame này để biết yêu cầu bị từ chối.
            // Trước đây gửi nhầm RESOURCE_ACCESS (0x05) nên Agent bỏ qua và người dùng bị treo.
            ResourceResFrame frame = ResourceResFrame.failed(requestId, ResourceResFrame.STATUS_ERROR, reason);
            agentChannel.writeAndFlush(new BinaryWebSocketFrame(
                    Unpooled.wrappedBuffer(frame.encode())
            )).addListener(f -> {
                if (!f.isSuccess()) {
                    log.warn("[Connector] Cannot send FAIL to agent: reqId={}, err={}",
                            requestId, f.cause().getMessage());
                }
            });
        } catch (Exception e) {
            log.warn("[Connector] Error building FAIL: reqId={}, err={}", requestId, e.getMessage());
        }
    }
}
