package com.example.ztna_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemRoleResponse {
    private UUID id;
    private String name;
    private String displayName;
    private String description;
    private long memberCount;
    private Instant createdAt;
    private Instant updatedAt;
}
