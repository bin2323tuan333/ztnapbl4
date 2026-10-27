package com.example.ztna_server.repository;

import com.example.ztna_server.entity.AuditLog;
import com.example.ztna_server.enums.Action;
import com.example.ztna_server.enums.TargetType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void testSaveAuditLog() {
        UUID actorId = UUID.randomUUID();

        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .actorUsername("manager")
                .action(Action.CREATE_USER)          
                .targetType(TargetType.USER)
                .targetId(UUID.randomUUID().toString())
                .details("{\"action\": \"create_user\", \"username\": \"newuser\"}")
                .ipAddress("10.0.0.1")
                .build();

        AuditLog saved = auditLogRepository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getActorUsername()).isEqualTo("manager");
        assertThat(saved.getAction()).isEqualTo(Action.CREATE_USER);
        assertThat(saved.getDetails()).contains("create_user");
    }
}