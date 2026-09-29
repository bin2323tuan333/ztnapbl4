package com.example.ztna_server.core;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class UserManager {

    private final Map<String, UserSession> byUserId = new ConcurrentHashMap<>();
    private final Map<Channel, UserSession> byChannel = new ConcurrentHashMap<>();

    public void put(UserSession session) {
        byUserId.put(session.getUserId(), session);
        byChannel.put(session.getChannel(), session);
        log.info("User added: {}", session.getUserId());
    }

    public UserSession getByUserId(String userId) {
        return byUserId.get(userId);
    }

    public UserSession getByChannel(Channel channel) {
        return byChannel.get(channel);
    }

    public UserSession remove(String userId) {
        UserSession session = byUserId.remove(userId);
        if (session != null) {
            byChannel.remove(session.getChannel());
            log.info("User removed: {}", userId);
        }
        return session;
    }

    public UserSession removeByChannel(Channel channel) {
        UserSession session = byChannel.remove(channel);
        if (session != null) {
            byUserId.remove(session.getUserId());
            log.info("User removed: {}", session.getUserId());
        }
        return session;
    }

    public boolean contains(String userId) {
        return byUserId.containsKey(userId);
    }

    public boolean containsChannel(Channel channel) {
        return byChannel.containsKey(channel);
    }

    public int size() {
        return byUserId.size();
    }

    public void clear() {
        int size = byUserId.size();
        byUserId.clear();
        byChannel.clear();
        log.info("UserManager cleared: {} users removed", size);
    }

    public void register(String userId, Channel channel) {
        UserSession session = new UserSession(userId, channel, Instant.now());
        put(session);

        log.info("User registered: {} from {}",
                userId,
                channel.remoteAddress());
    }

    public void unregister(Channel channel) {
        UserSession session = removeByChannel(channel);
        if (session != null) {
            log.info("User unregistered: {}", session.getUserId());
        }
    }

    public List<String> getAllUserIds() {
        return new ArrayList<>(byUserId.keySet());
    }

    public List<UserSession> getAllSessions() {
        return new ArrayList<>(byUserId.values());
    }

    public List<String> getUserIdsByIp(String ip) {
        List<String> ids = new ArrayList<>();

        for (Map.Entry<String, UserSession> entry : byUserId.entrySet()) {
            String remote = entry.getValue().getChannel().remoteAddress().toString();
            if (remote.contains(ip)) {
                ids.add(entry.getKey());
            }
        }

        return ids;
    }

    public long getOnlineSeconds(String userId) {
        UserSession session = byUserId.get(userId);
        if (session == null) return 0;

        return Duration.between(session.getConnectedAt(), Instant.now()).getSeconds();
    }

    public void closeAll() {
        Iterator<Map.Entry<Channel, UserSession>> iterator =
                byChannel.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Channel, UserSession> entry = iterator.next();
            Channel channel = entry.getKey();

            if (channel.isActive()) {
                channel.close();
            }

            iterator.remove();
            log.info("Closing user: {}", entry.getValue().getUserId());
        }

        byUserId.clear();
    }

    public void closeUser(String userId) {
        UserSession session = byUserId.get(userId);
        if (session == null) {
            log.warn("Cannot close: user {} not found", userId);
            return;
        }

        Channel channel = session.getChannel();
        if (channel.isActive()) {
            channel.close();
        }

        remove(userId);
        log.info("Closed user: {}", userId);
    }
}