package com.example.ztna_server.core;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class StreamManager {

    private final Map<Integer, StreamSession> streams = new ConcurrentHashMap<>();

    public void put(int streamId, StreamSession session) {
        streams.put(streamId, session);
        log.info("Stream added: {} → service={}", streamId, session.getService());
    }

    public StreamSession get(int streamId) {
        return streams.get(streamId);
    }

    public StreamSession remove(int streamId) {
        StreamSession removed = streams.remove(streamId);
        if (removed != null) {
            log.info("Stream removed: {} (service={})", streamId, removed.getService());
        }
        return removed;
    }

    public boolean contains(int streamId) {
        return streams.containsKey(streamId);
    }

    public int size() {
        return streams.size();
    }

    public void clear() {
        int size = streams.size();
        streams.clear();
        log.info("StreamManager cleared: {} streams removed", size);
    }

    public void register(int streamId, Channel user, Channel connector, String service) {
        StreamSession session = new StreamSession(streamId, user, connector, service);
        put(streamId, session);

        log.info("Stream registered: {} → user={}, connector={}, service={}",
                streamId,
                user.remoteAddress(),
                connector.remoteAddress(),
                service);
    }

    public Channel getConnector(int streamId) {
        StreamSession s = streams.get(streamId);
        if (s == null) return null;
        return s.getConnector();
    }

    public Channel getUser(int streamId) {
        StreamSession s = streams.get(streamId);
        if (s == null) return null;
        return s.getUser();
    }

    public int removeByUser(Channel userChannel) {
        int count = 0;

        Iterator<Map.Entry<Integer, StreamSession>> iterator =
                streams.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, StreamSession> entry = iterator.next();
            StreamSession session = entry.getValue();

            if (session.getUser().equals(userChannel)) {
                iterator.remove();
                count++;

                log.info("Cleaning stream {} (user disconnected, service={})",
                        entry.getKey(), session.getService());
            }
        }

        return count;
    }

    public int removeByConnector(Channel connectorChannel) {
        int count = 0;

        Iterator<Map.Entry<Integer, StreamSession>> iterator =
                streams.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, StreamSession> entry = iterator.next();
            StreamSession session = entry.getValue();

            if (session.getConnector().equals(connectorChannel)) {
                iterator.remove();
                count++;

                log.info("Cleaning stream {} (connector disconnected, service={})",
                        entry.getKey(), session.getService());
            }
        }

        return count;
    }

    public int countByUser(Channel userChannel) {
        int count = 0;

        for (StreamSession session : streams.values()) {
            if (session.getUser().equals(userChannel)) {
                count++;
            }
        }

        return count;
    }

    public int countByConnector(Channel connectorChannel) {
        int count = 0;

        for (StreamSession session : streams.values()) {
            if (session.getConnector().equals(connectorChannel)) {
                count++;
            }
        }

        return count;
    }

    public List<Integer> getStreamIdsByUser(Channel userChannel) {
        List<Integer> ids = new ArrayList<>();

        for (Map.Entry<Integer, StreamSession> entry : streams.entrySet()) {
            if (entry.getValue().getUser().equals(userChannel)) {
                ids.add(entry.getKey());
            }
        }

        return ids;
    }

    public List<Integer> getStreamIdsByConnector(Channel connectorChannel) {
        List<Integer> ids = new ArrayList<>();

        for (Map.Entry<Integer, StreamSession> entry : streams.entrySet()) {
            if (entry.getValue().getConnector().equals(connectorChannel)) {
                ids.add(entry.getKey());
            }
        }

        return ids;
    }
}