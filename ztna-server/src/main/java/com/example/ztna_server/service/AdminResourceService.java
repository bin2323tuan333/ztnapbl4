package com.example.ztna_server.service;

import com.example.ztna_server.dto.request.ResourceRequest;
import com.example.ztna_server.dto.response.AdminResourceResponse;
import com.example.ztna_server.dto.response.ConnectorResponse;
import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.ConnectorResource;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ConnectorResourceRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.routes.ConnectionRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Quản lý ứng dụng nội bộ (bảng resources) và connector phụ trách (bảng connector_resources).
 * Mỗi lần thay đổi, bảng định tuyến được nạp lại và đẩy xuống connector đang online.
 */
@Service
@RequiredArgsConstructor
public class AdminResourceService {

    private final ResourceRepository resourceRepository;
    private final ConnectorRepository connectorRepository;
    private final ConnectorResourceRepository connectorResourceRepository;
    private final ConnectionRegister connectionRegister;
    private final RoutingService routingService;

    // ───────────── Xem ─────────────

    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getResources() {
        return resourceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminResourceResponse getResource(UUID id) {
        return toResponse(findResource(id));
    }

    @Transactional(readOnly = true)
    public List<ConnectorResponse> getConnectors() {
        return connectorRepository.findAll().stream()
                .map(c -> ConnectorResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .enabled(c.isEnabled())
                        .online(connectionRegister.isOnline(c.getName()))
                        .appCount(connectorResourceRepository.countByConnectorId(c.getId()))
                        .build())
                .toList();
    }

    // ───────────── Thêm / sửa / bật tắt / xóa ─────────────

    @Transactional
    public AdminResourceResponse createResource(ResourceRequest request) {
        String name = request.getName().trim();
        if (resourceRepository.existsByName(name)) {
            throw new AppException(ErrorCode.APP_EXISTED);
        }
        String backendUrl = validateBackendUrl(request.getBackendUrl());
        Connector connector = findConnectorOrNull(request.getConnectorId());

        Resource resource = resourceRepository.save(Resource.builder()
                .name(name)
                .description(trimOrNull(request.getDescription()))
                .backendUrl(backendUrl)
                .enabled(request.getEnabled() == null || request.getEnabled())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        if (connector != null) {
            linkConnector(resource, connector);
            routingService.reloadAndPush(connector.getName());
        }
        return toResponse(resource);
    }

    @Transactional
    public AdminResourceResponse updateResource(UUID id, ResourceRequest request) {
        Resource resource = findResource(id);

        String name = request.getName().trim();
        resourceRepository.findByName(name)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new AppException(ErrorCode.APP_EXISTED); });

        resource.setName(name);
        resource.setDescription(trimOrNull(request.getDescription()));
        resource.setBackendUrl(validateBackendUrl(request.getBackendUrl()));
        if (request.getEnabled() != null) {
            resource.setEnabled(request.getEnabled());
        }
        resource.setUpdatedAt(Instant.now());
        resourceRepository.save(resource);

        // Đổi connector: gỡ liên kết cũ, tạo liên kết mới
        Set<String> affected = currentConnectorNames(id);
        Connector newConnector = findConnectorOrNull(request.getConnectorId());
        connectorResourceRepository.deleteAll(connectorResourceRepository.findByResourceId(id));
        if (newConnector != null) {
            linkConnector(resource, newConnector);
            affected.add(newConnector.getName());
        }
        connectorResourceRepository.flush();

        affected.forEach(routingService::reloadAndPush);
        return toResponse(resource);
    }

    @Transactional
    public AdminResourceResponse setEnabled(UUID id, boolean enabled) {
        Resource resource = findResource(id);
        resource.setEnabled(enabled);
        resource.setUpdatedAt(Instant.now());
        resourceRepository.saveAndFlush(resource);

        currentConnectorNames(id).forEach(routingService::reloadAndPush);
        return toResponse(resource);
    }

    @Transactional
    public void deleteResource(UUID id) {
        Resource resource = findResource(id);
        Set<String> affected = currentConnectorNames(id);
        try {
            connectorResourceRepository.deleteAll(connectorResourceRepository.findByResourceId(id));
            resourceRepository.delete(resource);
            resourceRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // App còn quyền truy cập hoặc log trỏ tới → hủy cả giao dịch, giữ nguyên dữ liệu
            throw new AppException(ErrorCode.APP_IN_USE);
        }
        affected.forEach(routingService::reloadAndPush);
    }

    // ───────────── Hàm phụ ─────────────

    private Resource findResource(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Connector findConnectorOrNull(UUID connectorId) {
        if (connectorId == null) return null;
        return connectorRepository.findById(connectorId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void linkConnector(Resource resource, Connector connector) {
        connectorResourceRepository.save(ConnectorResource.builder()
                .resource(resource)
                .connector(connector)
                .enabled(true)
                .createdAt(Instant.now())
                .build());
    }

    private Set<String> currentConnectorNames(UUID resourceId) {
        Set<String> names = new LinkedHashSet<>();
        for (ConnectorResource link : connectorResourceRepository.findByResourceId(resourceId)) {
            names.add(link.getConnector().getName());
        }
        return names;
    }

    private String validateBackendUrl(String url) {
        if (RoutingService.parseBackendUrl(url) == null) {
            throw new AppException(ErrorCode.INVALID_BACKEND_URL);
        }
        return url.trim();
    }

    private String trimOrNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // Gọi trong @Transactional vì connector trong ConnectorResource là LAZY
    private AdminResourceResponse toResponse(Resource r) {
        Connector connector = connectorResourceRepository.findByResourceId(r.getId()).stream()
                .map(ConnectorResource::getConnector)
                .findFirst()
                .orElse(null);

        return AdminResourceResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .backendUrl(r.getBackendUrl())
                .enabled(r.getEnabled())
                .connectorId(connector != null ? connector.getId() : null)
                .connectorName(connector != null ? connector.getName() : null)
                .connectorOnline(connector != null && connectionRegister.isOnline(connector.getName()))
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
