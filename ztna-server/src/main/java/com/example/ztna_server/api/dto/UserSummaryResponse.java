
package com.example.ztna_server.api.dto;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        Boolean enabled,
        Long roleId,
        String roleName,
        String roleDisplayName
) {}
