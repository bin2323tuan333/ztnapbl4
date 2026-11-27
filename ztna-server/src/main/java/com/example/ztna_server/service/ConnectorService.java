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
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.runtime.connection.connector.ConnectorConnectionRegistry;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.event.AccessPolicyChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;   // báo thay đổi quyền để đóng phiên không còn hợp lệ
    private final ConnectorConnectionRegistry connectorConnectionRegistry;
    private final ResourceRepository resourceRepository;


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
                .orElseThrow(() -> new AppException(ErrorCode.CONNECTOR_NOT_FOUND));
        return connectorMapper.toDetailResponse(connector);
    }

    // --- CRUD ---

    @Transactional
    public ConnectorDetailResponse createConnector(ConnectorCreationRequest request) {
        if (connectorRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CONNECTOR_EXISTED);
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
                .orElseThrow(() -> new AppException(ErrorCode.CONNECTOR_NOT_FOUND));

        if (connectorRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.CONNECTOR_EXISTED);
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
                .orElseThrow(() -> new AppException(ErrorCode.CONNECTOR_NOT_FOUND));

        connector.setEnabled(!connector.isEnabled());
        connector.setUpdatedAt(Instant.now());

        if (!connector.isEnabled()) {
            // Ngắt kết nối WS đang mở của connector (trước đây gọi remove(connectionId) với connectorId nên không có tác dụng)
            connectorConnectionRegistry.removeByConnectorId(id.toString());
        }

        Connector saved = connectorRepository.save(connector);

        eventPublisher.publishEvent(new AccessPolicyChangedEvent(saved.isEnabled() ? "ENABLE_CONNECTOR" : "DISABLE_CONNECTOR"));
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
                .orElseThrow(() -> new AppException(ErrorCode.CONNECTOR_NOT_FOUND));

        // Còn ứng dụng thuộc connector thì không xoá (trước đây xoá dây chuyền và lỗi 500 khi ứng dụng nằm trong rule)
        long resourceCount = resourceRepository.countByConnectorId(id);
        if (resourceCount > 0) {
            throw new AppException(ErrorCode.CONNECTOR_IN_USE);
        }

        connectorConnectionRegistry.removeByConnectorId(connector.getId().toString());
        String name = connector.getName();
        connectorRepository.deleteById(id);

        eventPublisher.publishEvent(new AccessPolicyChangedEvent("DELETE_CONNECTOR"));
        auditService.log(
                "DELETE_CONNECTOR",
                TargetType.CONNECTOR,
                id.toString(),
                name,
                null
        );
    }
}