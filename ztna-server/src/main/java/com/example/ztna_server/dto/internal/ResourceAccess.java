package com.example.ztna_server.dto.internal;

import com.example.ztna_server.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAccess {
    private UUID id;
    private String name;
    private ResourceType type;
    private String description;
    private Boolean enabled;
}
