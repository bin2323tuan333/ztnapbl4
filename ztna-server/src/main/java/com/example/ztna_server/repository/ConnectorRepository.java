package com.example.ztna_server.repository;

import com.example.ztna_server.entity.Connector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConnectorRepository extends JpaRepository<Connector, UUID> {
    Optional<Connector> findByConnectionString(String connectionString);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    Optional<Connector> findByName(String name);
}
