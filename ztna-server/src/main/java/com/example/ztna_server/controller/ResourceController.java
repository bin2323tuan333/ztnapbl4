package com.example.ztna_server.controller;

import com.example.ztna_server.dto.response.ResourceResponse;
import com.example.ztna_server.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping("/resources")
    public ResponseEntity<List<ResourceResponse>> getResources() {
        List<ResourceResponse> response = resourceService.getResources();
        return ResponseEntity.ok(response);
    }
}
