package com.example.ztna_server.repo;

import com.example.ztna_server.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Page<User> findBySystemRole_Id(Long roleId, Pageable pageable);

    boolean existsBySystemRole_Id(Long roleId);

    long countBySystemRole_Id(Long roleId);
}
