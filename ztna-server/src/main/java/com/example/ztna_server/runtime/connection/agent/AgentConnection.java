package com.example.ztna_server.runtime.connection.agent;

import io.netty.channel.Channel;

public class AgentConnection {
    private final String connectionId;
    private final String userId;
    private final Channel channel;

    public AgentConnection(String connectionId, String userId, Channel channel) {
        this.connectionId = connectionId;
        this.userId = userId;
        this.channel = channel;
    }

    public boolean isActive() {
        return this.channel.isActive();
    }

    public String getConnectionId() {
        return connectionId;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getUserId() {
        return userId;
    }
}
