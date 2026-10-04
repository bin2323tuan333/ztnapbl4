package com.example.ztna_server.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;

    private Long userId;

    private String username;
}