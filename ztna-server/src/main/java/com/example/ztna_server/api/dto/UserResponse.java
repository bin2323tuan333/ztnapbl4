package com.example.ztna_server.api.dto;

import com.example.ztna_server.entity.enums.SystemRoleName;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Boolean enabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    private Long systemRoleId;
    private SystemRoleName systemRoleName;
    private String systemRoleDisplayName;
}