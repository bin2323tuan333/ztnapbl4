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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connector_id")
    private Connector connector;

    @Column(name = "path", length = 500, nullable = false)
    private String path;

    @Column(name = "method", length = 10, nullable = false)
    private String method;

    @Column(name = "client_ip", nullable = false)
    private String clientIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", length = 5, nullable = false)
    private Decision decision;

    @Column(name = "reason", length = 255, nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
