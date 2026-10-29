package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.LoginRequest;
import com.example.ztna_server.dto.response.AuthResponse;
import com.example.ztna_server.dto.response.LoginResponse;
import com.example.ztna_server.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {
        AuthResponse authResponse = authService.login(request);

        Cookie refreshTokenCookie = new Cookie("refresh_token", authResponse.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
//        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/api/v1/auth");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        httpResponse.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không tìm thấy Refresh Token");
        }
        LoginResponse response = authService.refresh(refreshToken);
        return ResponseEntity.ok(response);
    }
}
