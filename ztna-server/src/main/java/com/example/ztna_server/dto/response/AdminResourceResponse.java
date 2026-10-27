package com.example.ztna_server.dto.response;

import com.example.ztna_server.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Thông tin app cho trang quản trị — có địa chỉ nội bộ (host, port).
 * Không dùng cho user thường: user chỉ nhận ResourceResponse (không lộ địa chỉ nội bộ).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResourceResponse {
    private UUID id;
    private String name;
    private ResourceType type;
    private String description;
    private String host;
    private int port;
    private Boolean enabled;

    private UUID connectorId;
    private String connectorName;
    private boolean connectorOnline;

    private Instant createdAt;
    private Instant updatedAt;
}
