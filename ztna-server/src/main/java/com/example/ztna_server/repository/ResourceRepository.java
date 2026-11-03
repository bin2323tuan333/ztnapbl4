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


    @Query("""
    SELECT new com.example.ztna_server.dto.internal.ResourceAccess(
        r.id, r.name, r.type, r.description, r.enabled
    )
    FROM Resource r
    WHERE r.id IN (
        SELECT r2.id
        FROM Resource r2
        JOIN r2.rules rule
        JOIN rule.users u
        WHERE u.id = :userId
          AND u.enabled = true
          AND rule.enabled = true
    )
    AND r.enabled = true
    ORDER BY r.name ASC
    """)
    List<ResourceAccess> findAllByUserId(@Param("userId") UUID userId);
}
