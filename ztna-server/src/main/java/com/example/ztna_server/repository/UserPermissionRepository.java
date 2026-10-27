package com.example.ztna_server.repository;

import com.example.ztna_server.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {

}
