package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.connector.ConnectorCreationRequest;
import com.example.ztna_server.dto.request.connector.ConnectorUpdateRequest;
import com.example.ztna_server.dto.response.connector.ConnectorDetailResponse;
import com.example.ztna_server.dto.response.connector.ConnectorNameResponse;
import com.example.ztna_server.dto.response.connector.ConnectorSummaryResponse;
import com.example.ztna_server.service.ConnectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/connectors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ConnectorController {

    private final ConnectorService connectorService;

    @GetMapping("/names")
    public ResponseEntity<List<ConnectorNameResponse>> getConnectorNames() {
        return ResponseEntity.ok(connectorService.getConnectorsName());
    }

    @GetMapping
    public ResponseEntity<List<ConnectorSummaryResponse>> getAllConnectors() {
        return ResponseEntity.ok(connectorService.getAllConnectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConnectorDetailResponse> getConnectorById(@PathVariable UUID id) {
        return ResponseEntity.ok(connectorService.getConnectorById(id));
    }

    @PostMapping
    public ResponseEntity<ConnectorDetailResponse> createConnector(@Valid @RequestBody ConnectorCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(connectorService.createConnector(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConnectorDetailResponse> updateConnector(
            @PathVariable UUID id,
            @Valid @RequestBody ConnectorUpdateRequest request) {
        return ResponseEntity.ok(connectorService.updateConnector(id, request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ConnectorDetailResponse> toggleConnectorStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(connectorService.toggleStatus(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnector(@PathVariable UUID id) {
        connectorService.deleteConnector(id);
        return ResponseEntity.noContent().build();
    }
}