package com.example.ztna_server.security;

import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            // Chỉ nhận access token; refresh token không dùng để gọi API.
            Claims claims = jwtService.parseAccessClaims(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // Zero Trust: kiểm lại trạng thái tài khoản ở mỗi request, không tin JWT đến hết hạn.
            // User bị khoá hoặc bị xoá thì token cũ mất tác dụng ngay (trước đây còn dùng được tới 15 phút).
            boolean active = userRepository.findById(UUID.fromString(userId))
                    .map(u -> Boolean.TRUE.equals(u.getEnabled()))
                    .orElse(false);

            if (role != null && active) {
                var auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
