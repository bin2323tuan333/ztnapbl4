package com.example.ztna_server.security;

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

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Không có token: cho đi tiếp, SecurityConfig sẽ chặn nếu API cần đăng nhập
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = jwtService.parseClaims(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // Refresh token không có claim "role", nên không được dùng thay access token
            if (role != null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        userId,                                         // principal = userId (UUID dạng chuỗi)
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)) // ROLE_ADMIN, ROLE_USER...
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Token sai hoặc hết hạn: không xác thực, request sẽ nhận 401
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
