package com.example.ztna_server.repository;

import com.example.ztna_server.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID>  {

}
