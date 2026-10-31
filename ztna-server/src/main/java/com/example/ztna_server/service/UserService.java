package com.example.ztna_server.service;

import com.example.ztna_server.dto.internal.UserInfo;
import com.example.ztna_server.dto.request.CreateUserRequest;
import com.example.ztna_server.dto.request.UpdatePasswordRequest;
import com.example.ztna_server.dto.request.UpdateUserRequest;
import com.example.ztna_server.dto.response.UserResponse;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.enums.Role;
import com.example.ztna_server.enums.TargetType;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.mapper.UserMapper;
import com.example.ztna_server.repository.RefreshTokenRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;          

    // ───────────── Dùng nội bộ ─────────────

    public UserInfo getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return userMapper.toUserInfo(user);
    }

    public UserInfo getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return userMapper.toUserInfo(user);
    }

    public UserInfo getUserById(UUID id) {
        return userMapper.toUserInfo(findUser(id));
    }

    public void updateLastLogin(UUID id) {
        User user = findUser(id);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    // ───────────── Quản lý user ─────────────

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse getUserByID(UUID id) {
        return userMapper.toUserResponse(findUser(id));
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        String email = normalizeEmail(request.getEmail());
        if (email != null && userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(email)
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User saved = userRepository.save(user);

        // Ghi audit
        auditService.log(
                "CREATE_USER",
                TargetType.USER,
                saved.getId().toString(),
                saved.getUsername(),
                getClientIp()
        );

        return userMapper.toUserResponse(saved);
    }

    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUser(id);

        String email = normalizeEmail(request.getEmail());
        if (email != null) {
            userRepository.findByEmail(email)
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new AppException(ErrorCode.EMAIL_EXISTED); });
        }

        if (request.getRole() != null && request.getRole() != user.getRole()) {
            ensureNotSelf(id);
            user.setRole(request.getRole());
        }

        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);

        // Ghi audit
        auditService.log(
                "UPDATE_USER",
                TargetType.USER,
                saved.getId().toString(),
                saved.getUsername(),
                getClientIp()
        );

        return userMapper.toUserResponse(saved);
    }

    public void blockUser(UUID id) {
        ensureNotSelf(id);
        User user = findUser(id);
        if (user.getEnabled()) {
            user.setEnabled(false);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);

            // Ghi audit
            auditService.log(
                    "BLOCK_USER",
                    TargetType.USER,
                    user.getId().toString(),
                    user.getUsername(),
                    getClientIp()
            );
        }
        refreshTokenRepository.findByUserId(id).ifPresent(refreshTokenRepository::delete);
    }

    public void unblockUser(UUID id) {
        User user = findUser(id);
        if (!user.getEnabled()) {
            user.setEnabled(true);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);

            // Ghi audit
            auditService.log(
                    "UNBLOCK_USER",
                    TargetType.USER,
                    user.getId().toString(),
                    user.getUsername(),
                    getClientIp()
            );
        }
    }

    public void resetPassword(UUID id, UpdatePasswordRequest request) {
        User user = findUser(id);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Đổi mật khẩu → đăng xuất phiên cũ
        refreshTokenRepository.findByUserId(id).ifPresent(refreshTokenRepository::delete);

        // Ghi audit
        auditService.log(
                "RESET_PASSWORD",
                TargetType.USER,
                user.getId().toString(),
                user.getUsername(),
                getClientIp()
        );
    }

    public void deleteUser(UUID id) {
        ensureNotSelf(id);
        User user = findUser(id);
        String username = user.getUsername(); // giữ lại trước khi xóa

        refreshTokenRepository.findByUserId(id).ifPresent(refreshTokenRepository::delete);
        try {
            userRepository.delete(user);
            userRepository.flush();

            // Ghi audit
            auditService.log(
                    "DELETE_USER",
                    TargetType.USER,
                    id.toString(),
                    username,
                    getClientIp()
            );
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_IN_USE);
        }
    }

    // ───────────── Hàm phụ ─────────────

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void ensureNotSelf(UUID id) {
        if (id.equals(SecurityUtils.currentUserId())) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_SELF);
        }
    }

    private String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? null : email.trim();
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;

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