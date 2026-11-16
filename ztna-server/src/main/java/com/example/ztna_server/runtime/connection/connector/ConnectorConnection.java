package com.example.ztna_server.runtime.connection.connector;

import io.netty.channel.Channel;

public final class ConnectorConnection {

    private final String connectionId;
    private final String connectorId;
    private final Channel channel;

    public ConnectorConnection(String connectionId, String connectorId, Channel channel) {
        this.connectionId = connectionId;
        this.connectorId = connectorId;
        this.channel = channel;

    }

    public boolean isActive() {
        return channel.isActive();
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public Channel getChannel() {
        return channel;
    }

}