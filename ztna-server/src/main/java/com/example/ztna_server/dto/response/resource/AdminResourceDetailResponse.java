package com.example.ztna_server.dto.response.resource;

import com.example.ztna_server.enums.ResourceType;
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
public class AdminResourceDetailResponse {
    private UUID id;
    private String name;
    private ResourceType type;
    private String description;
    private String host;
    private Integer port;
    private Boolean enabled;

    private UUID connectorId;
    private String connectorZone;

    private Instant createdAt;
    private Instant updatedAt;
}
