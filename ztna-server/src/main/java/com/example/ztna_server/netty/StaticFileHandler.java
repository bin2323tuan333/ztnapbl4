package com.example.ztna_server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@ChannelHandler.Sharable
public class StaticFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        String path = req.uri();

        // ═══════════════════════════════════════════════════════════
        // ① Bỏ query string
        // ═══════════════════════════════════════════════════════════
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }

        // ═══════════════════════════════════════════════════════════
        // ② Default: "/" → "/index.html"
        // ═══════════════════════════════════════════════════════════
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        log.info("Static: {}", path);

        // ═══════════════════════════════════════════════════════════
        // ③ Thử load file thật
        // ═══════════════════════════════════════════════════════════
        byte[] content = loadResource("static" + path);
        String servedPath = path;

        // ═══════════════════════════════════════════════════════════
        // ④ SPA Fallback: nếu không tìm thấy → index.html
        //    (cho React Router: /login, /apps → index.html)
        // ═══════════════════════════════════════════════════════════
        if (content == null) {
            log.info("SPA fallback: {} → /index.html", path);
            content = loadResource("static/index.html");
            servedPath = "/index.html";
        }

        // ═══════════════════════════════════════════════════════════
        // ⑤ Vẫn không có → 404
        // ═══════════════════════════════════════════════════════════
        if (content == null) {
            log.error("index.html not found in resources/static/!");
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // ═══════════════════════════════════════════════════════════
        // ⑥ Gửi response
        // ═══════════════════════════════════════════════════════════
        log.info("Serving: {} ({} bytes)", servedPath, content.length);
        sendResponse(ctx, content, getContentType(servedPath));
    }

    // ═══════════════════════════════════════════════════════════════
    // LOAD FILE TỪ CLASSPATH
    // ═══════════════════════════════════════════════════════════════
    private byte[] loadResource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return is != null ? is.readAllBytes() : null;
        } catch (IOException e) {
            log.error("Error loading resource: {}", path, e);
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SEND RESPONSE
    // ═══════════════════════════════════════════════════════════════
    private void sendResponse(ChannelHandlerContext ctx, byte[] content, String contentType) {
        ByteBuf buf = Unpooled.wrappedBuffer(content);

        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                buf
        );
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        res.headers().set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=3600");

        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.EMPTY_BUFFER
        );
        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    }

    // ═══════════════════════════════════════════════════════════════
    // CONTENT TYPE
    // ═══════════════════════════════════════════════════════════════
    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".ico")) return "image/x-icon";
        if (path.endsWith(".woff")) return "font/woff";
        if (path.endsWith(".woff2")) return "font/woff2";
        if (path.endsWith(".ttf")) return "font/ttf";
        if (path.endsWith(".map")) return "application/json";
        return "application/octet-stream";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("StaticFileHandler error", cause);
        ctx.close();
    }
}