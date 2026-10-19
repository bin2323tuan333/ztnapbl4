package com.example.ztna_server.repository;

import com.example.ztna_server.entity.*;
import com.example.ztna_server.enums.Decision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AccessLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccessLogRepository accessLogRepository;

    private User user;
    private Resource resource;

    @BeforeEach
    void setUp() {
        user = User.builder().username("user1").password("123").email("u@a.com").enabled(true).build();
        entityManager.persist(user);

        resource = Resource.builder().name("API").backendUrl("http://api").enabled(true).build();
        entityManager.persist(resource);
    }

    @Test
    void testSaveAccessLog() {
        AccessLog log = new AccessLog();
        log.setRequestId(UUID.randomUUID());
        log.setUser(user);
        log.setResource(resource);
        log.setPath("/api/v1/secure");
        log.setMethod("GET");
        log.setClientIp("192.168.1.5");
        log.setDecision(Decision.ALLOW); // Thay bằng Enum của bạn
        log.setReason("Access granted by policy");

        AccessLog saved = accessLogRepository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDecision()).isEqualTo(Decision.ALLOW);
    }
}