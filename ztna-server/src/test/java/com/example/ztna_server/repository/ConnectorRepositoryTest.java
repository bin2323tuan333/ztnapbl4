package com.example.ztna_server.repository;

import com.example.ztna_server.entity.Connector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ConnectorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConnectorRepository connectorRepository;

    @Test
    void testSaveConnectorWithConfig() {
        Connector connector = Connector.builder()
                .name("Agent-Server-01")
                .connectionStringOrConfig("{\"ip\":\"10.0.0.5\", \"port\": 8443}") // Test cột TEXT
                .enabled(true)
                .build();

        Connector saved = entityManager.persistAndFlush(connector);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getConnectionStringOrConfig()).contains("10.0.0.5");
    }
}