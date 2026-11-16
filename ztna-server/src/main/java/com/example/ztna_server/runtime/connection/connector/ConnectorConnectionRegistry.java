package com.example.ztna_server.runtime.connection.connector;

import io.netty.channel.Channel;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConnectorConnectionRegistry {

    private final Map<String, ConnectorConnection> byConnectionId = new HashMap<>();
    private final Map<String, ConnectorConnection> byConnectorId  = new HashMap<>();

    public ConnectorConnection register(String connectorId, Channel channel) {
        if (connectorId == null || connectorId.isBlank()) {
            throw new IllegalArgumentException("connectorId is required");
        }
        if (channel == null || !channel.isActive()) {
            throw new IllegalArgumentException("Channel is not active");
        }

        ConnectorConnection old;
        ConnectorConnection connection;
        synchronized (this) {
            old = byConnectorId.get(connectorId);
            if (old != null) {
                byConnectionId.remove(old.getConnectionId());
            }

            String connectionId = UUID.randomUUID().toString();
            connection = new ConnectorConnection(connectionId, connectorId, channel);
            byConnectionId.put(connectionId, connection);
            byConnectorId.put(connectorId, connection);
        }

        if (old != null) {
            old.getChannel().close();
        }
        return connection;
    }

    public ConnectorConnection getByConnectionId(String connectionId) {
        return byConnectionId.get(connectionId);
    }

    public ConnectorConnection getByConnectorId(String connectorId) {
        return byConnectorId.get(connectorId);
    }

    public boolean containsConnection(String connectionId) {
        return byConnectionId.containsKey(connectionId);
    }

    public boolean containsConnector(String connectorId) {
        return byConnectorId.containsKey(connectorId);
    }

    public boolean isOnline(String connectorId) {
        ConnectorConnection c = byConnectorId.get(connectorId);
        return c != null && c.isActive();
    }

    public void remove(String connectionId) {
        ConnectorConnection removed;
        synchronized (this) {
            removed = byConnectionId.remove(connectionId);
            if (removed == null) return;
            if (byConnectorId.get(removed.getConnectorId()) == removed) {
                byConnectorId.remove(removed.getConnectorId());
            }
        }
        removed.getChannel().close();
    }

    public void removeByConnectorId(String connectorId) {
        ConnectorConnection c;
        synchronized (this) {
            c = byConnectorId.get(connectorId);
        }
        if (c != null) remove(c.getConnectionId());
    }

    public int size() {
        return byConnectionId.size();
    }

    public List<ConnectorConnection> getAll() {
        return List.copyOf(byConnectionId.values());
    }

    @PreDestroy
    public void shutdown() {
        List<ConnectorConnection> snapshot;
        synchronized (this) {
            snapshot = List.copyOf(byConnectionId.values());
            byConnectionId.clear();
            byConnectorId.clear();
        }
        for (ConnectorConnection c : snapshot) {
            c.getChannel().close();
        }
    }
}