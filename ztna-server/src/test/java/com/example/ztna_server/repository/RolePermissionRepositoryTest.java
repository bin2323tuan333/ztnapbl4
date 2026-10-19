package com.example.ztna_server.repository;

import com.example.ztna_server.entity.*;
import com.example.ztna_server.enums.UserAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RolePermissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    private SystemRole role;
    private Resource resource;
    private User admin;

    @BeforeEach
    void setUp() {
        admin = User.builder().username("admin").password("123").email("a@a.com").enabled(true).build();
        entityManager.persist(admin);

        role = SystemRole.builder().name("ADMIN").displayName("Admin").description("Admin").build();
        entityManager.persist(role);

        resource = Resource.builder().name("API").backendUrl("http://api").enabled(true).build();
        entityManager.persist(resource);
    }

    @Test
    void testGrantPermissionToRole() {
        RolePermission perm = new RolePermission();
        perm.setRole(role);
        perm.setResource(resource);
        perm.setAction(UserAction.READ); // Thay bằng Enum thực tế của bạn
        perm.setGrantedBy(admin);
        perm.setReason("Admin needs read access");
        perm.setEnabled(true);

        RolePermission saved = rolePermissionRepository.save(perm);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole().getName()).isEqualTo("ADMIN");
        assertThat(saved.getAction()).isEqualTo(UserAction.READ);
    }
}