package com.example.ztna_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemRoleRequest {
    // Mã role, viết hoa không dấu, vd: DEV, KE_TOAN
    @NotBlank(message = "Mã role không được để trống")
    @Size(max = 30, message = "Mã role tối đa 30 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã role chỉ gồm chữ in hoa, số và dấu _")
    private String name;

    // Tên hiển thị, vd: "Lập trình viên"
    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(max = 50, message = "Tên hiển thị tối đa 50 ký tự")
    private String displayName;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;
}
