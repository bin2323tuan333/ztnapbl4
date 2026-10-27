package com.example.ztna_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectorResponse {
    private UUID id;
    private String name;
    private boolean enabled;
    private boolean online;     // connector đang kết nối WebSocket tới server
    private long appCount;      // số app connector đang phụ trách
}
