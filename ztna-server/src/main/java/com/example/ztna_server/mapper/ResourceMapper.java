package com.example.ztna_server.mapper;

import com.example.ztna_server.dto.response.ResourceResponse;
import com.example.ztna_server.entity.Resource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceMapper {
    ResourceResponse toResourceResponse(Resource resource);
}
