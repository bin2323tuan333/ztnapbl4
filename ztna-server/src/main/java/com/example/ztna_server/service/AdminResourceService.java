package com.example.ztna_server.service;

import com.example.ztna_server.dto.App;
import com.example.ztna_server.dto.request.ResourceRequest;
import com.example.ztna_server.dto.response.resource.AdminResourceDetailResponse;
import com.example.ztna_server.dto.response.resource.AdminResourceSummaryResponse;
import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.enums.TargetType;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.mapper.ResourceMapper;
import com.example.ztna_server.protocol.frames.RoutingResFrame;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.routes.ConnectionRegister;
import com.example.ztna_server.routes.RoutingManager;
import com.example.ztna_server.routes.RoutingTable;
import com.example.ztna_server.security.SecurityUtils;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminResourceService {
    private final ResourceRepository resourceRepository;
    private final ConnectorRepository connectorRepository;
    private final ConnectionRegister connectionRegister;
    private final RoutingManager routingManager;
    private final RoutingTable routingTable;
    private final ResourceMapper resourceMapper;
    private final AuditService auditService;   

    @Transactional(readOnly = true)
    public List<AdminResourceSummaryResponse> getResources() {
        return resourceRepository.findAll()
                .stream()
                .map(resourceMapper::toAdminResourceSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminResourceDetailResponse getResource(UUID id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        AdminResourceDetailResponse response = resourceMapper.toAdminResourceDetailResponse(resource);
        return response;
    }


    @Transactional
    public AdminResourceDetailResponse createResource(ResourceRequest request) {
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

        // Ghi audit log
        auditService.log(
                "CREATE_RESOURCE",
                TargetType.RESOURCE,
                resource.getId().toString(),
                resource.getName(),
                null
        );

        refreshAndPush(Set.of(connector.getId()));
        return toResponse(resource);
    }

    @Transactional
    public AdminResourceDetailResponse updateResource(UUID id, ResourceRequest request) {
        Resource resource = findResource(id);

        String name = request.getName().trim();
        resourceRepository.findByName(name)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new AppException(ErrorCode.APP_EXISTED); });

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

        // Ghi audit log
        auditService.log(
                "UPDATE_RESOURCE",
                TargetType.RESOURCE,
                resource.getId().toString(),
                resource.getName(),
                null
        );

        refreshAndPush(affected);
        return toResponse(resource);
    }

    @Transactional
    public AdminResourceDetailResponse setEnabled(UUID id, boolean enabled) {
        Resource resource = findResource(id);
        resource.setEnabled(enabled);
        resource.setUpdatedAt(Instant.now());
        resourceRepository.saveAndFlush(resource);

        // Ghi audit log
        auditService.log(
                enabled ? "ENABLE_RESOURCE" : "DISABLE_RESOURCE",
                TargetType.RESOURCE,
                resource.getId().toString(),
                resource.getName().toString(),
                null);

        refreshAndPush(Set.of(resource.getConnector().getId()));
        return toResponse(resource);
    }

    @Transactional
    public void deleteResource(UUID id) {
        Resource resource = findResource(id);
        UUID connectorId = resource.getConnector().getId();
        String resourceName = resource.getName();

        try {
            resourceRepository.delete(resource);
            resourceRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.APP_IN_USE);
        }

        // Ghi audit log
        auditService.log(
                "DELETE_RESOURCE",
                TargetType.RESOURCE,
                id.toString(),
                resourceName,
                null
        );

        refreshAndPush(Set.of(connectorId));
    }


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

    private AdminResourceDetailResponse toResponse(Resource r) {
        Connector c = r.getConnector();
        return AdminResourceDetailResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .type(r.getType())
                .description(r.getDescription())
                .host(r.getHost())
                .port(r.getPort())
                .enabled(r.getEnabled())
                .connectorZone(c.getName())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}