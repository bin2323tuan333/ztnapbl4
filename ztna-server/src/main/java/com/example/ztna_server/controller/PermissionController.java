package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.GrantRolePermissionRequest;
import com.example.ztna_server.dto.request.GrantUserPermissionRequest;
import com.example.ztna_server.dto.response.EffectiveAccessResponse;
import com.example.ztna_server.dto.response.PermissionResponse;
import com.example.ztna_server.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Phân quyền truy cập ứng dụng — chỉ ADMIN gọi được (SecurityConfig chặn /admin/**).
 */
@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // GET /admin/permissions/roles?roleId=... — quyền theo role (bỏ roleId = xem tất cả)
    @GetMapping("/roles")
    public ResponseEntity<List<PermissionResponse>> getRolePermissions(
            @RequestParam(required = false) UUID roleId) {
        return ResponseEntity.ok(permissionService.getRolePermissions(roleId));
    }

    // POST /admin/permissions/roles — cấp quyền vào app cho role
    @PostMapping("/roles")
    public ResponseEntity<PermissionResponse> grantToRole(@Valid @RequestBody GrantRolePermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.grantToRole(request));
    }

    // DELETE /admin/permissions/roles/{id} — thu hồi quyền của role
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> revokeFromRole(@PathVariable UUID id) {
        permissionService.revokeFromRole(id);
        return ResponseEntity.noContent().build();
    }

    // GET /admin/permissions/users?userId=... — quyền riêng của user (bỏ userId = xem tất cả)
    @GetMapping("/users")
    public ResponseEntity<List<PermissionResponse>> getUserPermissions(
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(permissionService.getUserPermissions(userId));
    }

    // POST /admin/permissions/users — cấp quyền riêng vào app cho 1 user
    @PostMapping("/users")
    public ResponseEntity<PermissionResponse> grantToUser(@Valid @RequestBody GrantUserPermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.grantToUser(request));
    }

    // DELETE /admin/permissions/users/{id} — thu hồi quyền riêng
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> revokeFromUser(@PathVariable UUID id) {
        permissionService.revokeFromUser(id);
        return ResponseEntity.noContent().build();
    }

    // GET /admin/permissions/effective/{userId} — các app user đang thực sự được vào
    @GetMapping("/effective/{userId}")
    public ResponseEntity<List<EffectiveAccessResponse>> getEffectiveAccess(@PathVariable UUID userId) {
        return ResponseEntity.ok(permissionService.getEffectiveAccess(userId));
    }
}
