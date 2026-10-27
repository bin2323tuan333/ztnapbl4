package com.example.ztna_server.repository;

import com.example.ztna_server.entity.AccessLog;
import com.example.ztna_server.enums.Decision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {

    @Query("""
        SELECT a FROM AccessLog a
        WHERE (:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:resourceName IS NULL OR LOWER(a.resourceName) LIKE LOWER(CONCAT('%', :resourceName, '%')))
          AND (:decision IS NULL OR a.decision = :decision)
          AND (:from IS NULL OR a.createdAt >= :from)
          AND (:to IS NULL OR a.createdAt <= :to)
        ORDER BY a.createdAt DESC
        """)
    Page<AccessLog> search(
            @Param("username") String username,
            @Param("resourceName") String resourceName,
            @Param("decision") Decision decision,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}