package com.example.ztna_server.runtime.connection.agent;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vé truy cập (access ticket) cấp cho Agent sau khi HTTP_ACCESS xác thực JWT thành công.
 *
 * Một Agent có thể được nhiều người dùng chung (mỗi người một JWT). Vì vậy danh tính
 * không được gắn với kết nối WebSocket, mà gắn với từng vé:
 *   vé -> (userId, resourceId, kênh Agent đã nhận vé)
 * Mỗi RESOURCE_ACCESS phải gửi kèm vé; Server lấy userId từ vé rồi mới kiểm quyền.
 * Vé chỉ dùng được trên đúng kênh đã nhận nó và bị huỷ khi kênh đóng.
 */
@Component
public class AccessTicketRegistry {

    public record AccessTicket(String ticket, String userId, String resourceId, Channel agentChannel, Instant issuedAt) {}

    private final Map<String, AccessTicket> byTicket = new ConcurrentHashMap<>();

    public AccessTicket issue(String userId, String resourceId, Channel agentChannel) {
        String ticket = UUID.randomUUID().toString();
        AccessTicket t = new AccessTicket(ticket, userId, resourceId, agentChannel, Instant.now());
        byTicket.put(ticket, t);
        return t;
    }

    public AccessTicket get(String ticket) {
        if (ticket == null || ticket.isBlank()) return null;
        return byTicket.get(ticket);
    }

    /** Huỷ mọi vé đã cấp cho một kênh Agent (gọi khi kênh đóng). */
    public void removeByChannel(Channel agentChannel) {
        byTicket.values().removeIf(t -> t.agentChannel() == agentChannel);
    }

    public int size() {
        return byTicket.size();
    }
}
