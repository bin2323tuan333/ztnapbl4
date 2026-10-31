package com.example.ztna_server.dto.response.connector;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorSummaryResponse {
    private UUID id;
    private String name;
    private boolean enabled;
    private Instant createdAt;
}