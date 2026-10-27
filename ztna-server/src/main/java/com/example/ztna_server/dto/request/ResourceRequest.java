package com.example.ztna_server.dto.request;

import com.example.ztna_server.enums.ResourceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // WEB / SSH / MYSQL — Agent dựa vào đây để mở trình duyệt, ssh hay mysql
    @NotNull(message = "Phải chọn loại ứng dụng")
    private ResourceType type;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    // Địa chỉ nội bộ mà Connector kết nối tới, vd 10.0.0.5
    @NotBlank(message = "Host không được để trống")
    @Size(max = 255, message = "Host tối đa 255 ký tự")
    private String host;

    @NotNull(message = "Port không được để trống")
    @Min(value = 1, message = "Port từ 1 đến 65535")
    @Max(value = 65535, message = "Port từ 1 đến 65535")
    private Integer port;

    // Connector phụ trách app này (bắt buộc)
    @NotNull(message = "Phải chọn connector")
    private UUID connectorId;

    // Không gửi khi tạo = bật; không gửi khi sửa = giữ nguyên
    private Boolean enabled;
}
