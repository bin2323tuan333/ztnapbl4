package com.example.ztna_server.controller;

import com.example.ztna_server.dto.response.UserResponse;
import com.example.ztna_server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(userService.getUserByID(userId));
    }

//    // Tự cập nhật thông tin (Chỉ được sửa tên, sđt, avatar - DTO không có trường role)
//    @PutMapping("/me")
//    public ResponseEntity<UserResponse> updateMyProfile(
//            @AuthenticationPrincipal String userIdStr,
//            @Valid @RequestBody UpdateProfileRequest request) {
//        UUID userId = UUID.fromString(userIdStr);
//        return ResponseEntity.ok(userService.updateProfile(userId, request));
//    }

//    // Tự đổi mật khẩu
//    @PatchMapping("/me/password")
//    public ResponseEntity<Void> changeMyPassword(
//            @AuthenticationPrincipal String userIdStr,
//            @Valid @RequestBody ChangePasswordRequest request) {
//        UUID userId = UUID.fromString(userIdStr);
//        userService.changePassword(userId, request);
//        return ResponseEntity.noContent().build();
//    }
}
