package com.example.ztna_server.runtime.connection.agent;

import io.netty.channel.Channel;
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
    // Kênh Agent đã gửi RESOURCE_ACCESS. Lưu trực tiếp vì nhiều người dùng có thể dùng chung một Agent,
    // không tra ngược theo userId được.
    private Channel agentChannel;
}
