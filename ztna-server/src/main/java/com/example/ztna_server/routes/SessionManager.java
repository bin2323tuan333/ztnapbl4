package com.example.ztna_server.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {
    // streamId → Session
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    // connectorId -> streamIds
    private final Map<String, Set<String>> byConnector  = new ConcurrentHashMap<>();
    // userId -> StreamIds
    private final Map<String, Set<String>> byUser = new ConcurrentHashMap<>();
    // reqId -> Session
    private final Map<String, Session> reqSession = new ConcurrentHashMap<>();
    // streamId -> reqId
    private final Map<String, String> streamToReq = new ConcurrentHashMap<>();

    public String nextStreamId() {
        return UUID.randomUUID().toString();
    }

    public void add(String reqId, String streamId, Session session) {
        sessions.put(streamId, session);

        if (reqId != null && !reqId.isBlank()) {
            reqSession.put(reqId, session);
            streamToReq.put(streamId, reqId);
        }

        byConnector.computeIfAbsent(session.getConnectorId(), k -> ConcurrentHashMap.newKeySet())
                .add(streamId);
        byUser.computeIfAbsent(session.getUserId(), k -> ConcurrentHashMap.newKeySet())
                .add(streamId);
    }

    public Session remove(String streamId) {
        Session s = sessions.remove(streamId);
        if (s == null) return null;

        String reqId = streamToReq.remove(streamId);
        if (reqId != null) reqSession.remove(reqId);

        removeFromIndex(byConnector, s.getConnectorId(), streamId);
        removeFromIndex(byUser, s.getUserId(), streamId);
        return s;
    }

    public List<Session> removeByUser(String userId) {
        if (userId == null) return List.of();
        Set<String> ids = byUser.remove(userId);
        if (ids == null || ids.isEmpty()) return List.of();

        return ids.stream()
                .map(this::remove)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Session> removeByConnector(String connectorId) {
        if (connectorId == null) return List.of();
        Set<String> ids = byConnector.remove(connectorId);
        if (ids == null || ids.isEmpty()) return List.of();

        return ids.stream()
                .map(this::remove)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Session> removeByUserAndConnector(String userId, String connectorId) {
        if (userId == null || connectorId == null) return List.of();
        Set<String> userSet = byUser.get(userId);
        if (userSet == null || userSet.isEmpty()) return List.of();

        return userSet.stream()
                .filter(id -> {
                    Session s = sessions.get(id);
                    return s != null && connectorId.equals(s.getConnectorId());
                })
                .map(this::remove)
                .filter(Objects::nonNull)
                .toList();
    }

    private void removeFromIndex(Map<String, Set<String>> index, String key, String streamId) {
        if (key == null) return;
        Set<String> set = index.get(key);
        if (set != null) {
            set.remove(streamId);
            if (set.isEmpty()) index.remove(key);
        }
    }

    public Session get(String streamId) { return sessions.get(streamId); }
    public Session getByReqId(String reqId) { return reqSession.get(reqId); }
    public String getReqIdByStreamId(String streamId) { return streamToReq.get(streamId); }

    public List<Session> getByConnector(String connectorId) {
        Set<String> ids = byConnector.get(connectorId);
        if (ids == null) return List.of();
        return ids.stream().map(sessions::get).filter(Objects::nonNull).toList();
    }

    public List<Session> getByUser(String userId) {
        Set<String> ids = byUser.get(userId);
        if (ids == null) return List.of();
        return ids.stream().map(sessions::get).filter(Objects::nonNull).toList();
    }

    public int size() { return sessions.size(); }
}