package com.example.ztna_server.entity;

import com.example.ztna_server.enums.UserAction;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "role_permitions")
public class RolePermition {
    @Id
    @GeneratedValue
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private SystemRole role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserAction action;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "granted_by", nullable = false)
    private User grantedBy;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "reason", length = 255, nullable = false)
    private String reason;

    @Column(nullable = false)
    private boolean enabled = true;
}
