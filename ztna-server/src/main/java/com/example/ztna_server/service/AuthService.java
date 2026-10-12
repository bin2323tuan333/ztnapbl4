package com.example.ztna_server.service;

import com.example.ztna_server.api.dto.LoginRequest;
import com.example.ztna_server.api.dto.LoginResponse;
import com.example.ztna_server.api.dto.RegisterRequest;
import com.example.ztna_server.entity.SystemRole;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.enums.AuditAction;
import com.example.ztna_server.entity.enums.SystemRoleName;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.exception.ErrorCode;
import com.example.ztna_server.repo.SystemRoleRepository;
import com.example.ztna_server.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final SystemRoleRepository systemRoleRepository;   // ← thêm

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            if (Boolean.FALSE.equals(user.getEnabled())) {
                throw new AppException(ErrorCode.ACCOUNT_DISABLED);
            }

            userService.updateLastLogin(user);

            String token = jwtService.generateAccessToken(user);

            auditLogService.log(
                    user.getId(),
                    user.getUsername(),
                    AuditAction.LOGIN,
                    "USER",
                    user.getId(),
                    "Đăng nhập thành công"
            );

            return LoginResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .build();

        } catch (AppException e) {
            auditLogService.log(
                    request.getUsername(),
                    AuditAction.LOGIN_FAILED,
                    "USER",
                    null,
                    e.getErrorCode().getMessage()
            );
            throw e;
        } catch (DisabledException e) {
            auditLogService.log(
                    request.getUsername(),
                    AuditAction.LOGIN_FAILED,
                    "USER",
                    null,
                    "Tài khoản đã bị khóa"
            );
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        } catch (AuthenticationException e) {
            auditLogService.log(
                    request.getUsername(),
                    AuditAction.LOGIN_FAILED,
                    "USER",
                    null,
                    "Sai tên đăng nhập hoặc mật khẩu"
            );
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        SystemRole defaultRole = systemRoleRepository
                .findByName(SystemRoleName.USER)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_ROLE_NOT_EXISTED));

        User user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                defaultRole
        );

        String token = jwtService.generateAccessToken(user);

        auditLogService.log(
                user.getId(),
                user.getUsername(),
                AuditAction.REGISTER,
                "USER",
                user.getId(),
                "Đăng ký tài khoản mới với role USER"
        );

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}