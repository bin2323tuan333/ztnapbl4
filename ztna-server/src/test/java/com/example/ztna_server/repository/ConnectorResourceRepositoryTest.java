package com.example.ztna_server.repository;

import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.ConnectorResource;
import com.example.ztna_server.entity.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ConnectorResourceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConnectorResourceRepository connectorResourceRepository;

    private Connector connector;
    private Resource resource;

    @BeforeEach
    void setUp() {
        connector = Connector.builder().name("Agent-01").connectionStringOrConfig("{}").enabled(true).build();
        entityManager.persist(connector);

        resource = Resource.builder().name("DB-Server").backendUrl("tcp://10.0.0.6:3306").enabled(true).build();
        entityManager.persist(resource);
    }

    @Test
    void testLinkConnectorToResource() {
        ConnectorResource cr = new ConnectorResource();
        cr.setConnector(connector);
        cr.setResource(resource);
        cr.setEnabled(true);

        ConnectorResource saved = connectorResourceRepository.save(cr);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getConnector().getName()).isEqualTo("Agent-01");
        assertThat(saved.getResource().getName()).isEqualTo("DB-Server");
    }
}