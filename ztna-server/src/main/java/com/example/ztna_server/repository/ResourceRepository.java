package com.example.ztna_server.repository;

import com.example.ztna_server.dto.internal.ResourceAccess;
import com.example.ztna_server.entity.Resource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    // Dùng cho RoutingManager: app đang bật thuộc connector đang bật
    @EntityGraph(attributePaths = {"connector"})
    List<Resource> findAllByEnabledTrueAndConnectorEnabledTrue();

    @EntityGraph(attributePaths = {"connector"})
    List<Resource> findAllByOrderByNameAsc();

    // Dùng cho quản lý app: kiểm tra trùng tên, đếm app của connector
    Optional<Resource> findByName(String name);
    boolean existsByName(String name);
    long countByConnectorId(UUID connectorId);

//    @Query("""
//    SELECT new com.example.ztna_server.dto.internal.ResourceAccess(
//        r.id, r.name, r.type, r.description, r.enabled
//    )
//    FROM Resource r
//    WHERE r.id IN (
//        SELECT up.resourceId
//        FROM UserPermition up
//        WHERE up.userId = :userId
//            AND up.enabled = true
//            AND (up.expiresAt IS NULL OR up.expiresAt > CURRENT_TIMESTAMP)
//
//        UNION
//
//        SELECT rp.resourceId
//        FROM UserRole ur
//        JOIN RolePermition rp ON ur.roleId = rp.roleId
//        WHERE ur.userId = :userId
//            AND ur.enabled = true
//            AND (ur.expiresAt IS NULL OR ur.expiresAt > CURRENT_TIMESTAMP)
//            AND rp.enabled = true
//            AND (rp.expiresAt IS NULL OR rp.expiresAt > CURRENT_TIMESTAMP)
//    )
//    ORDER BY r.name ASC
//    """)
//    List<ResourceAccess> findAllByUserId(@Param("userId") UUID userId);
}
