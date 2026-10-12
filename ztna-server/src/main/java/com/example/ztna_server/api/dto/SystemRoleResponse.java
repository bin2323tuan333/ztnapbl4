
package com.example.ztna_server.api.dto;

import com.example.ztna_server.entity.enums.SystemRoleName;
import java.time.Instant;

public record SystemRoleResponse(
        Long id,
        SystemRoleName name,
        String description,
        String displayName,
        Instant createdAt
) {}
