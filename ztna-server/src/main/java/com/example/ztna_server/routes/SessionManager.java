package com.example.ztna_server.routes;

import com.example.ztna_server.protocol.frames.CloseFrame;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class SessionManager {
    // streamId → Session
    private final Map<Integer, Session> sessions = new ConcurrentHashMap<>();
    // connectorId -> streamIds
    private final Map<String, Set<Integer>> byConnector  = new ConcurrentHashMap<>();
    // userId -> StreamIds
    private final Map<String, Set<Integer>> byUser = new ConcurrentHashMap<>();

    // Sinh streamId
    private final AtomicInteger idGen = new AtomicInteger(1000);


    public int nextStreamId() {
        return idGen.getAndIncrement();
    }




    public void add(Integer streamId, Session session) {
        sessions.put(streamId, session);
        Set<Integer> set = byConnector.get(session.getConnectorId());
        if (set == null) {
            set = ConcurrentHashMap.newKeySet();
            byConnector.put(session.getConnectorId(), set);
        }
        set.add(streamId);

        set = byUser.get(session.getUserId());
        if (set == null) {
            set = ConcurrentHashMap.newKeySet();
            byUser.put(session.getUserId(), set);
        }
        set.add(streamId);
    }

    public Session remove(Integer streamId) {
        Session s = sessions.remove(streamId);
        if (s == null) {
            return null;
        }

        removeFromIndex(byConnector, s.getConnectorId(), streamId);
        removeFromIndex(byUser, s.getUserId(), streamId);
        return s;
    }

    private void removeFromIndex(Map<String, Set<Integer>> index, String key, int streamId) {
        if (key == null) return;
        Set<Integer> set = index.get(key);
        if (set != null) {
            set.remove(streamId);
            if (set.isEmpty()) index.remove(key);
        }
    }

    public Session get(Integer streamId) {
        return sessions.get(streamId);
    }

    public List<Session> getByConnector(String connectorId) {
        Set<Integer> ids = byConnector.get(connectorId);
        if (ids == null) return List.of();
        return ids.stream()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Session> getByUser(String userId) {
        Set<Integer> ids = byUser.get(userId);
        if (ids == null) return List.of();
        return ids.stream()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public int size() {
        return sessions.size();
    }
}
