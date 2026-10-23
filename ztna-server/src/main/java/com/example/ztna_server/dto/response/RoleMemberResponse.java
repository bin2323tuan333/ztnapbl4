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
public class RoleMemberResponse {
    private UUID userId;
    private String username;
    private String fullName;
    private UUID roleId;
    private String roleName;
    private String grantedBy;   // username của admin đã gán
    private Instant grantedAt;
    private Instant expiresAt;
    private String reason;
    private boolean enabled;
}
