package com.example.ztna_server.service;

import com.example.ztna_server.entity.User;
import com.example.ztna_server.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public Optional<User> findById(Long id) {
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
                .passwordHash(passwordEncoder.encode(rawPassword))
                .email(email)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public User updatePassword(User user, String newRawPassword) {
        user.setPasswordHash(passwordEncoder.encode(newRawPassword));
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User setEnabled(User user, boolean enabled) {
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}