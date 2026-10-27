package com.example.ztna_server.repository;

import com.example.ztna_server.entity.Resource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
