package com.example.ztna_server.repository;

import com.example.ztna_server.entity.AuditLog;
import com.example.ztna_server.enums.Action;
import com.example.ztna_server.enums.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:actorUsername IS NULL OR LOWER(a.actorUsername) LIKE LOWER(CONCAT('%', :actorUsername, '%')))
          AND (:action IS NULL OR a.action = :action)
          AND (:targetType IS NULL OR a.targetType = :targetType)
          AND (:from IS NULL OR a.createdAt >= :from)
          AND (:to IS NULL OR a.createdAt <= :to)
        ORDER BY a.createdAt DESC
        """)
    Page<AuditLog> search(
            @Param("actorUsername") String actorUsername,
            @Param("action") Action action,
            @Param("targetType") TargetType targetType,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}