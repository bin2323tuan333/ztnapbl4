package com.example.ztna_server.service;

import com.example.ztna_server.dto.internal.UserInfo;
import com.example.ztna_server.dto.request.CreateUserRequest;
import com.example.ztna_server.dto.request.UpdatePasswordRequest;
import com.example.ztna_server.dto.response.UserResponse;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.mapper.UserMapper;
import com.example.ztna_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserInfo getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        return userMapper.toUserInfo(user);
    }

    public UserInfo getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        return userMapper.toUserInfo(user);
    }

    public UserInfo getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        return userMapper.toUserInfo(user);
    }


    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        User saveUser = userRepository.save(user);
        return userMapper.toUserResponse(saveUser);
    }

    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse getUserByID(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        return userMapper.toUserResponse(user);
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });
        userRepository.delete(user);
    }

    public void blockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        if (user.getEnabled()) {
            user.setEnabled(false);
            userRepository.save(user);
        }
    }

    public void unblockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        if (!user.getEnabled()) {
            user.setEnabled(false);
            userRepository.save(user);
        }
    }

    public void updateLastLogin(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        user.setUpdatedAt(Instant.now());
    }

    public UserResponse updatePassword(UpdatePasswordRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUpdatedAt(Instant.now());
        User newUser = userRepository.save(user);
        return userMapper.toUserResponse(newUser);
    }

}