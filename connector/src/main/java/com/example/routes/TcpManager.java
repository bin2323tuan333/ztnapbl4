package com.example.routes;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpManager {
    private static TcpManager instance = new TcpManager();
    Map<Integer, Channel> sessions = new ConcurrentHashMap<>();

    private TcpManager() {

    }

    public static TcpManager getInstance() {
        return  instance;
    }

    public Channel getChannel(int sessionId) {
        return sessions.get(sessionId);
    }

    public void add(int streamId, Channel channel) {
        sessions.put(streamId, channel);
    }

    public void remove(int streamId) {
        sessions.remove(streamId);
    }

    public Map<Integer, Channel> getSessions() {
        return sessions;
    }
}
