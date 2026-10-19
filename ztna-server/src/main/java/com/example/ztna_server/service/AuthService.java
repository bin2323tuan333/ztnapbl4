package com.example.ztna_server.service;

import com.example.ztna_server.dto.request.LoginRequest;
import com.example.ztna_server.dto.response.LoginResponse;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public LoginResponse login(LoginRequest request) {
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu");
        }

        if (!user.getEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        userService.updateLastLogin(user);

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().getRole().getName()
        );

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .build();

        return response;
    }
}