package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.ResourceRequest;
import com.example.ztna_server.dto.response.AdminResourceResponse;
import com.example.ztna_server.dto.response.ConnectorResponse;
import com.example.ztna_server.service.AdminResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/resources") // Thêm versioning
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminResourceController {

    private final AdminResourceService adminResourceService;

    // GET /api/v1/admin/resources
    @GetMapping // Đã bỏ "/resources"
    public ResponseEntity<List<AdminResourceResponse>> getResources() {
        return ResponseEntity.ok(adminResourceService.getResources());
    }

    // GET /api/v1/admin/resources/{id}
    @GetMapping("/{id}") // Đã bỏ "/resources"
    public ResponseEntity<AdminResourceResponse> getResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.getResource(id));
    }

    // POST /api/v1/admin/resources
    @PostMapping // Đã bỏ "/resources"
    public ResponseEntity<AdminResourceResponse> createResource(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminResourceService.createResource(request));
    }

    // PUT /api/v1/admin/resources/{id}
    @PutMapping("/{id}") // Đã bỏ "/resources"
    public ResponseEntity<AdminResourceResponse> updateResource(@PathVariable UUID id,
                                                                @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(adminResourceService.updateResource(id, request));
    }

    // PATCH /api/v1/admin/resources/{id}/disable
    @PatchMapping("/{id}/disable") // Đã bỏ "/resources"
    public ResponseEntity<AdminResourceResponse> disableResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.setEnabled(id, false));
    }

    // PATCH /api/v1/admin/resources/{id}/enable
    @PatchMapping("/{id}/enable") // Đã bỏ "/resources"
    public ResponseEntity<AdminResourceResponse> enableResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.setEnabled(id, true));
    }

    // DELETE /api/v1/admin/resources/{id}
    @DeleteMapping("/{id}") // Đã bỏ "/resources"
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id) {
        adminResourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
