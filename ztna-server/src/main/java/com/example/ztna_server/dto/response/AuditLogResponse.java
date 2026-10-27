package com.example.ztna_server.dto.response;

import com.example.ztna_server.enums.TargetType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponse {
    private UUID id;
    private UUID actorId;
    private String actorUsername;
    private String action;
    private TargetType targetType;
    private String targetId;
    private String details;
    private String ipAddress;
    private Instant createdAt;
}