package com.example.ztna_server.runtime.connection.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendingConnect {
    private String requestId;
    private String streamId;
    private String userId;
    private String connectorId;
    private String resourceId;
}
