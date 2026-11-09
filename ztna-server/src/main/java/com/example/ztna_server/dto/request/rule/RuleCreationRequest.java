package com.example.ztna_server.dto.request.rule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleCreationRequest {
    @NotBlank(message = "Tên Rule không được để trống")
    @Size(max = 100, message = "Tên Rule không được vượt quá 100 ký tự")
    private String name;
    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;
    private Boolean enabled;
    private Instant expiresAt;
    private List<UUID> userIds;
    private List<UUID> resourceIds;
}