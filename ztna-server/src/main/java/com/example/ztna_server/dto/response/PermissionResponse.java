package com.example.ztna_server.dto.response;

import com.example.ztna_server.enums.UserAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Dùng chung cho quyền theo role và quyền riêng của user.
 * type = "ROLE" thì có roleId/roleName; type = "USER" thì có userId/username.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private UUID id;
    private String type;

    private UUID roleId;
    private String roleName;

    private UUID userId;
    private String username;

    private UUID resourceId;
    private String resourceName;
    private UserAction action;

    private String grantedBy;
    private Instant grantedAt;
    private Instant expiresAt;
    private String reason;
    private boolean enabled;

    // true nếu quyền đang có hiệu lực (enabled và chưa hết hạn)
    private boolean active;
}
