package com.example.ztna_server.repository;

import com.example.ztna_server.entity.SystemRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SystemRoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SystemRoleRepository systemRoleRepository;

    @Test
    void testSaveSystemRole() {
        SystemRole role = SystemRole.builder()
                .name("ADMIN")
                .displayName("Administrator")
                .description("Full system access")
                .build();

        SystemRole savedRole = entityManager.persistAndFlush(role);

        assertThat(savedRole.getId()).isNotNull();
        assertThat(systemRoleRepository.findAll()).hasSize(1);
        assertThat(systemRoleRepository.findById(savedRole.getId()).get().getName()).isEqualTo("ADMIN");
    }
}