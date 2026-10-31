package com.example.ztna_server.service;

import com.example.ztna_server.dto.request.connector.ConnectorCreationRequest;
import com.example.ztna_server.dto.request.connector.ConnectorUpdateRequest;
import com.example.ztna_server.dto.response.connector.ConnectorDetailResponse;
import com.example.ztna_server.dto.response.connector.ConnectorNameResponse;
import com.example.ztna_server.dto.response.connector.ConnectorSummaryResponse;
import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.enums.TargetType;
import com.example.ztna_server.mapper.ConnectorMapper;
import com.example.ztna_server.repository.ConnectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectorService {

    private final ConnectorRepository connectorRepository;
    private final ConnectorMapper connectorMapper;
    private final AuditService auditService;  

    // --- GET ---

    @Transactional(readOnly = true)
    public List<ConnectorNameResponse> getConnectorsName() {
        return connectorRepository.findAll().stream()
                .map(connectorMapper::toNameResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConnectorSummaryResponse> getAllConnectors() {
        return connectorRepository.findAll().stream()
                .map(connectorMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConnectorDetailResponse getConnectorById(UUID id) {
        Connector connector = connectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector không tồn tại"));
        return connectorMapper.toDetailResponse(connector);
    }

    // --- CRUD ---

    @Transactional
    public ConnectorDetailResponse createConnector(ConnectorCreationRequest request) {
        if (connectorRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên Connector đã tồn tại. Vui lòng chọn tên khác!");
        }

        Connector connector = connectorMapper.toEntity(request);
        connector.setCreatedAt(Instant.now());
        connector.setUpdatedAt(Instant.now());

        Connector saved = connectorRepository.save(connector);

        // Audit log
        auditService.log(
                "CREATE_CONNECTOR",
                TargetType.CONNECTOR,
                saved.getId().toString(),
                saved.getName(),
                null
        );

        return connectorMapper.toDetailResponse(saved);
    }

    @Transactional
    public ConnectorDetailResponse updateConnector(UUID id, ConnectorUpdateRequest request) {
        Connector connector = connectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector không tồn tại"));

        if (connectorRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Tên Connector đã được sử dụng bởi thiết bị khác!");
        }

        connectorMapper.updateEntityFromRequest(request, connector);
        connector.setUpdatedAt(Instant.now());

        Connector saved = connectorRepository.save(connector);

        // Audit log
        auditService.log(
                "UPDATE_CONNECTOR",
                TargetType.CONNECTOR,
                saved.getId().toString(),
                saved.getName(),
                null
        );

        return connectorMapper.toDetailResponse(saved);
    }

    @Transactional
    public ConnectorDetailResponse toggleStatus(UUID id) {
        Connector connector = connectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector không tồn tại"));

        connector.setEnabled(!connector.isEnabled());
        connector.setUpdatedAt(Instant.now());

        Connector saved = connectorRepository.save(connector);

        // Audit log
        auditService.log(
                saved.isEnabled() ? "ENABLE_CONNECTOR" : "DISABLE_CONNECTOR",
                TargetType.CONNECTOR,
                saved.getId().toString(),
                saved.getName(),
                null
        );

        return connectorMapper.toDetailResponse(saved);
    }

    @Transactional
    public void deleteConnector(UUID id) {
        Connector connector = connectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector không tồn tại"));

        String name = connector.getName();
        connectorRepository.deleteById(id);

        // Audit log
        auditService.log(
                "DELETE_CONNECTOR",
                TargetType.CONNECTOR,
                id.toString(),
                name,
                null
        );
    }
}