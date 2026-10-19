package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.LoginRequest;
import com.example.ztna_server.dto.response.LoginResponse;
import com.example.ztna_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

     /**
     POST /api/auth/login
     Body: { "username": "admin", "password": "123" }
     Response: { "token": "eyJhbGc..." }
     **/
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
