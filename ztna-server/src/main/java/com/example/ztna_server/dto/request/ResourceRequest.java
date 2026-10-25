package com.example.ztna_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequest {
    @NotBlank(message = "Tên ứng dụng không được để trống")
    @Size(max = 100, message = "Tên ứng dụng tối đa 100 ký tự")
    private String name;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    // Địa chỉ nội bộ của app, phải có host và port. Vd: http://10.0.0.5:8080, tcp://db.internal:3306
    @NotBlank(message = "Địa chỉ backend không được để trống")
    @Size(max = 500, message = "Địa chỉ backend tối đa 500 ký tự")
    private String backendUrl;

    // Connector phụ trách app này (không gửi = chưa gán connector)
    private UUID connectorId;

    // Không gửi khi tạo = bật; không gửi khi sửa = giữ nguyên
    private Boolean enabled;
}
