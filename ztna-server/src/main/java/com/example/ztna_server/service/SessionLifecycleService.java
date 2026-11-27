
package com.example.ztna_server.service;

import com.example.ztna_server.entity.User;
import com.example.ztna_server.protocol.frames.CloseFrame;
import com.example.ztna_server.protocol.frames.ResourceAccessFrame;
import com.example.ztna_server.protocol.frames.ResourceResFrame;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.runtime.connection.agent.AgentConnection;
import com.example.ztna_server.runtime.connection.agent.AgentConnectionRegistry;
import com.example.ztna_server.runtime.connection.agent.PendingConnect;
import com.example.ztna_server.runtime.connection.agent.PendingConnectRegistry;
import com.example.ztna_server.runtime.connection.connector.ConnectorConnection;
import com.example.ztna_server.runtime.connection.connector.ConnectorConnectionRegistry;
import com.example.ztna_server.runtime.routing.RoutingRegistry;
import com.example.ztna_server.runtime.session.SessionContext;
import com.example.ztna_server.runtime.session.SessionManager;
import com.example.ztna_server.runtime.session.SessionState;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.ztna_server.event.AccessPolicyChangedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionLifecycleService {
    private final RoutingRegistry routingRegistry;
    private final ConnectorConnectionRegistry connectorConnectionRegistry;
    private final AgentConnectionRegistry agentConnectionRegistry;

    private final PendingConnectRegistry pendingConnectRegistry;
    private final SessionManager sessionManager;

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ConnectorRepository connectorRepository;

    private final RuleService ruleService;
    private final AccessLogService accessLogService;


    public boolean openSession(String streamId) {
        PendingConnect c = pendingConnectRegistry.get(streamId);
        if (c == null) {
            return false;
        }

        // Dùng đúng kênh Agent đã gửi RESOURCE_ACCESS (nhiều người dùng có thể chung một Agent)
        Channel agentChannel = c.getAgentChannel();
        if (agentChannel == null || !agentChannel.isActive()) {
            pendingConnectRegistry.remove(streamId);
            return false;
        }

        ConnectorConnection connectorConnection = connectorConnectionRegistry.getByConnectorId(c.getConnectorId());
        if (connectorConnection == null || !connectorConnection.isActive()) {
            pendingConnectRegistry.remove(streamId);
            sendResourceFail(agentChannel, c.getRequestId(), "Connector offline");
            return false;
        }

        User user = userRepository.findById(UUID.fromString(c.getUserId())).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            pendingConnectRegistry.remove(streamId);
            sendResourceFail(agentChannel, c.getRequestId(), "Tài khoản đã bị khóa");
            return false;
        }

        SessionContext session = SessionContext.builder()
                .streamId(streamId)
                .userId(c.getUserId())
                .connectorId(c.getConnectorId())
                .resourceId(c.getResourceId())
                .agentChannel(agentChannel)
                .connectorChannel(connectorConnection.getChannel())
                .state(SessionState.ACTIVE)
                .build();

        sessionManager.add(session);
        pendingConnectRegistry.remove(streamId);

        agentChannel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(
                new ResourceResFrame(c.getRequestId(), streamId, ResourceResFrame.STATUS_OK, "").encode()
        ))).addListener(f -> {
            if (!f.isSuccess()) {
                sessionManager.failSession(streamId, "Cannot resend to agent");
            }
        });

        log.info("[Lifecycle] SESSION_OPENED streamId={}, user={}, app={}", streamId, c.getUserId(), c.getResourceId());
        return true;
    }

    /**
     * Đóng stream theo yêu cầu của một bên (Agent hoặc Connector gửi CLOSE).
     * Chỉ chấp nhận khi stream thuộc đúng kênh gửi, để không ai đóng được phiên của người khác.
     */
    public void closeSessionFrom(Channel sender, String streamId, String reason) {
        SessionContext session = sessionManager.get(streamId);
        if (session == null) {
            return;
        }
        if (sender != session.getAgentChannel() && sender != session.getConnectorChannel()) {
            log.warn("[Lifecycle] Bỏ qua CLOSE: stream={} không thuộc kênh gửi", streamId);
            return;
        }
        closeSession(streamId, reason);
    }

    public void closeSession(String streamId, String reason) {
        SessionContext session = sessionManager.get(streamId);
        if (session == null) {
            return;
        }
        // Xoá khỏi SessionManager trước: từ lúc này RelayEngine không chuyển tiếp DATA của stream này nữa.
        // Nếu một luồng khác đã đóng trước thì thôi, không gửi CLOSE lần hai.
        if (!sessionManager.closeSession(streamId, reason)) {
            return;
        }
        log.info("[Lifecycle] Closing session: stream={}, reason={}", streamId, reason);
        sendClose(session.getAgentChannel(), streamId);
        sendClose(session.getConnectorChannel(), streamId);
    }

    // ─────────── Xác minh liên tục (Zero Trust) ───────────

    /** Admin vừa đổi rule / user / ứng dụng / connector: kiểm lại ngay sau khi DB đã commit. */
    @TransactionalEventListener(fallbackExecution = true)
    public void onAccessPolicyChanged(AccessPolicyChangedEvent event) {
        revalidateSessions(event.reason());
    }

    /** Lưới an toàn: kiểm định kỳ, bắt cả trường hợp rule hết hạn theo thời gian (expiresAt). */
    @Scheduled(fixedDelayString = "15000", initialDelayString = "15000")
    public void revalidatePeriodically() {
        revalidateSessions("kiểm tra định kỳ");
    }

    /**
     * Đóng mọi phiên đang mở mà người dùng không còn quyền:
     * tài khoản bị khoá/xoá, không còn rule đang bật và còn hạn, hoặc ứng dụng/connector bị tắt/xoá.
     */
    public void revalidateSessions(String trigger) {
        List<SessionContext> sessions = sessionManager.getAll();
        if (sessions.isEmpty()) return;

        Map<String, Boolean> decisionCache = new HashMap<>(); // userId|resourceId -> còn quyền?
        int closed = 0;
        for (SessionContext s : sessions) {
            String key = s.getUserId() + "|" + s.getResourceId();
            String denyReason = decisionCache.containsKey(key)
                    ? (decisionCache.get(key) ? null : "không còn quyền truy cập")
                    : checkAccess(s.getUserId(), s.getResourceId());
            decisionCache.putIfAbsent(key, denyReason == null);

            if (denyReason != null) {
                log.warn("[Lifecycle] Thu hồi phiên stream={}, user={}, resource={}: {} (do {})",
                        s.getStreamId(), s.getUserId(), s.getResourceId(), denyReason, trigger);
                closeSession(s.getStreamId(), "REVOKED: " + denyReason);
                closed++;
            }
        }
        if (closed > 0) {
            log.info("[Lifecycle] Đã đóng {} phiên không còn hợp lệ (do {})", closed, trigger);
        }
    }

    /** @return null nếu còn quyền, ngược lại là lý do bị thu hồi */
    private String checkAccess(String userIdStr, String resourceIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            UUID resourceId = UUID.fromString(resourceIdStr);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
                return "tài khoản đã bị khoá hoặc xoá";
            }
            if (!resourceRepository.existsByIdAndEnabledTrueAndConnectorEnabledTrue(resourceId)) {
                return "ứng dụng hoặc connector đã bị tắt hoặc xoá";
            }
            if (!ruleService.hasAccess(userId, resourceId)) {
                return "không còn rule cho phép (rule bị tắt, xoá, hết hạn hoặc người dùng bị gỡ khỏi rule)";
            }
            return null;
        } catch (Exception e) {
            // Không kiểm được thì đóng phiên (fail-closed), đúng nguyên tắc Zero Trust
            log.error("[Lifecycle] Lỗi khi kiểm lại quyền user={}, resource={}", userIdStr, resourceIdStr, e);
            return "lỗi khi kiểm tra quyền";
        }
    }


    private void sendClose(Channel channel, String streamId) {
        if (channel == null || !channel.isActive()) {
            return;
        }

        CloseFrame frame = new CloseFrame();
        frame.setStreamId(streamId);

        channel.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(frame.encode())
        ));
    }


    /** Kênh Agent đóng: huỷ mọi yêu cầu đang chờ và đóng mọi phiên đi qua kênh đó (của mọi người dùng trên Agent). */
    public void onAgentChannelClosed(Channel agentChannel) {
        for (PendingConnect pending : pendingConnectRegistry.getAll()) {
            if (pending.getAgentChannel() == agentChannel) {
                pendingConnectRegistry.remove(pending.getStreamId());
            }
        }

        List<SessionContext> sessions = sessionManager.getAll().stream()
                .filter(s -> s.getAgentChannel() == agentChannel)
                .toList();
        log.info("[Lifecycle] Agent disconnected, closing {} sessions", sessions.size());
        for (SessionContext session : sessions) {
            closeSession(session.getStreamId(), "AGENT_DISCONNECTED");
        }
    }


    public void onConnectorDisconnect(String connectorId) {
        List<PendingConnect> pendingList = pendingConnectRegistry.getByConnectorId(connectorId);

        for (PendingConnect pending : pendingList) {
            pendingConnectRegistry.remove(pending.getStreamId());
            sendResourceFail(pending.getAgentChannel(), pending.getRequestId(), "Connector đã mất kết nối");
        }

        List<SessionContext> sessions = sessionManager.getByConnectorId(connectorId);
        log.info("[Lifecycle] Connector connection {} disconnected, closing {} sessions", connectorId, sessions.size());
        for (SessionContext session : sessions) {
            closeSession(session.getStreamId(), "CONNECTOR_DISCONNECTED");
        }
    }




    private void sendResourceSuccess(Channel channel, String requestId, String streamId) {
        if (channel == null || !channel.isActive()) {
            return;
        }
        ResourceResFrame response = new ResourceResFrame(requestId, streamId, ResourceResFrame.STATUS_OK, "");
        channel.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(response.encode())
        ));
    }


    private void sendResourceFail(Channel channel, String requestId, String reason) {
        if (channel == null || !channel.isActive()) {
            return;
        }
        ResourceResFrame response = new ResourceResFrame(requestId, "", ResourceResFrame.STATUS_ERROR, reason);
        channel.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(response.encode())
        ));
    }



    public static String getIpAddress(Channel channel) {
        if (channel == null || channel.remoteAddress() == null) {
            return "Unknown";
        }

        if (channel.remoteAddress() instanceof java.net.InetSocketAddress address) {
            return address.getAddress() != null
                    ? address.getAddress().getHostAddress()
                    : address.getHostString();
        }

        return channel.remoteAddress().toString();
    }
}
