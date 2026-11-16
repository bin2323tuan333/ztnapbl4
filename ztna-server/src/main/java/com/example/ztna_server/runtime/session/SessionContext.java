package com.example.ztna_server.runtime.session;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
public class SessionContext {

    private final String streamId;
    private final String userId;
    private final String connectorId;
    private final String resourceId;
    private final Channel agentChannel;
    private final Channel connectorChannel;

    private volatile SessionState state;
    private volatile String closeReason;

    public boolean isActive()     { return state == SessionState.ACTIVE; }
    public boolean isTerminated() { return state == SessionState.CLOSED || state == SessionState.FAILED; }

    public synchronized boolean close(SessionState finalState, String reason) {
        if (isTerminated()) return false;
        if (finalState != SessionState.CLOSED && finalState != SessionState.FAILED) {
            throw new IllegalArgumentException("finalState must be CLOSED or FAILED");
        }
        this.state       = finalState;
        this.closeReason = reason;
        return true;
    }

    public String       getStreamId()            { return streamId; }
    public String       getUserId()              { return userId; }
    public String       getConnectorId()         { return connectorId; }
    public String       getResourceId()          { return resourceId; }
    public Channel getAgentChannel()     { return agentChannel; }
    public Channel getConnectorChannel() { return connectorChannel; }
    public SessionState getState()               { return state; }
    public String       getCloseReason()         { return closeReason; }

    @Override
    public String toString() {
        return "SessionContext{" +
                "streamId='" + streamId + '\'' +
                ", userId='" + userId + '\'' +
                ", connectorId='" + connectorId + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", state=" + state +
                (closeReason != null ? ", closeReason='" + closeReason + '\'' : "") +
                '}';
    }
}