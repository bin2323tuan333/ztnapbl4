package org.example.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.example.Factory;
import org.example.tunnel.TunnelClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final TunnelClient tunnelClient = Factory.tunnelClient;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (HttpMethod.OPTIONS.equals(req.method())) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
            addCorsHeaders(response);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        QueryStringDecoder queryDecoder = new QueryStringDecoder(req.uri());
        Map<String, List<String>> parameters = queryDecoder.parameters();

        String appId = getParam(parameters, "appId");
        String appType = getParam(parameters, "type");

        if (appId == null || appId.isEmpty()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "Thiếu tham số appId");
            return;
        }

        String jwt = "";
        String authHeader = req.headers().get("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        String reqId = UUID.randomUUID().toString();
        Factory.pendingHttpRequest.put(reqId, ctx.channel(), appType, appId);
        tunnelClient.sendHttpAccess(reqId, appId, jwt);
    }

    private String getParam(Map<String, List<String>> parameters, String key) {
        List<String> values = parameters.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String errorMsg) {
        String jsonBody = "{\"error\":\"" + errorMsg + "\"}";
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(jsonBody, CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        addCorsHeaders(response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void addCorsHeaders(FullHttpResponse response) {
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS");
    }
}