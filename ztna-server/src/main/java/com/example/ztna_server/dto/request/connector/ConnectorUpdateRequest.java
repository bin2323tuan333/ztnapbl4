package com.example.ztna_server.dto.request.connector;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectorUpdateRequest {
    @NotBlank(message = "Tên Connector không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Connection String không được để trống")
    private String connectionString;

    private boolean enabled;
}