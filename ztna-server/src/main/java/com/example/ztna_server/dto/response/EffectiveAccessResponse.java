package com.example.ztna_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 1 ứng dụng mà user đang được vào, kèm nguồn gốc của quyền.
 * sources ví dụ: ["USER", "ROLE:DEV"]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EffectiveAccessResponse {
    private UUID resourceId;
    private String resourceName;

    @Builder.Default
    private List<String> sources = new ArrayList<>();
}
