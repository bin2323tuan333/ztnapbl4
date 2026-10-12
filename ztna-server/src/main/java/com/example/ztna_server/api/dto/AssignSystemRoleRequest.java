
package com.example.ztna_server.api.dto;

import jakarta.validation.constraints.NotNull;

public record AssignSystemRoleRequest(
        @NotNull Long userId,
        @NotNull Long roleId
) {}
