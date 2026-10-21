package com.example.ztna_server.service;

import com.example.ztna_server.dto.request.LoginRequest;
import com.example.ztna_server.dto.response.AuthResponse;
import com.example.ztna_server.dto.response.LoginResponse;
import com.example.ztna_server.entity.RefreshToken;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.repository.RefreshTokenRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
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

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.getEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        userService.updateLastLogin(user.getId());

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
        refreshTokenRepository.findByUserId(id)
                .ifPresentOrElse((refreshToken) -> {
                    refreshTokenRepository.delete(refreshToken);
                },() -> {

                });
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

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    public void logoutAll() {
        refreshTokenRepository.deleteAll();
    }
}