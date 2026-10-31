package com.example.ztna_server.service;

import com.example.ztna_server.dto.request.LoginRequest;
import com.example.ztna_server.dto.response.AuthResponse;
import com.example.ztna_server.dto.response.LoginResponse;
import com.example.ztna_server.entity.RefreshToken;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.enums.TargetType;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.repository.RefreshTokenRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditService auditService;          // <-- thêm

    public AuthResponse login(LoginRequest request) {
        String ip = getClientIp();

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    // Login thất bại – user không tồn tại
                    auditService.log(
                            null,
                            request.getUsername(),
                            "LOGIN_FAILED",
                            TargetType.USER,
                            null,
                            request.getUsername(),
                            ip
                    );
                    return new AppException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Login thất bại – sai mật khẩu
            auditService.log(
                    user.getId(),
                    user.getUsername(),
                    "LOGIN_FAILED",
                    TargetType.USER,
                    user.getId().toString(),
                    user.getUsername(),
                    ip
            );
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.getEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        userService.updateLastLogin(user.getId());

        // Login thành công
        auditService.log(
                user.getId(),
                user.getUsername(),
                "LOGIN",
                TargetType.USER,
                user.getId().toString(),
                user.getUsername(),
                ip
        );

        Optional<RefreshToken> existRefresdToken = refreshTokenRepository.findByUserId(user.getId());

        String refrenshToken;
        String accessToken = jwtService.generateAccessToken(user);

        if (existRefresdToken.isPresent() && !jwtService.isTokenExpired(existRefresdToken.get().getToken())) {
            refrenshToken = existRefresdToken.get().getToken();

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refrenshToken)
                    .build();
        } else if (existRefresdToken.isPresent()) {
            refreshTokenRepository.delete(existRefresdToken.get());
        }

        refrenshToken = jwtService.generateRefreshToken(user);
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(refrenshToken)
                .expiredAt(jwtService.extractExpiration(refrenshToken).toInstant())
                .user(user)
                .active(true)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refrenshToken)
                .build();
    }

    public void logout(UUID id) {
        String ip = getClientIp();

        refreshTokenRepository.findByUserId(id)
                .ifPresentOrElse(
                        refreshToken -> {
                            refreshTokenRepository.delete(refreshToken);

                            // Logout thành công
                            auditService.log(
                                    id,
                                    SecurityUtils.currentUsername(),
                                    "LOGOUT",
                                    TargetType.USER,
                                    id.toString(),
                                    null,
                                    ip
                            );
                        },
                        () -> { }
                );
    }

    public LoginResponse refresh(String refreshTokenStr) {
        RefreshToken dbToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (dbToken.getExpiredAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(dbToken);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = dbToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        // Ghi audit
        auditService.log(
                user.getId(),
                user.getUsername(),
                "REFRESH_TOKEN",
                TargetType.USER,
                user.getId().toString(),
                user.getUsername(),
                getClientIp()
        );
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    public void logoutAll() {
        refreshTokenRepository.deleteAll();
    }

    // ───────────────────────── Helper ─────────────────────────
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "unknown";

            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return null;
        }
    }
}