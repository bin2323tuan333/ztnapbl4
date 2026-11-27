package com.example.ztna_server.service;

import com.example.ztna_server.entity.User;
import com.example.ztna_server.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    /** Claim phân biệt loại token: chỉ access token mới dùng để gọi API và mở ứng dụng qua Agent. */
    public static final String CLAIM_TYPE = "typ";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final UserRepository userRepository;

    private volatile SecretKey signingKey;

    private SecretKey key() {
        SecretKey k = signingKey;
        if (k == null) {
            synchronized (this) {
                if (signingKey == null) {
                    signingKey = buildKey();
                }
                k = signingKey;
            }
        }
        return k;
    }

    /** JWT_SECRET thiếu hoặc ngắn hơn 32 byte (HS256 yêu cầu) thì sinh secret ngẫu nhiên cho lần chạy này. */
    private SecretKey buildKey() {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            log.warn("JWT_SECRET chưa đặt hoặc ngắn hơn 32 ký tự: dùng secret ngẫu nhiên. "
                    + "Token cũ mất hiệu lực sau mỗi lần khởi động lại server.");
            bytes = new byte[64];
            new SecureRandom().nextBytes(bytes);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key())
                .compact();
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(key())
                .compact();
    }



    public UUID extractUserId(String token) {
        return UUID.fromString(
                Jwts.parser()
                        .verifyWith(key())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }

    public Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // Hết hạn, hoặc ký bằng secret cũ (server đã đổi JWT_SECRET): coi như không dùng được nữa,
            // thay vì để lỗi 500 lúc đăng nhập
            return true;
        }
    }
    /** Kiểm chữ ký + hạn dùng và bắt buộc là access token. Refresh token (hạn 7 ngày) bị từ chối. */
    public Claims parseAccessClaims(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("Không phải access token");
        }
        return claims;
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)   
                .getPayload();
    }
}
