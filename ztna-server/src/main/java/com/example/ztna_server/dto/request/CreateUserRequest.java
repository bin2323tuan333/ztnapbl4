package com.example.ztna_server.dto.request;

import com.example.ztna_server.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username dài từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @Email(message = "Email không hợp lệ")
    private String email;

    // Không gửi thì mặc định là USER
    private Role role;
}
