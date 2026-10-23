package com.example.ztna_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class GrantUserPermissionRequest {
    @NotNull(message = "Phải chọn user")
    private UUID userId;

    @NotNull(message = "Phải chọn ứng dụng")
    private UUID resourceId;

    @NotBlank(message = "Phải ghi lý do cấp quyền")
    @Size(max = 255, message = "Lý do tối đa 255 ký tự")
    private String reason;

    // Không gửi = không hết hạn. Định dạng: "2026-12-31T23:59:59Z"
    private Instant expiresAt;
}
