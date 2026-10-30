package com.example.ztna_server.service;

import com.example.ztna_server.dto.response.ResourceResponse;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.ErrorCode;
import com.example.ztna_server.enums.Role;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.mapper.ResourceMapper;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public List<ResourceResponse> getResources() {
        List<Resource> resources  = resourceRepository.findAll();

        return resources.stream()
                .map(resourceMapper::toResourceResponse)
                .toList();
    }

        /**
     * App mà user đang được vào: quyền riêng + quyền qua vai trò, còn hiệu lực,
     * app và connector đang bật. Dùng lại logic của PermissionService để Portal
     * và trang "quyền thực tế" của admin luôn cho cùng một kết quả.
     */
    public List<ResourceResponse> getResourcesByUserId(UUID userId) {
        List<UUID> ids = permissionService.getAccessibleResourceIds(userId);
        if (ids.isEmpty()) {
            return List.of();
        }
        return resourceRepository.findAllById(ids).stream()
                .sorted(Comparator.comparing(Resource::getName))
                .map(resourceMapper::toResourceResponse)
                .toList();
    }
}
