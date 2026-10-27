package com.example.ztna_server.repository;

import com.example.ztna_server.entity.ConnectorResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConnectorRepository extends JpaRepository<ConnectorResource, UUID> {
}
