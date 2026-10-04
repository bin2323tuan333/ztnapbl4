package com.example.ztna_server.service;

import com.example.ztna_server.api.dto.LoginRequest;
import com.example.ztna_server.api.dto.LoginResponse;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;


    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (Boolean.FALSE.equals(user.getEnabled())) {
                throw new DisabledException("Account is disabled");
            }

            userService.updateLastLogin(user);

            String token = jwtService.generateAccessToken(user);

            return LoginResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .build();

        } catch (DisabledException e) {
            throw e;
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}