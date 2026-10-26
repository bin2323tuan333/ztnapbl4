package com.example.ztna_server.service;

import com.example.ztna_server.dto.App;
import com.example.ztna_server.protocol.frames.CloseFrame;
import com.example.ztna_server.protocol.frames.ConnectFrame;
import com.example.ztna_server.protocol.frames.DataFrame;
import com.example.ztna_server.routes.ConnectionRegister;
import com.example.ztna_server.routes.RoutingTable;
import com.example.ztna_server.routes.Session;
import com.example.ztna_server.routes.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SessionLifecycleService {
    private final SessionManager sessionManager;
    private final ConnectionRegister connectionRegister;
    private final RoutingTable routingTable;

    public SessionLifecycleService(SessionManager sessionManager, ConnectionRegister connectionRegister, RoutingTable routingTable) {
        this.sessionManager = sessionManager;
        this.connectionRegister = connectionRegister;
        this.routingTable = routingTable;
    }

    public Session openSession(String userId, String appId, Channel clientChannel) {
        var route = routingTable.getAppRoute(appId);
        if (route == null) {
            log.warn("[Lifecycle] App không tồn tại: {}", appId);
            return null;
        }

        String connectorId = route.getConnectorId();
        App app = route.getApp();

        Channel connCh = connectionRegister.getChannel(connectorId);
        if (connCh == null || !connCh.isActive()) {
            log.warn("[Lifecycle] Connector {} offline", connectorId);
            return null;
        }

        // Sinh streamId dạng String UUID
        String streamId = UUID.randomUUID().toString();

        Session session = Session.builder()
                .streamId(streamId)
                .userId(userId)
                .appId(appId)
                .connectorId(connectorId)
                .userChannel(clientChannel)
                .wsChannel(connCh)
                .state(Session.State.INIT)
                .openedAt(System.currentTimeMillis())
                .build();

        sessionManager.add(streamId, session);

        // Gửi lệnh CONNECT xuống Agent (Connector)
        ConnectFrame cf = new ConnectFrame(streamId, appId);
        connCh.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(cf.encode())
        ));

        session.setState(Session.State.OPENING);

        log.info("[Lifecycle] OPEN stream={} user={} app={} conn={}", streamId, userId, appId, connectorId);

        return session;
    }

    public void sessionDisconnect(String streamId, String reason) {
        Session session = sessionManager.remove(streamId);
        if (session == null) {
            return;
        }
        session.setCloseReason(reason);
        log.info("Close session: {}", reason);
        session.setState(Session.State.CLOSING);
        closeClient(session);
        notifyConnector(session);
        session.setState(Session.State.CLOSED);
    }

    private void closeClient(Session session) {
        Channel userCh = session.getUserChannel();
        if (userCh == null || !userCh.isActive()) return;

        CloseFrame cf = new CloseFrame();
        cf.setStreamId(session.getStreamId()); // Đã đổi thành String

        userCh.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(cf.encode())
        )).addListener(ChannelFutureListener.CLOSE);
    }

    private void notifyConnector(Session session) {
        Channel connCh = session.getWsChannel();
        if (connCh == null || !connCh.isActive()) return;

        CloseFrame cf = new CloseFrame();
        cf.setStreamId(session.getStreamId()); // Đã đổi thành String

        connCh.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(cf.encode())
        ));
    }

    public void forwardToConnector(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        DataFrame frame = new DataFrame();
        frame.decode(data);

        String streamId = frame.getStreamId(); // String
        byte[] payload = frame.getData();

        if (payload == null || payload.length == 0) {
            log.debug("[Lifecycle] DATA rỗng cho stream={}", streamId);
            return;
        }

        Session session = sessionManager.get(streamId);
        if (session == null || !(session.getState() == Session.State.READY)) {
            log.warn("[Lifecycle] Stream {} không tồn tại hoặc chưa READY", streamId);
            return;
        }

        Channel conChannel = session.getWsChannel();
        if (conChannel == null || !conChannel.isActive()) {
            sessionDisconnect(streamId, "CONNECTOR CONNECT NULL");
            log.info("Channel null");
            return;
        }

        conChannel.writeAndFlush(
                new BinaryWebSocketFrame(
                        Unpooled.wrappedBuffer(frame.encode())
                )
        );

        log.debug("[Lifecycle] Server → App: stream={}, {} bytes", streamId, payload.length);
    }

    public void forwardToClient(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        DataFrame frame = new DataFrame();
        frame.decode(data);

        String streamId = frame.getStreamId(); // SỬA: Đổi từ int sang String
        byte[] payload = frame.getData();      // SỬA: Đổi từ getPayload() sang getData()

        if (payload == null || payload.length == 0) {
            log.debug("[Lifecycle] DATA rỗng cho stream={}", streamId);
            return;
        }

        Session session = sessionManager.get(streamId);
        if (session == null || !(session.getState() == Session.State.READY)) {
            log.warn("[Lifecycle] Stream {} không tồn tại hoặc chưa READY", streamId);
            return;
        }

        Channel clientCh = session.getUserChannel();
        if (clientCh == null || !clientCh.isActive()) {
            sessionDisconnect(streamId, "USER CONNECT NULL");
            return;
        }

        clientCh.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(frame.encode())
        ));

        log.debug("[Lifecycle] Server → Client: stream={}, {} bytes", streamId, payload.length);
    }

    public void onClientDisconnect(String userId) {
        List<Session> list = sessionManager.getByUser(userId);
        log.info("[Lifecycle] Client {} disconnected, closing {} sessions", userId, list.size());

        for (Session item : list) {
            sessionDisconnect(item.getStreamId(), "CLIENT DISCONNECT");
        }
    }

    public void onConnectorDisconnect(String connectorId) {
        List<Session> list = sessionManager.getByConnector(connectorId);
        log.info("[Lifecycle] Connector {} disconnected, closing {} sessions",connectorId, list.size());

        for (Session item : list) {
            sessionDisconnect(item.getStreamId(), "CONNECTOR DISCONNECT");
        }
    }

    public void onConnectAckOk(String streamId) {
        Session session = sessionManager.get(streamId);
        if (session == null) {
            log.warn("[Lifecycle] CONNECT_ACK cho stream không tồn tại: {}", streamId);
            return;
        }

        session.setState(Session.State.READY);
        session.touch();

        log.info("[Lifecycle] Stream {} READY", streamId);
    }

    // SỬA: Đổi tham số int thành String
    public void onConnectAckFailed(String streamId, String reason) {
        sessionDisconnect(streamId, "CONNECT_FAILED: " + reason);
    }
}