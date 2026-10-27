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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    /**
     * Danh sách app cho người đang đăng nhập (Least Privilege):
     * - ADMIN thấy tất cả để quản trị
     * - Người khác chỉ thấy app mình được cấp quyền (riêng hoặc qua role)
     * - Tài khoản bị khóa không thấy gì, kể cả khi access token chưa hết hạn
     */
    public List<ResourceResponse> getResources() {
        UUID userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        List<Resource> resources;
        if (user.getRole() == Role.ADMIN) {
            resources = resourceRepository.findAll();
        } else {
            List<UUID> allowedIds = permissionService.getAccessibleResourceIds(userId);
            resources = resourceRepository.findAllById(allowedIds);
        }

        return resources.stream()
                .map(resourceMapper::toResourceResponse)
                .toList();
    }
}
