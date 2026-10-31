package com.example.ztna_server.mapper;

import com.example.ztna_server.dto.request.connector.ConnectorCreationRequest;
import com.example.ztna_server.dto.request.connector.ConnectorUpdateRequest;
import com.example.ztna_server.dto.response.connector.ConnectorDetailResponse;
import com.example.ztna_server.dto.response.connector.ConnectorNameResponse;
import com.example.ztna_server.dto.response.connector.ConnectorSummaryResponse;
import com.example.ztna_server.entity.Connector;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConnectorMapper {

    ConnectorNameResponse toNameResponse(Connector connector);

    ConnectorSummaryResponse toSummaryResponse(Connector connector);

    ConnectorDetailResponse toDetailResponse(Connector connector);

    // --- MAP TỪ REQUEST VÀO ENTITY ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resources", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Connector toEntity(ConnectorCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resources", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ConnectorUpdateRequest request, @MappingTarget Connector connector);
}