package com.example.ztna_server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "connectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connector {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "connection_string", nullable = false, columnDefinition = "TEXT")
    private String connectionString;

    @OneToMany(mappedBy = "connector", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
