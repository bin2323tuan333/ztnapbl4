package com.example.ztna_server.service;

import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.UserRole;
import com.example.ztna_server.enums.Role;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.repository.UserRoleRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    public String getUsername(String token) {
        return parse(token).getSubject();
    }


    public UUID getUserId(String token) {
        return UUID.fromString(
                parse(token).get("id", String.class)
        );
    }


    public Role getRole(String token) {
        return Role.valueOf(
                parse(token).get("role", String.class)
        );
    }

    public Date getExpiresAt(String token) {
        return parse(token).getExpiration();
    }

}