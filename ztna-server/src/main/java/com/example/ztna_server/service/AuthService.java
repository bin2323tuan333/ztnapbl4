package com.example.ztna_server.service;

import com.example.ztna_server.dto.internal.UserInfo;
import com.example.ztna_server.dto.request.LoginRequest;
import com.example.ztna_server.dto.request.TokenLoginRequest;
import com.example.ztna_server.dto.response.LoginResponse;
import com.example.ztna_server.entity.RefreshToken;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.UserRole;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.enums.Role;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.repository.RefreshTokenRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.repository.UserRoleRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponse login(LoginRequest request) {
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


        String token = jwtService.generateToken(user);
        Date date = jwtService.getExpiresAt(token);
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(token)
                .expiredAt(date.toInstant())
                .user(user)
                .build();

        return LoginResponse.builder()
                .token(token)
                .build();
    }

    public void logout(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse((refreshToken) -> {
                    refreshTokenRepository.delete(refreshToken);
                },() -> {
                    throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });
    }

    public void logoutAll() {
        refreshTokenRepository.deleteAll();
    }

}