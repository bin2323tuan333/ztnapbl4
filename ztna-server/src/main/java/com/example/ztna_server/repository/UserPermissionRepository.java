package com.example.ztna_server.repository;

import com.example.ztna_server.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {
    // Quyền riêng của 1 user
    List<UserPermission> findByUserId(UUID userId);

    // Chống cấp trùng: cùng user, cùng app
    boolean existsByUserIdAndResourceId(UUID userId, UUID resourceId);
}
