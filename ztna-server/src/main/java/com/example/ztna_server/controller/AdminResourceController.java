package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.ResourceRequest;
import com.example.ztna_server.dto.response.AdminResourceResponse;
import com.example.ztna_server.dto.response.ConnectorResponse;
import com.example.ztna_server.service.AdminResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Quản lý ứng dụng nội bộ và xem connector — chỉ ADMIN gọi được (SecurityConfig chặn /admin/**).
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminResourceController {

    private final AdminResourceService adminResourceService;

    // GET /admin/resources — danh sách app (có địa chỉ nội bộ, connector)
    @GetMapping("/resources")
    public ResponseEntity<List<AdminResourceResponse>> getResources() {
        return ResponseEntity.ok(adminResourceService.getResources());
    }

    // GET /admin/resources/{id} — chi tiết 1 app
    @GetMapping("/resources/{id}")
    public ResponseEntity<AdminResourceResponse> getResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.getResource(id));
    }

    // POST /admin/resources — thêm app
    @PostMapping("/resources")
    public ResponseEntity<AdminResourceResponse> createResource(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminResourceService.createResource(request));
    }

    // PUT /admin/resources/{id} — sửa app (tên, mô tả, địa chỉ, connector)
    @PutMapping("/resources/{id}")
    public ResponseEntity<AdminResourceResponse> updateResource(@PathVariable UUID id,
                                                                @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(adminResourceService.updateResource(id, request));
    }

    // PATCH /admin/resources/{id}/disable — tạm tắt app (mọi user mất quyền vào ngay)
    @PatchMapping("/resources/{id}/disable")
    public ResponseEntity<AdminResourceResponse> disableResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.setEnabled(id, false));
    }

    // PATCH /admin/resources/{id}/enable — bật lại app
    @PatchMapping("/resources/{id}/enable")
    public ResponseEntity<AdminResourceResponse> enableResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.setEnabled(id, true));
    }

    // DELETE /admin/resources/{id} — xóa app (không còn quyền nào trỏ tới)
    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id) {
        adminResourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    // GET /admin/connectors — danh sách connector (để chọn khi thêm app)
    @GetMapping("/connectors")
    public ResponseEntity<List<ConnectorResponse>> getConnectors() {
        return ResponseEntity.ok(adminResourceService.getConnectors());
    }
}
