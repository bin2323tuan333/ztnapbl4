package com.example.ztna_server.repository;

import com.example.ztna_server.entity.AccessLog;
import com.example.ztna_server.enums.Decision;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AccessLogRepositoryTest {

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Test
    void testSaveAccessLog() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        AccessLog log = AccessLog.builder()
                .requestId(UUID.randomUUID())
                .userId(userId)
                .username("user1")
                .resourceId(resourceId)
                .resourceName("Demo Web Nginx")
                .path("/api/v1/secure")
                .method("GET")
                .clientIp("192.168.1.5")
                .decision(Decision.ALLOW)
                .reason("Access granted by policy")
                .build();

        AccessLog saved = accessLogRepository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDecision()).isEqualTo(Decision.ALLOW);
        assertThat(saved.getUsername()).isEqualTo("user1");
        assertThat(saved.getResourceName()).isEqualTo("Demo Web Nginx");
    }
}