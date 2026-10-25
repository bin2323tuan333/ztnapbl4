package com.example.ztna_server.repository;

import com.example.ztna_server.entity.ConnectorResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConnectorResourceRepository extends JpaRepository<ConnectorResource, UUID> {
    // Các app mà 1 connector phụ trách
    List<ConnectorResource> findByConnectorId(UUID connectorId);
    long countByConnectorId(UUID connectorId);

    // Connector đang phụ trách 1 app
    List<ConnectorResource> findByResourceId(UUID resourceId);
}
