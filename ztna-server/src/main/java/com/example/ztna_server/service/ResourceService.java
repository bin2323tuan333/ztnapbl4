package com.example.ztna_server.service;

import com.example.ztna_server.dto.response.resource.ResourceResponse;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.mapper.ResourceMapper;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final UserRepository userRepository;

    public List<ResourceResponse> getResources() {
        List<Resource> resources  = resourceRepository.findAll();

        return resources.stream()
                .map(resourceMapper::toResourceResponse)
                .toList();
    }

//    public List<ResourceResponse> getResourcesByUserId(UUID userId) {
//        return resourceRepository.findAllByUserId(userId).stream()
//                .map(resourceMapper::toResourceResponse)
//                .toList();
//    }
}
