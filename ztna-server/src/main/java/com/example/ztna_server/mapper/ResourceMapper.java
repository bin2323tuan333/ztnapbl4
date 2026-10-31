package com.example.ztna_server.mapper;

import com.example.ztna_server.dto.internal.ResourceAccess;
import com.example.ztna_server.dto.response.resource.AdminResourceDetailResponse;
import com.example.ztna_server.dto.response.resource.ResourceResponse;
import com.example.ztna_server.dto.response.resource.AdminResourceSummaryResponse;
import com.example.ztna_server.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResourceMapper {
    ResourceResponse toResourceResponse(Resource resource);
    ResourceResponse toResourceResponse(ResourceAccess resource);
//    @Mapping(target = "id", source = "connector.id")
    @Mapping(target = "connectorZone", source = "connector.name")
    AdminResourceSummaryResponse toAdminResourceSummaryResponse(Resource resource);


    @Mapping(target = "connectorId", source = "connector.id")
    @Mapping(target = "connectorZone", source = "connector.name")
    AdminResourceDetailResponse toAdminResourceDetailResponse(Resource resource);
}