package com.example.ztna_server.dto.response.connector;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorDetailResponse {
    private UUID id;
    private String name;
    private String connectionString;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}