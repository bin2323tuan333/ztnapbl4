package com.example.ztna_server.entity;


import com.example.ztna_server.enums.Decision;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;                 // denormalize

    @Column(name = "resource_id")
    private UUID resourceId;                 // null nếu DENY trước khi biết resource

    @Column(name = "resource_name", length = 100)
    private String resourceName;

    @Column(name = "connector_id")
    private UUID connectorId;

    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "method", length = 10)
    private String method;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", length = 10, nullable = false)
    private Decision decision;               // ALLOW / DENY

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}