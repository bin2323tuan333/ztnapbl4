package com.example.ztna_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(nullable = false, name = "expired_at")
    private Instant expiredAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
