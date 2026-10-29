package com.example.ztna_server.controller;

import com.example.ztna_server.dto.response.ResourceResponse;
import com.example.ztna_server.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping("/myresources")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ResourceResponse>> getMyResources(@AuthenticationPrincipal String userIdStr) {
        if (userIdStr == null) {
            return ResponseEntity.status(401).build();
        }
        UUID userId = UUID.fromString(userIdStr);
        List<ResourceResponse> resources = resourceService.getResourcesByUserId(userId);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/resources")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceResponse>> getResources() {
        List<ResourceResponse> resources = resourceService.getResources();
        return ResponseEntity.ok(resources);
    }
}
