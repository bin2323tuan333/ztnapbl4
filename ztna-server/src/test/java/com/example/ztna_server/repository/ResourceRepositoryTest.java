package com.example.ztna_server.repository;

import com.example.ztna_server.entity.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ResourceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void testSaveResource() {
        Resource res = Resource.builder()
                .name("Payment API")
                .backendUrl("http://192.168.1.100:8080")
                .description("Internal payment gateway")
                .enabled(true)
                .build();

        Resource saved = entityManager.persistAndFlush(res);

        assertThat(saved.getId()).isNotNull();
        assertThat(resourceRepository.findAll()).hasSize(1);
    }
}