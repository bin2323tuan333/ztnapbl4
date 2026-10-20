package com.example.ztna_server.dto.request;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    private String username;
    private String fullName;
    private String password;
    private String email;
}
