package com.example.ztna_server.repository;

import com.example.ztna_server.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
}
