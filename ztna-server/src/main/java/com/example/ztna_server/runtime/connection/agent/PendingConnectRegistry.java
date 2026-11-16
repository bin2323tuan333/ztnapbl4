package com.example.ztna_server.runtime.connection.agent;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PendingConnectRegistry {

    private final Map<String, PendingConnect> byStreamId    = new HashMap<>();
    private final Map<String, Set<String>>    byUserId      = new HashMap<>();
    private final Map<String, Set<String>>    byConnectorId = new HashMap<>();

    // ---------------- add ----------------

    public PendingConnect add(PendingConnect pending) {
        if (pending == null) throw new IllegalArgumentException("pending is null");

        synchronized (this) {
            String streamId = pending.getStreamId();
            if (byStreamId.containsKey(streamId)) {
                throw new IllegalStateException("Pending already exists: " + streamId);
            }
            byStreamId.put(streamId, pending);
            byUserId     .computeIfAbsent(pending.getUserId(),      k -> new HashSet<>()).add(streamId);
            byConnectorId.computeIfAbsent(pending.getConnectorId(), k -> new HashSet<>()).add(streamId);
        }
        return pending;
    }


    public synchronized PendingConnect get(String streamId) {
        return byStreamId.get(streamId);
    }

    public synchronized boolean contains(String streamId) {
        return byStreamId.containsKey(streamId);
    }

    public synchronized int size() {
        return byStreamId.size();
    }

    public synchronized List<PendingConnect> getAll() {
        return List.copyOf(byStreamId.values());
    }

    public synchronized List<PendingConnect> getByUserId(String userId) {
        Set<String> ids = byUserId.get(userId);
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(byStreamId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public synchronized List<PendingConnect> getByConnectorId(String connectorId) {
        Set<String> ids = byConnectorId.get(connectorId);
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(byStreamId::get)
                .filter(Objects::nonNull)
                .toList();
    }


    public PendingConnect remove(String streamId) {
        synchronized (this) {
            PendingConnect removed = byStreamId.remove(streamId);
            if (removed == null) return null;
            removeFromIndex(byUserId,      removed.getUserId(),      streamId);
            removeFromIndex(byConnectorId, removed.getConnectorId(), streamId);
            return removed;
        }
    }

    public List<PendingConnect> removeByUserId(String userId) {
        List<String> snapshot;
        synchronized (this) {
            Set<String> ids = byUserId.get(userId);
            snapshot = (ids == null) ? List.of() : List.copyOf(ids);
        }
        List<PendingConnect> removed = new ArrayList<>(snapshot.size());
        for (String streamId : snapshot) {
            PendingConnect p = remove(streamId);
            if (p != null) removed.add(p);
        }
        return removed;
    }

    public List<PendingConnect> removeByConnectorId(String connectorId) {
        List<String> snapshot;
        synchronized (this) {
            Set<String> ids = byConnectorId.get(connectorId);
            snapshot = (ids == null) ? List.of() : List.copyOf(ids);
        }
        List<PendingConnect> removed = new ArrayList<>(snapshot.size());
        for (String streamId : snapshot) {
            PendingConnect p = remove(streamId);
            if (p != null) removed.add(p);
        }
        return removed;
    }

    private static void removeFromIndex(Map<String, Set<String>> index, String key, String streamId) {
        Set<String> ids = index.get(key);
        if (ids == null) return;
        ids.remove(streamId);
        if (ids.isEmpty()) index.remove(key);
    }

    @PreDestroy
    public void shutdown() {
        synchronized (this) {
            if (!byStreamId.isEmpty()) {
                log.warn("[PendingRegistry] Shutdown with {} pending connects not acked", byStreamId.size());
            }
            byStreamId.clear();
            byUserId.clear();
            byConnectorId.clear();
        }
    }
}