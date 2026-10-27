package com.example.ztna_server.service;

import com.example.ztna_server.dto.request.AssignRoleRequest;
import com.example.ztna_server.dto.request.SystemRoleRequest;
import com.example.ztna_server.dto.response.RoleMemberResponse;
import com.example.ztna_server.dto.response.SystemRoleResponse;
import com.example.ztna_server.entity.SystemRole;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.UserRole;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.repository.SystemRoleRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.repository.UserRoleRepository;
import com.example.ztna_server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Quản lý role nghiệp vụ (bảng system_roles) và việc gán role cho user (bảng user_roles).
 * Khác với enum Role trong bảng users (ADMIN/SUPERVISOR/USER) — enum đó là quyền hệ thống,
 * còn system_roles là nhóm nghiệp vụ (DEV, KE_TOAN...) dùng để phân quyền vào app ở bước sau.
 */
@Service
@RequiredArgsConstructor
public class SystemRoleService {

    private final SystemRoleRepository systemRoleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    // ───────────── CRUD role ─────────────

    public List<SystemRoleResponse> getRoles() {
        return systemRoleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SystemRoleResponse getRole(UUID id) {
        return toResponse(findRole(id));
    }

    public SystemRoleResponse createRole(SystemRoleRequest request) {
        String name = request.getName().trim();
        if (systemRoleRepository.existsByName(name)) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        SystemRole role = SystemRole.builder()
                .name(name)
                .displayName(request.getDisplayName().trim())
                .description(request.getDescription().trim())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return toResponse(systemRoleRepository.save(role));
    }

    public SystemRoleResponse updateRole(UUID id, SystemRoleRequest request) {
        SystemRole role = findRole(id);
        String name = request.getName().trim();
        systemRoleRepository.findByName(name)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new AppException(ErrorCode.ROLE_EXISTED); });

        role.setName(name);
        role.setDisplayName(request.getDisplayName().trim());
        role.setDescription(request.getDescription().trim());
        role.setUpdatedAt(Instant.now());
        return toResponse(systemRoleRepository.save(role));
    }

    public void deleteRole(UUID id) {
        SystemRole role = findRole(id);
        // Còn user trong role thì không cho xóa, tránh mất quyền ngoài ý muốn
        if (userRoleRepository.countByRoleId(id) > 0) {
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }
        try {
            systemRoleRepository.delete(role);
            systemRoleRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // Role đang được dùng trong bảng phân quyền (role_permitions)
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }
    }

    // ───────────── Gán / gỡ user khỏi role ─────────────

    @Transactional(readOnly = true)
    public List<RoleMemberResponse> getMembers(UUID roleId) {
        findRole(roleId);
        return userRoleRepository.findByRoleId(roleId).stream()
                .map(this::toMember)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleMemberResponse> getRolesOfUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return userRoleRepository.findByUserId(userId).stream()
                .map(this::toMember)
                .toList();
    }

    @Transactional
    public RoleMemberResponse assignUser(UUID roleId, AssignRoleRequest request) {
        SystemRole role = findRole(roleId);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (userRoleRepository.existsByUserIdAndRoleId(user.getId(), roleId)) {
            throw new AppException(ErrorCode.ROLE_ALREADY_ASSIGNED);
        }
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Admin đang đăng nhập là người gán (lấy từ JWT)
        User admin = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .grantedBy(admin)
                .grantedAt(Instant.now())
                .expiresAt(request.getExpiresAt())
                .reason(request.getReason().trim())
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        return toMember(userRoleRepository.save(userRole));
    }

    @Transactional
    public void removeUser(UUID roleId, UUID userId) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_ASSIGNED));
        userRoleRepository.delete(userRole);
    }

    // ───────────── Hàm phụ ─────────────

    private SystemRole findRole(UUID id) {
        return systemRoleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private SystemRoleResponse toResponse(SystemRole role) {
        return SystemRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .memberCount(userRoleRepository.countByRoleId(role.getId()))
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    // Gọi trong @Transactional vì user/role/grantedBy là LAZY
    private RoleMemberResponse toMember(UserRole ur) {
        return RoleMemberResponse.builder()
                .userId(ur.getUser().getId())
                .username(ur.getUser().getUsername())
                .fullName(ur.getUser().getFullName())
                .roleId(ur.getRole().getId())
                .roleName(ur.getRole().getName())
                .grantedBy(ur.getGrantedBy().getUsername())
                .grantedAt(ur.getGrantedAt())
                .expiresAt(ur.getExpiresAt())
                .reason(ur.getReason())
                .enabled(ur.isEnabled())
                .build();
    }
}
