package com.example.ztna_server.repository;

import com.example.ztna_server.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID>  {
    Optional<UserRole> getByUserId(UUID userId);

    // Thành viên của 1 role
    List<UserRole> findByRoleId(UUID roleId);
    long countByRoleId(UUID roleId);

    // Các role của 1 user
    List<UserRole> findByUserId(UUID userId);

    // Kiểm tra / tìm 1 cặp user - role
    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);
    Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);
}
