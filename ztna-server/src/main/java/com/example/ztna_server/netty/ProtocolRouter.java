package com.example.ztna_server.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ProtocolRouter extends ChannelInboundHandlerAdapter {
    private final ResourceWsHandler resourceWsHandler;
    private final RestApiHandler restApiHandler;
    private final StaticFileHandler staticFileHandler;

    public ProtocolRouter(ResourceWsHandler resourceWsHandler, RestApiHandler restApiHandler, StaticFileHandler staticFileHandler) {
        this.resourceWsHandler = resourceWsHandler;
        this.restApiHandler = restApiHandler;
        this.staticFileHandler = staticFileHandler;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest req)) {
            ctx.fireChannelRead(msg);
            return;
        }


        String path = req.uri();
        HttpMethod method = req.method();

        log.info("Request: {} {} from {}", method, path, ctx.channel().remoteAddress());

        if (isWebSocketUpgrade(req)) {
            log.info("→ WebSocket: {}", path);
            String wsPath = path.contains("?")
                    ? path.substring(0, path.indexOf("?"))
                    : path;
            ctx.pipeline().addLast(
                    new WebSocketServerProtocolHandler(
                            wsPath,
                            null,
                            true,
                            65536,
                            false,
                            true
                    )
            );
            ctx.pipeline().addLast(resourceWsHandler);
            ctx.fireChannelRead(req);
            return;
        }

        if (path.startsWith("/api/")) {
            log.info("→ REST API: {}", path);
            ctx.pipeline().addLast(restApiHandler);
            ctx.fireChannelRead(req);
            return;
        }

        log.info("→ Static: {}", path);
        ctx.pipeline().addLast(staticFileHandler);
        ctx.fireChannelRead(req);

    }

    private boolean isWebSocketUpgrade(FullHttpRequest req) {
        if (!HttpMethod.GET.equals(req.method())) return false;
        if (!"websocket".equalsIgnoreCase(req.headers().get(HttpHeaderNames.UPGRADE))) return false;
        String conn = req.headers().get(HttpHeaderNames.CONNECTION);
        if (conn == null || !conn.toLowerCase().contains("upgrade")) return false;
        if (req.headers().get("Sec-WebSocket-Key") == null) return false;
        return "13".equals(req.headers().get("Sec-WebSocket-Version"));
    }
}