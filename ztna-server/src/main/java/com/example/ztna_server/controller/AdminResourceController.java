package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.ResourceRequest;
import com.example.ztna_server.dto.response.resource.AdminResourceDetailResponse;
import com.example.ztna_server.dto.response.resource.AdminResourceSummaryResponse;
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
@RequestMapping("/api/v1/admin/resources")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminResourceController {

    private final AdminResourceService adminResourceService;

    // GET /api/v1/admin/resources
    @GetMapping // Đã bỏ "/resources"
    public ResponseEntity<List<AdminResourceSummaryResponse>> getResources() {
        return ResponseEntity.ok(adminResourceService.getResources());
    }

    // GET /api/v1/admin/resources/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AdminResourceDetailResponse> getResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.getResource(id));
    }

    // POST /api/v1/admin/resources
    @PostMapping
    public ResponseEntity<AdminResourceDetailResponse> createResource(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminResourceService.createResource(request));
    }

    // PUT /api/v1/admin/resources/{id}
    @PutMapping("/{id}")
    public ResponseEntity<AdminResourceDetailResponse> updateResource(@PathVariable UUID id,
                                                                      @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(adminResourceService.updateResource(id, request));
    }

    // PATCH /api/v1/admin/resources/{id}/disable
    @PatchMapping("/{id}/disable")
    public ResponseEntity<AdminResourceDetailResponse> disableResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.setEnabled(id, false));
    }

    // PATCH /api/v1/admin/resources/{id}/enable
    @PatchMapping("/{id}/enable")
    public ResponseEntity<AdminResourceDetailResponse> enableResource(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResourceService.setEnabled(id, true));
    }

    // DELETE /api/v1/admin/resources/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id) {
        adminResourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
