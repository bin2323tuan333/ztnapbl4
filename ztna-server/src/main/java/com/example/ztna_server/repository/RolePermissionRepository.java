package com.example.ztna_server.repository;

import com.example.ztna_server.entity.RolePermission;
import com.example.ztna_server.enums.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    // Quyền của 1 role
    List<RolePermission> findByRoleId(UUID roleId);

    // Quyền của nhiều role cùng lúc (các role mà 1 user đang thuộc)
    List<RolePermission> findByRoleIdIn(Collection<UUID> roleIds);

    // Chống cấp trùng: cùng role, cùng app, cùng hành động
    boolean existsByRoleIdAndResourceIdAndAction(UUID roleId, UUID resourceId, UserAction action);
}
