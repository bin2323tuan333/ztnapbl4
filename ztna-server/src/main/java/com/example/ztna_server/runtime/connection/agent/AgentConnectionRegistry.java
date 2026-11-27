package com.example.ztna_server.runtime.connection.agent;

import io.netty.channel.Channel;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AgentConnectionRegistry {
    private final Map<String, AgentConnection> byConnectionId = new HashMap<>();
    private final Map<String, AgentConnection> byUserId = new HashMap<>();

    public AgentConnection register(String userId, Channel channel) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId is required");
        if (channel == null || !channel.isActive())
            throw new IllegalArgumentException("Channel is not active");

        AgentConnection old;
        AgentConnection conn;
        synchronized (this) {
            old = byUserId.get(userId);
            if (old != null) byConnectionId.remove(old.getConnectionId());

            String connectionId = UUID.randomUUID().toString();
            conn = new AgentConnection(connectionId, userId, channel);
            byConnectionId.put(connectionId, conn);
            byUserId.put(userId, conn);
        }
        if (old != null) old.getChannel().close();
        return conn;
    }

    public void remove(String connectionId) {
        AgentConnection removed;
        synchronized (this) {
            removed = byConnectionId.remove(connectionId);
            if (removed == null) return;
            if (byUserId.get(removed.getUserId()) == removed) {
                byUserId.remove(removed.getUserId());
            }
        }
        removed.getChannel().close();
    }

    public void removeByUserId(String userId) {
        AgentConnection c;
        synchronized (this) {
            c = byUserId.get(userId);
        }
        if (c != null) remove(c.getConnectionId());
    }

    public AgentConnection getByConnectionId(String connectionId) {
        return byConnectionId.get(connectionId);
    }

    public AgentConnection getByUserId(String userId) {
        return byUserId.get(userId);
    }

    public boolean containsConnection(String connectionId) {
        return byConnectionId.containsKey(connectionId);
    }

    public boolean containsUser(String userId) {
        return byUserId.containsKey(userId);
    }

    public boolean isOnline(String userId) {
        AgentConnection connection = byUserId.get(userId);
        return connection != null && connection.isActive();
    }

    public int size() {
        return byConnectionId.size();
    }

    public List<AgentConnection> getAll() {
        return List.copyOf(byConnectionId.values());
    }

    @PreDestroy
    public void shutdown() {
        List<AgentConnection> snapshot;
        synchronized (this) {
            snapshot = List.copyOf(byConnectionId.values());
            byConnectionId.clear();
            byUserId.clear();
        }
        for (AgentConnection c : snapshot) {
            c.getChannel().close();
        }
    }
}
