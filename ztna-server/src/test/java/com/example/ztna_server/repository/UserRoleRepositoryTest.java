package com.example.ztna_server.repository;

import com.example.ztna_server.entity.SystemRole;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User user;
    private SystemRole role;

    @BeforeEach
    void setUp() {
        user = User.builder().username("user1").password("123").email("u1@a.com").enabled(true).build();
        entityManager.persist(user);

        role = SystemRole.builder().name("USER").displayName("User").description("Normal user").build();
        entityManager.persist(role);
    }

    @Test
    void testAssignRoleToUser() {
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .grantedBy(user) // Tự cấp
                .reason("Default role")
                .enabled(true)
                .build();

        UserRole saved = userRoleRepository.save(userRole);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getUsername()).isEqualTo("user1");
        assertThat(saved.getRole().getName()).isEqualTo("USER");
    }
}