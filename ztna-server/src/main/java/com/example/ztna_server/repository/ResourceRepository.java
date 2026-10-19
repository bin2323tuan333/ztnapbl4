package com.example.ztna_server.repository;

import com.example.ztna_server.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
}
