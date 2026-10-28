package org.example.cache;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PendingHttpRequest {
    private static final PendingHttpRequest INSTANCE = new PendingHttpRequest();

    private final Map<String, Channel> channels = new ConcurrentHashMap<>();
    private final Map<String, String> types = new ConcurrentHashMap<>();
    private final Map<String, String> resources = new ConcurrentHashMap<>();

    private PendingHttpRequest() {
    }

    public static PendingHttpRequest getInstance() {
        return INSTANCE;
    }

    public void put(String requestId, Channel channel, String type, String resourceId) {
        channels.put(requestId, channel);
        types.put(requestId, type != null ? type.toLowerCase() : "web");
        resources.put(requestId, resourceId);
    }

    public Channel remove(String requestId) {
        Channel channel = channels.remove(requestId);
        types.remove(requestId);
        resources.remove(requestId);

        if (channel != null) {
            System.out.println("[Cache] Đã lấy ra và xóa đường truyền cho Request ID: " + requestId + " | Còn lại: " + channels.size());
        } else {
            System.out.println("[Cache] ⚠ Không tìm thấy đường truyền cho Request ID: " + requestId);
        }
        return channel;
    }

    public Channel getChannel(String requestId) {
        return channels.get(requestId);
    }

    public String getTypes(String requestId) {
        return types.get(requestId);
    }

    public String getResource(String requestId) {
        return resources.get(requestId);
    }

    public boolean contains(String requestId) {
        return channels.containsKey(requestId);
    }

    public int size() {
        return channels.size();
    }

    public void clear() {
        channels.clear();
        types.clear();
        resources.clear();
        System.out.println("[Cache] Đã xóa toàn bộ cache.");
    }
}