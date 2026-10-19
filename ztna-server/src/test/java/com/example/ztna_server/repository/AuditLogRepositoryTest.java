package com.example.ztna_server.repository;

import com.example.ztna_server.entity.AuditLog;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.Action;
import com.example.ztna_server.enums.TargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AuditLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private User actor;

    @BeforeEach
    void setUp() {
        actor = User.builder().username("manager").password("123").email("m@a.com").enabled(true).build();
        entityManager.persist(actor);
    }

    @Test
    void testSaveAuditLog() {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(Action.CREATE); // Thay bằng Enum của bạn
        log.setTargetType(TargetType.RESOURCE); // Thay bằng Enum của bạn
        log.setTargetId("uuid-1234");
        log.setDetails("{\"action\": \"create_resource\", \"name\": \"New API\"}"); // Test TEXT
        log.setIpAddress("10.0.0.1");

        AuditLog saved = auditLogRepository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDetails()).contains("create_resource");
        assertThat(saved.getActor().getUsername()).isEqualTo("manager");
    }
}