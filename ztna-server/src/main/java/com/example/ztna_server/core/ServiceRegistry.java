package com.example.ztna_server.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ServiceRegistry {
    private final Map<String, ConnectorSession> connectors = new ConcurrentHashMap<>();

    public void register(ConnectorSession session) {
        for (String svc : session.getServices()) {
            ConnectorSession old = connectors.put(svc, session);
            if (old != null && old != session) {
                log.warn("Service '{}' overridden: {} -> {}", svc, old.getId(), session.getId());
            }

        }
        log.info("Registered connector {} with services {}", session.getId(), session.getServices());
    }

    public void unregister(ConnectorSession session) {
        for (String svc : session.getServices()) {
            connectors.remove(svc, session);
        }
        log.info("Unregistered connector {}", session.getId());
    }

    public Optional<ConnectorSession> findConnectorByService(String service) {
        return Optional.ofNullable(connectors.get(service));
    }

    public ConnectorSession get(String service) {
        return connectors.get(service);
    }

    public boolean isOnline(String service) {
        return connectors.containsKey(service);
    }

    public int size() {
        return connectors.size();
    }
}
