package com.example.tcp;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TcpSessionManager {
    private final Map<Integer, TcpSession> sessions = new ConcurrentHashMap<>();

    public void put(int streamId, TcpSession session) {
        sessions.put(streamId, session);
        log.info("Session added: stream={}, service={}",
                streamId, session.getService());
    }

    public TcpSession get(int streamId) {
        return sessions.get(streamId);
    }

    public TcpSession remove(int streamId) {
        TcpSession session = sessions.remove(streamId);
        if (session != null) {
            log.info("Session removed: stream={}", streamId);
        }
        return session;
    }

    public boolean contains(int streamId) {
        return sessions.containsKey(streamId);
    }

    public int size() {
        return sessions.size();
    }

    public void clear() {
        int size = sessions.size();
        sessions.clear();
        log.info("SessionManager cleared: {} sessions", size);
    }


    public void closeAll() {
        sessions.values().forEach(s -> {
            if (s.getTcpChannel().isOpen()) {
                    s.getTcpChannel().close();
            }
        });
        sessions.clear();
    }
}