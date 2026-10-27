package com.example.ztna_server.repository;

import com.example.ztna_server.entity.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SystemRoleRepository extends JpaRepository<SystemRole, UUID> {
    Optional<SystemRole> findByName(String name);
    boolean existsByName(String name);
}
