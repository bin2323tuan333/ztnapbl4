package com.example.ztna_server.netty;

import com.example.ztna_server.config.ZtnaProperties;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ChannelHandler.Sharable
public class WsPathRouter extends ChannelInboundHandlerAdapter {

    private final ZtnaProperties props;
    private final ConnectorWsHandler connectorHandler;

    public WsPathRouter(ZtnaProperties props, ConnectorWsHandler connectorHandler) {
        this.props = props;
        this.connectorHandler = connectorHandler;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete h) {
            String uri = h.requestUri();
            log.info("WS handshake: uri={} remote={}", uri, ctx.channel().remoteAddress());

            // Tách path và query string
            String path = uri;
            Map<String, String> query = new HashMap<>();
            int qIdx = uri.indexOf('?');
            if (qIdx >= 0) {
                path = uri.substring(0, qIdx);
                query = parseQuery(uri.substring(qIdx + 1));
            }

            if (path.startsWith("/ws/connector")) {
                // Ưu tiên header, fallback query param
                String token = extractToken(h, query);
                String expected = props.getConnector().getToken();

                if (token == null || !token.equals(expected)) {
                    log.warn("Connector rejected: bad token, got={}", token);
                    ctx.close();
                    return;
                }

                ctx.pipeline().addLast("connectorHandler", connectorHandler);
                ctx.pipeline().remove(this);
                log.info("Routed to ConnectorWsHandler");

            } else {
                log.warn("Unknown WS path: {}", path);
                ctx.close();
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    private String extractToken(WebSocketServerProtocolHandler.HandshakeComplete h,
                                Map<String, String> query) {
        // 1. Header Authorization
        String auth = h.requestHeaders().get("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        // 2. Query param ?token=xxx
        String t = query.get("token");
        if (t != null && !t.isEmpty()) {
            return t;
        }
        return null;
    }

    private Map<String, String> parseQuery(String qs) {
        Map<String, String> map = new HashMap<>();
        for (String pair : qs.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                map.put(pair.substring(0, eq), pair.substring(eq + 1));
            }
        }
        return map;
    }
}