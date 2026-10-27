package com.example.ztna_server.dto.internal;

import lombok.Builder;
import lombok.Setter;

import java.util.UUID;

@Setter
@Builder
public class UserInfo {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
}
