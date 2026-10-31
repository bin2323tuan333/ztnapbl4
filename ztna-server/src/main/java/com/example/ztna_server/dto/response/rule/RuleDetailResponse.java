package com.example.ztna_server.dto.response.rule;

import com.example.ztna_server.dto.internal.ResourceInfo;
import com.example.ztna_server.dto.internal.UserInfo;
import com.example.ztna_server.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private Instant createdAt;
    private boolean enabled;

    private List<UserInfo> users;

    private List<ResourceInfo> resources;
}