package com.example.ztna_server.dto.response;

import com.example.ztna_server.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private Boolean enabled;
    private Instant lastLoginAt;
    private Instant createdAt;
}
