package com.example.routes;

import com.example.ws.ConnectorClient;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpManager {
    private static TcpManager instance = new TcpManager();
    Map<String, Channel> sessions = new ConcurrentHashMap<>();

    private TcpManager() {

    }

    public static TcpManager getInstance() {
        return  instance;
    }

    public Channel getChannel(String streamId) {
        return sessions.get(streamId);
    }

    public void add(String streamId, Channel channel) {
        sessions.put(streamId, channel);
    }

    public void remove(String streamId, byte reason) {
        sessions.remove(streamId);
        ConnectorClient connectorClient = ConnectorClient.getInstance();
        connectorClient.sendClose(streamId, reason);
    }

    public Map<String, Channel> getSessions() {
        return sessions;
    }

}
