package com.example.ztna_server.entity;

import com.example.ztna_server.enums.Action;
import com.example.ztna_server.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 10, nullable = false)
    private Action action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20, nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", length = 36, nullable = false)
    private String targetId;

    @Column(name = "details", nullable = false, columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
