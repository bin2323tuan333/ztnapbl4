package com.example.ztna_server.dto.response;

import com.example.ztna_server.enums.Decision;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccessLogResponse {
    private UUID id;
    private UUID userId;
    private String userIp;
    private String userName;
    private UUID resourceId;
    private String resourceName;
    private String resourceHost;
    private UUID connectorId;
    private String connectorName;
    private Decision decision;
    private String reason;
    private Instant createdAt;
}