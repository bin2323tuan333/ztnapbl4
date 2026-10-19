package com.example.ztna_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "system_roles")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SystemRole {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 30, nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "display_name", length = 50, nullable = false)
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
