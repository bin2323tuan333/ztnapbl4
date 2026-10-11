package com.example.ztna_server.service;

import com.example.ztna_server.api.dto.ChangePasswordRequest;
import com.example.ztna_server.api.dto.UserResponse;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.enums.AuditAction;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.exception.ErrorCode;
import com.example.ztna_server.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public UserResponse getByUsername(String username) {
        User user = findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return toResponse(user);
    }

    public UserResponse getById(Long id) {
        User user = findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return toResponse(user);
    }

    @Transactional
    public User createUser(String username, String rawPassword, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (email != null && !email.isBlank()
                && userRepository.findByEmail(email).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .email(email)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        auditLogService.log(
                saved.getId(),
                saved.getUsername(),
                AuditAction.CREATE_USER,
                "USER",
                saved.getId(),
                "Tạo người dùng"
        );

        return saved;
    }

    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditLogService.log(
                user.getId(),
                user.getUsername(),
                AuditAction.CHANGE_PASSWORD,
                "USER",
                user.getId(),
                "Đổi mật khẩu"
        );
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        auditLogService.log(
                saved.getId(),
                saved.getUsername(),
                enabled ? AuditAction.ENABLE_USER : AuditAction.DISABLE_USER,
                "USER",
                saved.getId(),
                enabled ? "Mở khóa tài khoản" : "Khóa tài khoản"
        );

        return toResponse(saved);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}