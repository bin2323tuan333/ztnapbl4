package com.example.ztna_server.dto.response.connector;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorNameResponse {
    private UUID id;
    private String name;
}