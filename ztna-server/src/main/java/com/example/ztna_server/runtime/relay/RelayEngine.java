package com.example.ztna_server.runtime.relay;

import com.example.ztna_server.protocol.frames.DataFrame;
import com.example.ztna_server.runtime.session.SessionContext;
import com.example.ztna_server.runtime.session.SessionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelayEngine {

    private final SessionManager sessionManager;

    /** sender: kênh WS đã gửi frame. Chỉ chuyển tiếp khi stream thuộc đúng kênh đó. */
    public RelayResult forwardToConnector(DataFrame frame, Channel sender) {
        return forward(frame, Direction.AGENT_TO_CONNECTOR, sender);
    }

    public RelayResult forwardToAgent(DataFrame frame, Channel sender) {
        return forward(frame, Direction.CONNECTOR_TO_AGENT, sender);
    }

    private RelayResult forward(DataFrame frame, Direction dir, Channel sender) {
        if (frame == null) {
            return RelayResult.INVALID_FRAME;
        }

        String streamId = frame.getStreamId();
        if (streamId == null || streamId.isBlank()) {
            log.warn("[Relay] Frame thiếu streamId, dir={}", dir);
            releaseQuietly(frame);
            return RelayResult.INVALID_FRAME;
        }

        SessionContext session = sessionManager.get(streamId);
        if (session == null) {
            log.warn("[Relay] Session không tồn tại: stream={} dir={}", streamId, dir);
            releaseQuietly(frame);
            return RelayResult.SESSION_NOT_FOUND;
        }

        if (!session.isActive()) {
            log.warn("[Relay] Session chưa ACTIVE: stream={} state={} dir={}",
                    streamId, session.getState(), dir);
            releaseQuietly(frame);
            return RelayResult.SESSION_NOT_ACTIVE;
        }

        Channel source = dir == Direction.AGENT_TO_CONNECTOR
                ? session.getAgentChannel()
                : session.getConnectorChannel();
        Channel dest = dir == Direction.AGENT_TO_CONNECTOR
                ? session.getConnectorChannel()
                : session.getAgentChannel();

        // Chống chèn dữ liệu: một Agent/Connector biết streamId của người khác cũng không gửi được vào stream đó
        if (sender != source) {
            log.warn("[Relay] Từ chối frame: stream={} không thuộc kênh gửi, dir={}", streamId, dir);
            releaseQuietly(frame);
            return RelayResult.INVALID_FRAME;
        }

        if (source == null || !source.isActive()) {
            log.warn("[Relay] Source channel inactive: stream={} dir={}", streamId, dir);
            releaseQuietly(frame);
            sessionManager.closeSession(streamId, "source channel inactive");
            return RelayResult.SOURCE_INACTIVE;
        }

        if (dest == null || !dest.isActive()) {
            log.warn("[Relay] Dest channel inactive: stream={} dir={}", streamId, dir);
            releaseQuietly(frame);
            sessionManager.closeSession(streamId, "dest channel inactive");
            return RelayResult.DESTINATION_INACTIVE;
        }

        dest.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(frame.encode())
        )).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                Throwable cause = future.cause();
                log.warn("[Relay] Write failed: stream={} dir={} cause={}",
                        streamId, dir, cause != null ? cause.toString() : "unknown");
                sessionManager.failSession(streamId,
                        "write failed: " + (cause != null ? cause.getMessage() : "unknown"));
            }
        });

        return RelayResult.SUCCESS;
    }


    private static void releaseQuietly(Object msg) {
        try {
            ReferenceCountUtil.safeRelease(msg);
        } catch (Exception e) {
            log.warn("[Relay] Failed to release message", e);
        }
    }


}