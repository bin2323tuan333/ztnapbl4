package com.example.ztna_server.routes;


import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ConnectionRegister {
    // connectorId → Channel
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    // connectorId → lastSeen (heartbeat)
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();


    public void put(String connectorId, Channel channel) {
        channels.put(connectorId, channel);
        markAlive(connectorId);
    }

    public void remove(String connectorId) {
        Channel channel = channels.remove(connectorId);
        if (channel == null) {
            lastSeen.remove(connectorId);
        }
    }

    public Channel getChannel(String connectorId) {
        return channels.get(connectorId);
    }

    public boolean isOnline(String connectorId) {
        Channel ch = channels.get(connectorId);
        return ch != null && ch.isActive();
    }

    public void markAlive(String connectorId) {
        lastSeen.put(connectorId, System.currentTimeMillis());
    }

    public List<String> findTimeout(long timeoutMs) {
        long now = System.currentTimeMillis();
        return lastSeen.entrySet().stream()
                .filter(e -> now - e.getValue() > timeoutMs)
                .map(Map.Entry::getKey)
                .toList();
    }

    public int size() {
        return channels.size();
    }
}
