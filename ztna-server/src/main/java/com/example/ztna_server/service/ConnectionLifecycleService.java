package com.example.ztna_server.service;

import com.example.ztna_server.enums.TargetType;
import com.example.ztna_server.routes.ConnecterRegister;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConnectionLifecycleService {
    private final ConnecterRegister connecterRegister;
    private final AuditService auditService;

    public ConnectionLifecycleService(ConnecterRegister connecterRegister, AuditService auditService) {
        this.connecterRegister = connecterRegister;
        this.auditService = auditService;
    }

    public void disconnect(String connectorId) {
        Channel channel = connecterRegister.getChannel(connectorId);
        connecterRegister.remove(connectorId);
        auditService.log(
                "CONNECTOR_OFFLINE",
                TargetType.CONNECTOR,
                connectorId,
                connectorId.toString() + " disconnected",
                null
        );
        if (channel == null) return;
        channel.close();
    }

    public void register(String connectorId, Channel channel) {
        if (channel == null || !channel.isActive()) {
            auditService.log(
                    "CONNECTOR_ONLINE",          
                    TargetType.CONNECTOR,
                    connectorId,
                    connectorId.toString() + " connected",
                    null
        );
            return;
        }

        Channel old = connecterRegister.getChannel(connectorId);
        if (old != null && old != channel && old.isActive()) {
            old.close();
        }

        connecterRegister.put(connectorId, channel);
        connecterRegister.markAlive(connectorId);
    }

    public Channel getChannel(String connectorId) {
        return connecterRegister.getChannel(connectorId);
    }

    public void markAlive(String connectorId) {
        connecterRegister.markAlive(connectorId);
    }

    public boolean isOnline(String connectorId) {
        return connecterRegister.isOnline(connectorId);
    }

}
