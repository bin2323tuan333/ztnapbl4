package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.CreateUserRequest;
import com.example.ztna_server.dto.request.UpdatePasswordRequest;
import com.example.ztna_server.dto.request.UpdateUserRequest;
import com.example.ztna_server.dto.response.UserResponse;
import com.example.ztna_server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Quản lý user — chỉ ADMIN gọi được (SecurityConfig chặn /admin/**).
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /admin/users — danh sách user
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    // GET /admin/users/{id} — chi tiết 1 user
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserByID(id));
    }

    // POST /admin/users — tạo user
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    // PUT /admin/users/{id} — sửa họ tên, email, role
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // PATCH /admin/users/{id}/block — khóa tài khoản
    @PatchMapping("/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable UUID id) {
        userService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /admin/users/{id}/unblock — mở khóa
    @PatchMapping("/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable UUID id) {
        userService.unblockUser(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /admin/users/{id}/password — admin đặt lại mật khẩu
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID id,
                                              @Valid @RequestBody UpdatePasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    // DELETE /admin/users/{id} — xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
