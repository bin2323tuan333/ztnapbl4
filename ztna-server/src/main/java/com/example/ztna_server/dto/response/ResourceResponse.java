package com.example.ztna_server.dto.response;

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
    private String description;
    private Boolean enabled;
}
