package com.example.ztna_server.mapper;

import com.example.ztna_server.dto.internal.ResourceAccess;
import com.example.ztna_server.dto.response.ResourceResponse;
import com.example.ztna_server.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResourceMapper {
    @Mapping(target = "type", expression = "java(resource.getType().name().toLowerCase())")
    ResourceResponse toResourceResponse(Resource resource);
    @Mapping(target = "type", expression = "java(resource.getType().name().toLowerCase())")
    ResourceResponse toResourceResponse(ResourceAccess resource);
}
