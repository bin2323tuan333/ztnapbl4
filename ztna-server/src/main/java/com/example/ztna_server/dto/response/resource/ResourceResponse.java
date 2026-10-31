package com.example.ztna_server.dto.response.resource;

import com.example.ztna_server.enums.ResourceType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {
    private UUID id;
    private String name;
    private ResourceType type;
    private String description;
    private Boolean enabled;
}
