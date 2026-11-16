package com.example.ztna_server.runtime.routing;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class RoutingRegistry {
    private Map<String, RouteTarget> byResourceId = new HashMap<>();
    private Map<String, List<RouteTarget>> byConnectorId = new HashMap<>();

    public synchronized RouteTarget register(RouteTarget target) {
        if (target == null) {
            throw new IllegalArgumentException("RouteTarget is null");
        }

        RouteTarget old = byResourceId.put(target.resourceId(), target);
        if (old != null) {
            removeFromConnectorIndex(old.connectorId(), old.resourceId());
        }
        byConnectorId
                .computeIfAbsent(target.connectorId(), k -> new ArrayList<>())
                .add(target);

        if (old != null) {
            log.info("Route replaced: {} (oldConnector={}, newConnector={})",
                    target.resourceId(), old.connectorId(), target.connectorId());
        }
        return old;
    }

    public synchronized void replaceAll(Map<String, RouteTarget> targets) {
        if (targets == null) targets = Map.of();

        List<String> toRemove = new ArrayList<>();
        for (String id : byResourceId.keySet()) {
            if (!targets.containsKey(id)) toRemove.add(id);
        }
        for (String id : toRemove) {
            RouteTarget removed = byResourceId.remove(id);
            if (removed != null) removeFromConnectorIndex(removed.connectorId(), id);
        }

        for (RouteTarget t : targets.values()) {
            RouteTarget old = byResourceId.get(t.resourceId());
            if (old == null) {
                byResourceId.put(t.resourceId(), t);
                byConnectorId.computeIfAbsent(t.connectorId(), k -> new ArrayList<>()).add(t);
            } else if (!sameTarget(old, t)) {
                byResourceId.put(t.resourceId(), t);
                removeFromConnectorIndex(old.connectorId(), t.resourceId());
                byConnectorId.computeIfAbsent(t.connectorId(), k -> new ArrayList<>()).add(t);
            }
        }
    }

    private static boolean sameTarget(RouteTarget a, RouteTarget b) {
        return a.resourceId().equals(b.resourceId())
                && a.connectorId().equals(b.connectorId())
                && a.host().equals(b.host())
                && a.port() == b.port();
    }


    public synchronized List<RouteTarget> registerAll(List<RouteTarget> targets) {
        if (targets == null || targets.isEmpty()) return List.of();
        List<RouteTarget> replaced = new ArrayList<>();
        for (RouteTarget t : targets) {
            RouteTarget old = register(t);
            if (old != null) replaced.add(old);
        }
        return List.copyOf(replaced);
    }

    public synchronized RouteTarget unregister(String resourceId) {
        RouteTarget removed = byResourceId.remove(resourceId);
        if (removed == null) return null;
        removeFromConnectorIndex(removed.connectorId(), removed.resourceId());
        return removed;
    }


    public synchronized List<RouteTarget> unregisterByConnectorId(String connectorId) {
        List<RouteTarget> removed = byConnectorId.remove(connectorId);
        if (removed == null || removed.isEmpty()) return List.of();

        List<RouteTarget> snapshot = List.copyOf(removed);
        for (RouteTarget t : snapshot) {
            byResourceId.remove(t.resourceId(), t);
        }
        log.info("Unregistered {} resources of connector {}", snapshot.size(), connectorId);
        return snapshot;
    }


    public synchronized RouteTarget getByResourceId(String resourceId) {
        return byResourceId.get(resourceId);
    }

    public synchronized List<RouteTarget> getByConnectorId(String connectorId) {
        List<RouteTarget> list = byConnectorId.get(connectorId);
        return list == null ? List.of() : List.copyOf(list);
    }

    public synchronized boolean contains(String resourceId) {
        return byResourceId.containsKey(resourceId);
    }

    public synchronized boolean containsConnector(String connectorId) {
        List<RouteTarget> list = byConnectorId.get(connectorId);
        return list != null && !list.isEmpty();
    }

    public synchronized int size() {
        return byResourceId.size();
    }

    public synchronized List<RouteTarget> getAll() {
        return List.copyOf(byResourceId.values());
    }


    private synchronized void removeFromConnectorIndex(String connectorId, String resourceId) {
        List<RouteTarget> list = byConnectorId.get(connectorId);
        if (list == null) return;
        list.removeIf(t -> t.resourceId().equals(resourceId));
        if (list.isEmpty()) byConnectorId.remove(connectorId);
    }


    @PreDestroy
    public synchronized void clear() {
        byResourceId.clear();
        byConnectorId.clear();
    }
}
