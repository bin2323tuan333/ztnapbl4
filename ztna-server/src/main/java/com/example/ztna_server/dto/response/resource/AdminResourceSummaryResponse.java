package com.example.ztna_server.dto.response.resource;

import com.example.ztna_server.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResourceSummaryResponse {
    private UUID id;
    private String name;
    private ResourceType type;
    private String host;
    private Integer port;
    private Boolean enabled;

    private String connectorZone;
}
