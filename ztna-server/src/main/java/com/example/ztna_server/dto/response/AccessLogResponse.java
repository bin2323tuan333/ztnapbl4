package com.example.ztna_server.dto.response;

import com.example.ztna_server.enums.Decision;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccessLogResponse {
    private UUID id;
    private UUID requestId;
    private UUID userId;
    private String username;
    private UUID resourceId;
    private String resourceName;
    private UUID connectorId;
    private String path;
    private String method;
    private String clientIp;
    private Decision decision;
    private String reason;
    private Instant createdAt;
}