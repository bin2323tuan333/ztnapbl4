package com.example.ztna_server.service;

import com.example.ztna_server.dto.request.GrantRolePermissionRequest;
import com.example.ztna_server.dto.request.GrantUserPermissionRequest;
import com.example.ztna_server.dto.response.EffectiveAccessResponse;
import com.example.ztna_server.dto.response.PermissionResponse;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.entity.RolePermission;
import com.example.ztna_server.entity.SystemRole;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.UserPermission;
import com.example.ztna_server.entity.UserRole;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.enums.UserAction;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.RolePermissionRepository;
import com.example.ztna_server.repository.SystemRoleRepository;
import com.example.ztna_server.repository.UserPermissionRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.repository.UserRoleRepository;
import com.example.ztna_server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Phân quyền truy cập ứng dụng (Least Privilege).
 * User được vào 1 app khi có ÍT NHẤT 1 trong 2 nguồn còn hiệu lực:
 *   1) Quyền riêng của user          (bảng user_permitions)
 *   2) Quyền của role mà user thuộc  (bảng role_permitions + user_roles)
 * "Còn hiệu lực" = enabled và chưa hết hạn; app cũng phải đang enabled.
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final SystemRoleRepository systemRoleRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    // ───────────── Quyền theo role ─────────────

    @Transactional(readOnly = true)
    public List<PermissionResponse> getRolePermissions(UUID roleId) {
        List<RolePermission> list = (roleId == null)
                ? rolePermissionRepository.findAll()
                : rolePermissionRepository.findByRoleId(roleId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional
    public PermissionResponse grantToRole(GrantRolePermissionRequest request) {
        SystemRole role = systemRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        Resource resource = findResource(request.getResourceId());
        UserAction action = request.getAction() != null ? request.getAction() : UserAction.READ;

        if (rolePermissionRepository.existsByRoleIdAndResourceIdAndAction(role.getId(), resource.getId(), action)) {
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
        checkExpiry(request.getExpiresAt());

        RolePermission permission = RolePermission.builder()
                .role(role)
                .resource(resource)
                .action(action)
                .grantedBy(currentAdmin())
                .grantedAt(Instant.now())
                .expiresAt(request.getExpiresAt())
                .reason(request.getReason().trim())
                .enabled(true)
                .build();
        return toResponse(rolePermissionRepository.save(permission));
    }

    @Transactional
    public void revokeFromRole(UUID permissionId) {
        RolePermission permission = rolePermissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        rolePermissionRepository.delete(permission);
    }

    // ───────────── Quyền riêng của user ─────────────

    @Transactional(readOnly = true)
    public List<PermissionResponse> getUserPermissions(UUID userId) {
        List<UserPermission> list = (userId == null)
                ? userPermissionRepository.findAll()
                : userPermissionRepository.findByUserId(userId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional
    public PermissionResponse grantToUser(GrantUserPermissionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        Resource resource = findResource(request.getResourceId());

        if (userPermissionRepository.existsByUserIdAndResourceId(user.getId(), resource.getId())) {
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
        checkExpiry(request.getExpiresAt());

        UserPermission permission = UserPermission.builder()
                .user(user)
                .resource(resource)
                .grantedBy(currentAdmin())
                .grantedAt(Instant.now())
                .expiresAt(request.getExpiresAt())
                .reason(request.getReason().trim())
                .enabled(true)
                .build();
        return toResponse(userPermissionRepository.save(permission));
    }

    @Transactional
    public void revokeFromUser(UUID permissionId) {
        UserPermission permission = userPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        userPermissionRepository.delete(permission);
    }

    // ───────────── Quyền hiệu lực (dùng để quyết định ALLOW / DENY) ─────────────

    /** Danh sách app user đang được vào, kèm nguồn quyền. */
    @Transactional(readOnly = true)
    public List<EffectiveAccessResponse> getEffectiveAccess(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        Instant now = Instant.now();
        Map<UUID, EffectiveAccessResponse> result = new LinkedHashMap<>();

        // 1) Quyền riêng của user
        for (UserPermission p : userPermissionRepository.findByUserId(userId)) {
            if (isActive(p.isEnabled(), p.getExpiresAt(), now) && isResourceEnabled(p.getResource())) {
                addSource(result, p.getResource(), "USER");
            }
        }

        // 2) Quyền qua role: chỉ tính các role user đang thuộc và còn hiệu lực
        List<UUID> activeRoleIds = new ArrayList<>();
        for (UserRole ur : userRoleRepository.findByUserId(userId)) {
            if (isActive(ur.isEnabled(), ur.getExpiresAt(), now)) {
                activeRoleIds.add(ur.getRole().getId());
            }
        }
        if (!activeRoleIds.isEmpty()) {
            for (RolePermission p : rolePermissionRepository.findByRoleIdIn(activeRoleIds)) {
                if (isActive(p.isEnabled(), p.getExpiresAt(), now) && isResourceEnabled(p.getResource())) {
                    addSource(result, p.getResource(), "ROLE:" + p.getRole().getName());
                }
            }
        }
        return new ArrayList<>(result.values());
    }

    /** Id các app user đang được vào — ResourceService dùng để lọc danh sách. */
    @Transactional(readOnly = true)
    public List<UUID> getAccessibleResourceIds(UUID userId) {
        return getEffectiveAccess(userId).stream()
                .map(EffectiveAccessResponse::getResourceId)
                .toList();
    }

    // ───────────── Hàm phụ ─────────────

    private Resource findResource(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User currentAdmin() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
    }

    private void checkExpiry(Instant expiresAt) {
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            throw new AppException(ErrorCode.INVALID_EXPIRY);
        }
    }

    private boolean isActive(boolean enabled, Instant expiresAt, Instant now) {
        return enabled && (expiresAt == null || expiresAt.isAfter(now));
    }

    private boolean isResourceEnabled(Resource resource) {
        return Boolean.TRUE.equals(resource.getEnabled());
    }

    private void addSource(Map<UUID, EffectiveAccessResponse> result, Resource resource, String source) {
        EffectiveAccessResponse item = result.computeIfAbsent(resource.getId(), id ->
                EffectiveAccessResponse.builder()
                        .resourceId(id)
                        .resourceName(resource.getName())
                        .build());
        if (!item.getSources().contains(source)) {
            item.getSources().add(source);
        }
    }

    // Gọi trong @Transactional vì role/user/resource/grantedBy là LAZY
    private PermissionResponse toResponse(RolePermission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .type("ROLE")
                .roleId(p.getRole().getId())
                .roleName(p.getRole().getName())
                .resourceId(p.getResource().getId())
                .resourceName(p.getResource().getName())
                .action(p.getAction())
                .grantedBy(p.getGrantedBy().getUsername())
                .grantedAt(p.getGrantedAt())
                .expiresAt(p.getExpiresAt())
                .reason(p.getReason())
                .enabled(p.isEnabled())
                .active(isActive(p.isEnabled(), p.getExpiresAt(), Instant.now()))
                .build();
    }

    private PermissionResponse toResponse(UserPermission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .type("USER")
                .userId(p.getUser().getId())
                .username(p.getUser().getUsername())
                .resourceId(p.getResource().getId())
                .resourceName(p.getResource().getName())
                .grantedBy(p.getGrantedBy().getUsername())
                .grantedAt(p.getGrantedAt())
                .expiresAt(p.getExpiresAt())
                .reason(p.getReason())
                .enabled(p.isEnabled())
                .active(isActive(p.isEnabled(), p.getExpiresAt(), Instant.now()))
                .build();
    }
}
