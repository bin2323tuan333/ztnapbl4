package com.example.ztna_server.service;

import com.example.ztna_server.routes.ConnectionRegister;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConnectionLifecycleService {
    private final ConnectionRegister connectionRegister;
    public ConnectionLifecycleService(ConnectionRegister connectionRegister) {
        this.connectionRegister = connectionRegister;
    }

    public void disconnect(String connectorId) {
        Channel channel = connectionRegister.getChannel(connectorId);
        connectionRegister.remove(connectorId);
        if (channel == null) return;
        channel.close();

    }

    public void register(String connectorId, Channel channel) {
        if (channel == null || !channel.isActive()) {
            log.warn("[ConnLifecycle] register: channel null/inactive for {}", connectorId);
            return;
        }

        Channel old = connectionRegister.getChannel(connectorId);
        if (old != null && old != channel && old.isActive()) {
            log.warn("[ConnLifecycle] {} reconnect — đóng channel cũ", connectorId);
            old.close();
        }

        connectionRegister.put(connectorId, channel);
        connectionRegister.markAlive(connectorId);
        log.info("[ConnLifecycle] Registered {}", connectorId);
    }

    public Channel getChannel(String connectorId) {
        return connectionRegister.getChannel(connectorId);
    }

    public void markAlive(String connectorId) {
        connectionRegister.markAlive(connectorId);
    }

    public boolean isOnline(String connectorId) {
        return connectionRegister.isOnline(connectorId);
    }

}
