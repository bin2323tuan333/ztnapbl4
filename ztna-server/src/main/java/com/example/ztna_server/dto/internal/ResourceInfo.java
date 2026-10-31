package com.example.ztna_server.dto.internal;

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

public class ResourceInfo {
    private UUID id;
    private String name;
    private ResourceType type;
}
