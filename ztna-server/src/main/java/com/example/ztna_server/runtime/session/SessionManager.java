package com.example.ztna_server.runtime.session;

import io.netty.channel.Channel;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SessionManager {

    private final Map<String, SessionContext> byStreamId    = new HashMap<>();
    private final Map<String, Set<String>>    byUserId      = new HashMap<>();
    private final Map<String, Set<String>>    byConnectorId = new HashMap<>();

    public SessionContext add(SessionContext session) {
        if (session == null) throw new IllegalArgumentException("session is null");

        synchronized (this) {
            String streamId = session.getStreamId();
            if (byStreamId.containsKey(streamId)) {
                throw new IllegalStateException("Session already exists: " + streamId);
            }
            byStreamId.put(streamId, session);
            byUserId.computeIfAbsent(session.getUserId(), k -> new HashSet<>()).add(streamId);
            byConnectorId.computeIfAbsent(session.getConnectorId(), k -> new HashSet<>()).add(streamId);
        }
        return session;
    }

    public synchronized SessionContext get(String streamId) {
        return byStreamId.get(streamId);
    }

    public synchronized boolean contains(String streamId) {
        return byStreamId.containsKey(streamId);
    }

    public synchronized int size() {
        return byStreamId.size();
    }

    public synchronized List<SessionContext> getAll() {
        return List.copyOf(byStreamId.values());
    }

    public synchronized List<SessionContext> getByUserId(String userId) {
        Set<String> ids = byUserId.get(userId);
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(byStreamId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public synchronized List<SessionContext> getByConnectorId(String connectorId) {
        Set<String> ids = byConnectorId.get(connectorId);
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(byStreamId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public synchronized boolean isUserActive(String userId) {
        Set<String> ids = byUserId.get(userId);
        if (ids == null || ids.isEmpty()) return false;
        for (String id : ids) {
            SessionContext s = byStreamId.get(id);
            if (s != null && s.isActive()) return true;
        }
        return false;
    }


    public int closeByUserId(String userId, String reason) {
        var closes = byUserId.get(userId);
        int closed = 0;
        for (String id : closes) {
            if (closeSession(id, reason)) closed++;
        }
        return closed;
    }

    public int closeByConnectorId(String connectorId, String reason) {
        var closes = byConnectorId.get(connectorId);
        int closed = 0;
        for (String id : closes) {
            if (closeSession(id, reason)) closed++;
        }
        return closed;
    }

    public boolean closeSession(String streamId, String reason) {
        SessionContext closed = byStreamId.get(streamId);
        byUserId.get(closed.getUserId()).remove(streamId);
        byConnectorId.get(closed.getConnectorId()).remove(streamId);
        byStreamId.remove(streamId);
        closed.close(SessionState.CLOSED, reason);
        return true;
    }

    public boolean failSession(String streamId, String reason) {
        SessionContext closed = byStreamId.get(streamId);
        byUserId.get(closed.getUserId()).remove(streamId);
        byConnectorId.get(closed.getConnectorId()).remove(streamId);
        byStreamId.remove(streamId);
        closed.close(SessionState.FAILED, reason);
        return true;
    }

    @PreDestroy
    public void shutdown() {
        List<SessionContext> snapshot;
        synchronized (this) {
            snapshot = List.copyOf(byStreamId.values());
            byStreamId.clear();
            byUserId.clear();
            byConnectorId.clear();
        }
        for (SessionContext s : snapshot) {
            s.close(SessionState.CLOSED, "server shutdown");
        }
        log.info("SessionManager shutdown, closed {} sessions", snapshot.size());
    }
}