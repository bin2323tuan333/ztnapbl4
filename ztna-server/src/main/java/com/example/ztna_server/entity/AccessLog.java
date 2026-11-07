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

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_ip", nullable = false)
    private String userIp;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "resource_name", length = 100)
    private String resourceName;

    @Column(name = "resource_host", length = 100)
    private String resourceHost;

    @Column(name = "connector_id")
    private UUID connectorId;

    @Column(name = "connector_name")
    private String connectorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", length = 10, nullable = false)
    private Decision decision;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}