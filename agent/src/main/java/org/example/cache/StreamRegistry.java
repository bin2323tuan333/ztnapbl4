package org.example.cache;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.*;

public class StreamRegistry {
    private final Map<String, Channel> streams = new ConcurrentHashMap<>();


    public void register(String streamId, Channel channel) {
        streams.put(streamId, channel);
    }

    public Channel get(String streamId) {
        return streams.get(streamId);
    }

    public Channel remove(String streamId) {
        return streams.remove(streamId);
    }


    public void closeAll() {
        streams.values().forEach(Channel::close);
        streams.clear();
    }
}