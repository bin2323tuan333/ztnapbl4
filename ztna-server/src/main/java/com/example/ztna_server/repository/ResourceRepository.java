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

    // Dùng cho quản lý app: kiểm tra trùng tên, đếm app của connector
    Optional<Resource> findByName(String name);
    boolean existsByName(String name);
    long countByConnectorId(UUID connectorId);

    @Query(value = """
        SELECT DISTINCT r.id, r.name, r.type, r.description, r.enabled
        FROM resources r
        JOIN (
            SELECT up.resource_id
            FROM user_permitions as up
            WHERE up.user_id = :userId
                AND up.enabled = true
                AND (up.expires_at IS NULL OR up.expires_at > NOW())
                
            UNION 
                
            SELECT rp.resource_id
            FROM user_roles AS ur 
            JOIN role_permitions AS rp ON ur.role_id = rp.role_id
            WHERE ur.user_id = :userId
                AND ur.enabled = true
                AND (ur.expires_at IS NULL OR ur.expires_at > NOW())
                AND rp.enabled = true
                AND (rp.expires_at IS NULL OR rp.expires_at > NOW())
        ) AS allowed_resource ON r.id = allowed_resource.resource_id
        ORDER BY r.name ASC;
    """, nativeQuery = true)
    List<ResourceAccess> findAllByUserId(@Param("userId") UUID userId);
}
