package com.example.ztna_server.routes;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    // ===== Định danh =====
    private String streamId;
    private String userId;
    private String appId;
    private String appName;
    private String connectorId;
    private String connectorName;

    private Channel wsChannel;
    private Channel userChannel;

    private State state;
    private String closeReason;

    private long openedAt;
    private long lastActive;

    public enum State {
        INIT,
        OPENING,
        READY,
        CLOSING,
        CLOSED
    }

    public void touch() {
        this.lastActive = System.currentTimeMillis();
    }

    public long getDurationMs() {
        return System.currentTimeMillis() - openedAt;
    }
}