package com.example.ztna_server.service;

import com.example.ztna_server.entity.User;
import com.example.ztna_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(String username, String rawPassword, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLoginAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public User updatePassword(User user, String newRawPassword) {
        user.setPassword(passwordEncoder.encode(newRawPassword));
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    @Transactional
    public User setEnabled(User user, boolean enabled) {
        user.setEnabled(enabled);
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }
}