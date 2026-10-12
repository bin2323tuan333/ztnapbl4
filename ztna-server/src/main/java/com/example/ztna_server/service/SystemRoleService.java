package com.example.ztna_server.service;

import com.example.ztna_server.api.dto.AssignSystemRoleRequest;
import com.example.ztna_server.api.dto.SystemRoleRequest;
import com.example.ztna_server.api.dto.SystemRoleResponse;
import com.example.ztna_server.api.dto.UserSummaryResponse;
import com.example.ztna_server.entity.SystemRole;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.enums.AuditAction;
import com.example.ztna_server.entity.enums.SystemRoleName;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.exception.ErrorCode;
import com.example.ztna_server.repo.SystemRoleRepository;
import com.example.ztna_server.repo.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemRoleService {

    private final SystemRoleRepository systemRoleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    // ==================== CREATE ====================

    @Transactional
    public SystemRoleResponse create(
            SystemRoleRequest request,
            Long actorId,
            String actorUsername) {

        if (systemRoleRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.SYSTEM_ROLE_EXISTED);
        }

        SystemRole role = SystemRole.builder()
                .name(request.name())
                .description(request.description())
                .displayName(request.displayName())
                .build();

        role = systemRoleRepository.save(role);

        auditLogService.log(
                actorId,
                actorUsername,
                AuditAction.CREATE_SYSTEM_ROLE,
                "SYSTEM_ROLE",
                role.getId(),
                "Tạo SystemRole: " + role.getName()
        );

        return toResponse(role);
    }

    // ==================== READ ====================

    @Transactional(readOnly = true)
    public SystemRoleResponse getById(Long roleId) {
        return toResponse(findRole(roleId));
    }

    @Transactional(readOnly = true)
    public Page<SystemRoleResponse> getAll(Pageable pageable) {
        return systemRoleRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ==================== UPDATE ====================

    @Transactional
    public SystemRoleResponse update(
            Long roleId,
            SystemRoleRequest request,
            Long actorId,
            String actorUsername) {

        SystemRole role = findRole(roleId);

        if (systemRoleRepository.existsByNameAndIdNot(
                request.name(), roleId)) {
            throw new AppException(ErrorCode.SYSTEM_ROLE_EXISTED);
        }

        SystemRoleName oldName = role.getName();

        role.setName(request.name());
        role.setDescription(request.description());
        role.setDisplayName(request.displayName());

        role = systemRoleRepository.save(role);

        auditLogService.log(
                actorId,
                actorUsername,
                AuditAction.UPDATE_SYSTEM_ROLE,
                "SYSTEM_ROLE",
                role.getId(),
                "Cập nhật SystemRole từ " + oldName
                        + " thành " + role.getName()
        );

        return toResponse(role);
    }

    // ==================== DELETE ====================

    @Transactional
    public void delete(
            Long roleId,
            Long actorId,
            String actorUsername) {

        SystemRole role = findRole(roleId);

        if (userRepository.existsBySystemRole_Id(roleId)) {
            throw new AppException(ErrorCode.SYSTEM_ROLE_IN_USE);
        }

        String roleName = role.getName().name();

        systemRoleRepository.delete(role);

        auditLogService.log(
                actorId,
                actorUsername,
                AuditAction.DELETE_SYSTEM_ROLE,
                "SYSTEM_ROLE",
                roleId,
                "Xóa SystemRole: " + roleName
        );
    }

    // ==================== ASSIGN ROLE ====================

    @Transactional
    public UserSummaryResponse assignRole(
            AssignSystemRoleRequest request,
            Long actorId,
            String actorUsername) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        SystemRole newRole = findRole(request.roleId());

        String oldRoleName = user.getSystemRole() == null
                ? "NONE"
                : user.getSystemRole().getName().name();

        user.setSystemRole(newRole);
        user = userRepository.save(user);

        auditLogService.log(
                actorId,
                actorUsername,
                AuditAction.ASSIGN_SYSTEM_ROLE,
                "USER",
                user.getId(),
                "Đổi role của user " + user.getUsername()
                        + " từ " + oldRoleName
                        + " thành " + newRole.getName()
        );

        return toUserResponse(user);
    }

    // ==================== LIST USERS BY ROLE ====================

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getUsersByRole(
            Long roleId,
            Pageable pageable) {

        findRole(roleId);

        return userRepository.findBySystemRole_Id(roleId, pageable)
                .map(this::toUserResponse);
    }

    // ==================== HELPERS ====================

    private SystemRole findRole(Long roleId) {
        return systemRoleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_ROLE_NOT_EXISTED));
    }

    private SystemRoleResponse toResponse(SystemRole role) {
        return new SystemRoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getDisplayName(),
                role.getCreatedAt()
        );
    }

    private UserSummaryResponse toUserResponse(User user) {
        SystemRole role = user.getSystemRole();

        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getEnabled(),
                role == null ? null : role.getId(),
                role == null ? null : role.getName().name(),
                role == null ? null : role.getDisplayName()
        );
    }
}