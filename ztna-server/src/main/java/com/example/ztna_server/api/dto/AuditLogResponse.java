package com.example.ztna_server.api.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long actorId;
    private String actorUsername;
    private String action;         
    private String targetType;
    private Long targetId;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}