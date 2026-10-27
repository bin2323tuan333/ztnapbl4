package org.example.stream;

import io.netty.channel.Channel;
import org.example.protocol.frames.OpenResFrame;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class StreamRegistry {
    // Map lưu channel đang hoạt động (streamId -> Channel)
    private final Map<String, Channel> streams = new ConcurrentHashMap<>();

    // Hàng đợi hứng kết quả OpenRes từ Gateway trả về
    private final BlockingQueue<OpenResFrame> openResQueue = new LinkedBlockingQueue<>();

    public void register(String streamId, Channel channel) {
        streams.put(streamId, channel);
    }

    public Channel get(String streamId) {
        return streams.get(streamId);
    }

    public Channel remove(String streamId) {
        return streams.remove(streamId);
    }

    public void putOpenResponse(OpenResFrame frame) {
        openResQueue.offer(frame);
    }

    public OpenResFrame waitForOpenResponse() throws InterruptedException {
        // Chờ tối đa 10 giây
        return openResQueue.poll(10, TimeUnit.SECONDS);
    }

    public void closeAll() {
        streams.values().forEach(Channel::close);
        streams.clear();
    }
}