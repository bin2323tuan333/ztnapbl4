
package com.example.ztna_server.api.dto;

import com.example.ztna_server.entity.enums.SystemRoleName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SystemRoleRequest(
        @NotNull
        SystemRoleName name,

        @Size(max = 255)
        String description,

        @Size(max = 100)
        String displayName
) {}
