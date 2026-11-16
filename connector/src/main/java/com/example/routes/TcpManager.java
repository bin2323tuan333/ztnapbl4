package com.example.routes;

import com.example.ws.ConnectorClient;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpManager {
    private static TcpManager instance = new TcpManager();

    // Map cũ: Lưu channel TCP đang active
    Map<String, Channel> sessions = new ConcurrentHashMap<>();

    // MỚI: Map lưu streamId -> resourceId (appId)
    Map<String, String> streamToResource = new ConcurrentHashMap<>();

    private TcpManager() {
    }

    public static TcpManager getInstance() {
        return instance;
    }

    public Channel getChannel(String streamId) {
        return sessions.get(streamId);
    }

    public String getResourceId(String streamId) {
        return streamToResource.get(streamId);
    }

    public void add(String streamId, String resourceId, Channel channel) {
        sessions.put(streamId, channel);
        if (resourceId != null) {
            streamToResource.put(streamId, resourceId);
        }
    }

    public void remove(String streamId, byte reason) {
        sessions.remove(streamId);
        streamToResource.remove(streamId);

//        ConnectorClient connectorClient = ConnectorClient.getInstance();
//        connectorClient.sendClose(streamId, reason);
    }

    public Map<String, Channel> getSessions() {
        return sessions;
    }
}