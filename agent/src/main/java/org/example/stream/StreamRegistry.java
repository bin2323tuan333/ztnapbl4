package org.example.stream;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StreamRegistry {
    private final Map<String, Channel> streams = new ConcurrentHashMap<>();
    private final Queue<Channel> channelQueue = new ConcurrentLinkedQueue<>();

    public void register(String streamId, Channel channel) {
        streams.put(streamId, channel);
        System.out.println("[Registry] active+ streamId=" + streamId
                + " (total=" + streams.size() + ")");
    }

    public Channel get(String streamId) {
        return streams.get(streamId);
    }

    public Channel remove(String streamId) {
        Channel ch = streams.remove(streamId);
        System.out.println("[Registry] active- streamId=" + streamId
                + " (remaining=" + streams.size() + ")");
        return ch;
    }

    public Channel pop() {
        Channel ch = channelQueue.poll();
        System.out.println("[Registry] pending- (remaining=" + channelQueue.size() + ")");
        return ch;
    }

    public void push(Channel channel) {
        channelQueue.add(channel);
        System.out.println("[Registry] pending+ (total=" + channelQueue.size() + ")");
    }

    public boolean removeQueued(Channel channel) {
        boolean removed = channelQueue.remove(channel);
        if (removed) {
            System.out.println("[Registry] pending- (removed dead, remaining="
                    + channelQueue.size() + ")");
        }
        return removed;
    }

    public void closeAll() {
        System.out.println("[Registry] closeAll: active=" + streams.size()
                + " pending=" + channelQueue.size());
        streams.values().forEach(Channel::close);
        channelQueue.forEach(Channel::close);
        streams.clear();
        channelQueue.clear();
    }
}