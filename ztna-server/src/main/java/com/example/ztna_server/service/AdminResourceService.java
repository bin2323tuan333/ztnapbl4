package com.example.ztna_server.service;

import com.example.ztna_server.dto.App;
import com.example.ztna_server.dto.request.ResourceRequest;
import com.example.ztna_server.dto.response.AdminResourceResponse;
import com.example.ztna_server.dto.response.ConnectorResponse;
import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.protocol.frames.RoutingResFrame;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.routes.ConnectionRegister;
import com.example.ztna_server.routes.RoutingManager;
import com.example.ztna_server.routes.RoutingTable;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Quản lý ứng dụng nội bộ (bảng resources). Mỗi app thuộc đúng 1 connector.
 * Sau mỗi thay đổi: RoutingManager nạp lại bảng định tuyến từ database,
 * rồi gửi ROUTING_RESPONSE mới cho connector đang online (không phải chờ lần quét 60 giây).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminResourceService {

    private final ResourceRepository resourceRepository;
    private final ConnectorRepository connectorRepository;
    private final ConnectionRegister connectionRegister;
    private final RoutingManager routingManager;
    private final RoutingTable routingTable;

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
                        .online(connectionRegister.isOnline(c.getId().toString()))
                        .appCount(resourceRepository.countByConnectorId(c.getId()))
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
        Connector connector = findConnector(request.getConnectorId());

        Resource resource = resourceRepository.saveAndFlush(Resource.builder()
                .name(name)
                .type(request.getType())
                .description(trimOrNull(request.getDescription()))
                .host(request.getHost().trim())
                .port(request.getPort())
                .connector(connector)
                .enabled(request.getEnabled() == null || request.getEnabled())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        refreshAndPush(Set.of(connector.getId()));
        return toResponse(resource);
    }

    @Transactional
    public AdminResourceResponse updateResource(UUID id, ResourceRequest request) {
        Resource resource = findResource(id);

        String name = request.getName().trim();
        resourceRepository.findByName(name)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new AppException(ErrorCode.APP_EXISTED); });

        // Đổi connector thì cả connector cũ lẫn mới đều cần bảng định tuyến mới
        Set<UUID> affected = new LinkedHashSet<>();
        affected.add(resource.getConnector().getId());
        Connector connector = findConnector(request.getConnectorId());
        affected.add(connector.getId());

        resource.setName(name);
        resource.setType(request.getType());
        resource.setDescription(trimOrNull(request.getDescription()));
        resource.setHost(request.getHost().trim());
        resource.setPort(request.getPort());
        resource.setConnector(connector);
        if (request.getEnabled() != null) {
            resource.setEnabled(request.getEnabled());
        }
        resource.setUpdatedAt(Instant.now());
        resourceRepository.saveAndFlush(resource);

        refreshAndPush(affected);
        return toResponse(resource);
    }

    @Transactional
    public AdminResourceResponse setEnabled(UUID id, boolean enabled) {
        Resource resource = findResource(id);
        resource.setEnabled(enabled);
        resource.setUpdatedAt(Instant.now());
        resourceRepository.saveAndFlush(resource);

        refreshAndPush(Set.of(resource.getConnector().getId()));
        return toResponse(resource);
    }

    @Transactional
    public void deleteResource(UUID id) {
        Resource resource = findResource(id);
        UUID connectorId = resource.getConnector().getId();
        try {
            resourceRepository.delete(resource);
            resourceRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // App còn quyền truy cập hoặc log trỏ tới → hủy cả giao dịch, giữ nguyên dữ liệu
            throw new AppException(ErrorCode.APP_IN_USE);
        }
        refreshAndPush(Set.of(connectorId));
    }

    // ───────────── Hàm phụ ─────────────

    /** Nạp lại bảng định tuyến từ DB và gửi ngay cho các connector đang online. */
    private void refreshAndPush(Set<UUID> connectorIds) {
        routingManager.refreshRoutingTable();
        for (UUID cid : connectorIds) {
            String key = cid.toString();
            Channel channel = connectionRegister.getChannel(key);
            if (channel == null || !channel.isActive()) continue;

            List<App> apps = routingTable.getAppsByConnector(key);
            RoutingResFrame res = new RoutingResFrame();
            res.setAppCount(apps.size());
            res.setApps(apps);
            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(res.encode())));
            log.info("[ADMIN] Đã gửi bảng định tuyến mới ({} app) cho connector {}", apps.size(), key);
        }
    }

    private Resource findResource(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Connector findConnector(UUID connectorId) {
        return connectorRepository.findById(connectorId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private String trimOrNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // Gọi trong @Transactional vì connector là LAZY
    private AdminResourceResponse toResponse(Resource r) {
        Connector c = r.getConnector();
        return AdminResourceResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .type(r.getType())
                .description(r.getDescription())
                .host(r.getHost())
                .port(r.getPort())
                .enabled(r.getEnabled())
                .connectorId(c.getId())
                .connectorName(c.getName())
                .connectorOnline(connectionRegister.isOnline(c.getId().toString()))
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
