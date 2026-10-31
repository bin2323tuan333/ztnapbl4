package com.example.ztna_server.dto.response.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleSummaryResponse {
    private UUID id;
    private String name;
    private String description;

    private int userCount;
    private int resourceCount;

    private Instant createdAt;
    private boolean enabled;
}