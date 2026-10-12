
package com.example.ztna_server.repo;

import com.example.ztna_server.entity.SystemRole;
import com.example.ztna_server.entity.enums.SystemRoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemRoleRepository
        extends JpaRepository<SystemRole, Long> {

    Optional<SystemRole> findByName(SystemRoleName name);

    boolean existsByName(SystemRoleName name);

    boolean existsByNameAndIdNot(
            SystemRoleName name, Long id);
}
