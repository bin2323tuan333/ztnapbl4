package org.example.cache;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientOpenRegistry {
    // reqId -> Channel
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    public void put(String reqId, Channel channel) {
        channels.put(reqId, channel);
    }

    public Channel remove(String reqId) {
        return channels.remove(reqId);
    }

    public Channel get(String reqId) {
        return channels.get(reqId);
    }

    public int size() {
        return channels.size();
    }
}