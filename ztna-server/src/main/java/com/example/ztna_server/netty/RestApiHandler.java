package com.example.ztna_server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ChannelHandler.Sharable
public class RestApiHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        String path = req.uri();
        HttpMethod method = req.method();

        // Bỏ query string
        String cleanPath = path.contains("?")
                ? path.substring(0, path.indexOf("?"))
                : path;

        log.info("API: {} {}", method, cleanPath);

        try {
            // ═══════════════════════════════════════════════════════
            // POST /api/login
            // ═══════════════════════════════════════════════════════
            if (cleanPath.equals("/api/login") && method.equals(HttpMethod.POST)) {
                handleLogin(ctx, req);
            }
            // ═══════════════════════════════════════════════════════
            // POST /api/logout
            // ═══════════════════════════════════════════════════════
            else if (cleanPath.equals("/api/logout") && method.equals(HttpMethod.POST)) {
                handleLogout(ctx, req);
            }
            // ═══════════════════════════════════════════════════════
            // GET /api/apps — Danh sách apps
            // ═══════════════════════════════════════════════════════
            else if (cleanPath.equals("/api/apps") && method.equals(HttpMethod.GET)) {
                handleApps(ctx, req);
            }
            // ═══════════════════════════════════════════════════════
            // GET /api/user — Thông tin user
            // ═══════════════════════════════════════════════════════
            else if (cleanPath.equals("/api/user") && method.equals(HttpMethod.GET)) {
                handleUser(ctx, req);
            }
            // ═══════════════════════════════════════════════════════
            // GET /api/sessions — Danh sách session đang mở
            // ═══════════════════════════════════════════════════════
            else if (cleanPath.equals("/api/sessions") && method.equals(HttpMethod.GET)) {
                handleSessions(ctx, req);
            }
            // ═══════════════════════════════════════════════════════
            // 404
            // ═══════════════════════════════════════════════════════
            else {
                log.warn("Unknown endpoint: {} {}", method, cleanPath);
                sendJson(ctx, HttpResponseStatus.NOT_FOUND,
                        "{\"error\":\"Endpoint not found\"}");
            }

        } catch (Exception e) {
            log.error("API error: {} {}", method, cleanPath, e);
            sendJson(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Internal server error\"}");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /api/login — Đăng nhập
    // ═══════════════════════════════════════════════════════════════
    private void handleLogin(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String body = req.content().toString(CharsetUtil.UTF_8);
        log.info("Login request body: {}", body);

        LoginRequest lr = mapper.readValue(body, LoginRequest.class);

        // Validate
        if (lr.username() == null || lr.password() == null) {
            sendJson(ctx, HttpResponseStatus.BAD_REQUEST,
                    "{\"error\":\"Missing username or password\"}");
            return;
        }

        // Verify credentials (★ GIẢ LẬP — thay bằng DB/LDAP sau)
        if (!"alice".equals(lr.username()) || !"secret".equals(lr.password())) {
            log.warn("Login failed for user: {}", lr.username());
            sendJson(ctx, HttpResponseStatus.UNAUTHORIZED,
                    "{\"error\":\"Invalid credentials\"}");
            return;
        }

        // Tạo JWT (★ GIẢ LẬP — thay bằng JWT service sau)
        String jwt = generateFakeJwt(lr.username());

        log.info("Login OK: {}", lr.username());

        String response = mapper.writeValueAsString(Map.of(
                "token", jwt,
                "username", lr.username(),
                "expiresIn", 3600
        ));

        sendJson(ctx, HttpResponseStatus.OK, response);
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /api/logout
    // ═══════════════════════════════════════════════════════════════
    private void handleLogout(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String username = extractUser(req);
        log.info("Logout: {}", username);

        // (Nếu có blacklist JWT → thêm vào)

        sendJson(ctx, HttpResponseStatus.OK,
                "{\"message\":\"Logged out successfully\"}");
    }

    // ═══════════════════════════════════════════════════════════════
    // GET /api/apps — Danh sách apps
    // ═══════════════════════════════════════════════════════════════
    private void handleApps(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String username = extractUser(req);
        if (username == null) {
            sendJson(ctx, HttpResponseStatus.UNAUTHORIZED,
                    "{\"error\":\"Missing or invalid token\"}");
            return;
        }

        log.info("List apps for user: {}", username);

        // ★ GIẢ LẬP — thay bằng query DB theo user
        List<AppInfo> apps = List.of(
                new AppInfo("ssh-bastion", "SSH Bastion", "ssh.internal:22", "SSH"),
                new AppInfo("mysql-prod", "MySQL Production", "db.internal:3306", "MySQL"),
                new AppInfo("smb-files", "File Share", "smb.internal:445", "SMB"),
                new AppInfo("web-intranet", "Intranet Web", "web.internal:80", "HTTP")
        );

        String response = mapper.writeValueAsString(Map.of(
                "username", username,
                "apps", apps
        ));

        sendJson(ctx, HttpResponseStatus.OK, response);
    }

    // ═══════════════════════════════════════════════════════════════
    // GET /api/user — Thông tin user
    // ═══════════════════════════════════════════════════════════════
    private void handleUser(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String username = extractUser(req);
        if (username == null) {
            sendJson(ctx, HttpResponseStatus.UNAUTHORIZED,
                    "{\"error\":\"Missing or invalid token\"}");
            return;
        }

        // ★ GIẢ LẬP
        String response = mapper.writeValueAsString(Map.of(
                "userId", username,
                "name", "Alice Nguyen",
                "email", username + "@company.com",
                "role", "admin"
        ));

        sendJson(ctx, HttpResponseStatus.OK, response);
    }

    // ═══════════════════════════════════════════════════════════════
    // GET /api/sessions — Sessions đang mở
    // ═══════════════════════════════════════════════════════════════
    private void handleSessions(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String username = extractUser(req);
        if (username == null) {
            sendJson(ctx, HttpResponseStatus.UNAUTHORIZED,
                    "{\"error\":\"Missing or invalid token\"}");
            return;
        }

        // ★ GIẢ LẬP — thay bằng StreamManager.getUserSessions(username)
        String response = mapper.writeValueAsString(Map.of(
                "sessions", List.of()
        ));

        sendJson(ctx, HttpResponseStatus.OK, response);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * ★ Trích userId từ JWT trong Authorization header.
     * Giả lập — chỉ decode đơn giản. Thay bằng verify thật sau.
     */
    private String extractUser(FullHttpRequest req) {
        String auth = req.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }

        String token = auth.substring(7);
        return verifyJwt(token);
    }

    /**
     * ★ Verify JWT — GIẢ LẬP.
     * TODO: Dùng thư viện JWT (jjwt, nimbus-jose-jwt) để verify thật.
     */
    private String verifyJwt(String token) {
        // Giả lập: token format "header.payload.signature"
        // Payload = base64({"userId":"alice"})
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payload = new String(
                    java.util.Base64.getUrlDecoder().decode(parts[1]),
                    CharsetUtil.UTF_8
            );

            Map<String, Object> claims = mapper.readValue(payload, Map.class);
            return (String) claims.get("userId");

        } catch (Exception e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ★ Tạo JWT — GIẢ LẬP.
     * TODO: Dùng jjwt hoặc nimbus-jose-jwt.
     */
    private String generateFakeJwt(String username) {
        try {
            String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
            String payload = base64Url(mapper.writeValueAsString(Map.of(
                    "userId", username,
                    "iat", System.currentTimeMillis() / 1000,
                    "exp", System.currentTimeMillis() / 1000 + 3600
            )));
            String signature = "fake-signature-" + username;

            return header + "." + payload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String base64Url(String s) {
        return java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(s.getBytes(CharsetUtil.UTF_8));
    }

    /**
     * Gửi JSON response và đóng connection.
     */
    private void sendJson(ChannelHandlerContext ctx,
                          HttpResponseStatus status,
                          String body) {
        ByteBuf content = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);

        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, content
        );
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type");
        res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");

        log.info("Response: {} {} bytes", status.code(), content.readableBytes());

        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("RestApiHandler error", cause);
        ctx.close();
    }

    // ═══════════════════════════════════════════════════════════════
    // DTO
    // ═══════════════════════════════════════════════════════════════

    public record LoginRequest(String username, String password) {}

    public record AppInfo(String id, String name, String target, String type) {}
}