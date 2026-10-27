package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.AssignRoleRequest;
import com.example.ztna_server.dto.request.SystemRoleRequest;
import com.example.ztna_server.dto.response.RoleMemberResponse;
import com.example.ztna_server.dto.response.SystemRoleResponse;
import com.example.ztna_server.service.SystemRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Quản lý role nghiệp vụ — chỉ ADMIN gọi được (SecurityConfig chặn /admin/**).
 */
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class SystemRoleController {

    private final SystemRoleService systemRoleService;

    // GET /admin/roles — danh sách role (kèm số thành viên)
    @GetMapping
    public ResponseEntity<List<SystemRoleResponse>> getRoles() {
        return ResponseEntity.ok(systemRoleService.getRoles());
    }

    // GET /admin/roles/{id} — chi tiết 1 role
    @GetMapping("/{id}")
    public ResponseEntity<SystemRoleResponse> getRole(@PathVariable UUID id) {
        return ResponseEntity.ok(systemRoleService.getRole(id));
    }

    // POST /admin/roles — tạo role
    @PostMapping
    public ResponseEntity<SystemRoleResponse> createRole(@Valid @RequestBody SystemRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(systemRoleService.createRole(request));
    }

    // PUT /admin/roles/{id} — sửa role
    @PutMapping("/{id}")
    public ResponseEntity<SystemRoleResponse> updateRole(@PathVariable UUID id,
                                                         @Valid @RequestBody SystemRoleRequest request) {
        return ResponseEntity.ok(systemRoleService.updateRole(id, request));
    }

    // DELETE /admin/roles/{id} — xóa role (role phải không còn thành viên)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        systemRoleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    // GET /admin/roles/{id}/users — danh sách user thuộc role
    @GetMapping("/{id}/users")
    public ResponseEntity<List<RoleMemberResponse>> getMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(systemRoleService.getMembers(id));
    }

    // POST /admin/roles/{id}/users — gán user vào role
    @PostMapping("/{id}/users")
    public ResponseEntity<RoleMemberResponse> assignUser(@PathVariable UUID id,
                                                         @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(systemRoleService.assignUser(id, request));
    }

    // DELETE /admin/roles/{id}/users/{userId} — gỡ user khỏi role
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<Void> removeUser(@PathVariable UUID id, @PathVariable UUID userId) {
        systemRoleService.removeUser(id, userId);
        return ResponseEntity.noContent().build();
    }

    // GET /admin/roles/of-user/{userId} — các role của 1 user
    @GetMapping("/of-user/{userId}")
    public ResponseEntity<List<RoleMemberResponse>> getRolesOfUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(systemRoleService.getRolesOfUser(userId));
    }
}
