package com.example.ztna_server.repository;

import com.example.ztna_server.dto.internal.ResourceAccess;
import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.entity.Rule;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.ResourceType;
import com.example.ztna_server.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Danh sách ứng dụng của người dùng (Portal) chỉ gồm ứng dụng mà người dùng có rule hợp lệ,
 * ứng dụng đang bật và connector của nó đang bật. Chạy với H2 nhúng, không mở cổng mạng.
 */
@DataJpaTest
class ResourceRepositoryTest {

    @Autowired ConnectorRepository connectorRepository;
    @Autowired ResourceRepository resourceRepository;
    @Autowired UserRepository userRepository;
    @Autowired RuleRepository ruleRepository;

    @Test
    void portalChiLietKeUngDungCoQuyenVaConnectorDangBat() {
        Instant now = Instant.now();
        Connector conOn = connectorRepository.save(Connector.builder().name("C-on").connectionString("tok-on").enabled(true).build());
        Connector conOff = connectorRepository.save(Connector.builder().name("C-off").connectionString("tok-off").enabled(false).build());

        Resource appOk = resourceRepository.save(Resource.builder().name("A-ok").type(ResourceType.WEB)
                .host("localhost").port(8082).enabled(true).connector(conOn).build());
        Resource appConOff = resourceRepository.save(Resource.builder().name("B-connector-tat").type(ResourceType.WEB)
                .host("localhost").port(8083).enabled(true).connector(conOff).build());
        Resource appDisabled = resourceRepository.save(Resource.builder().name("C-app-tat").type(ResourceType.WEB)
                .host("localhost").port(8084).enabled(false).connector(conOn).build());
        resourceRepository.save(Resource.builder().name("D-khong-rule").type(ResourceType.WEB)
                .host("localhost").port(8085).enabled(true).connector(conOn).build());

        User u = userRepository.save(User.builder().username("u1").fullName("U1").password("x")
                .email("u1@test").enabled(true).role(Role.USER).createdAt(now).updatedAt(now).build());

        ruleRepository.save(Rule.builder().name("R1").users(Set.of(u))
                .resources(Set.of(appOk, appConOff, appDisabled)).build());

        List<ResourceAccess> list = resourceRepository.findAllByUserId(u.getId(), now);

        assertEquals(1, list.size(), "Chỉ ứng dụng có rule, đang bật và connector đang bật mới hiện ở Portal");
        assertEquals("A-ok", list.get(0).getName());

        // Bật lại connector thì ứng dụng hiện lại
        conOff.setEnabled(true);
        connectorRepository.save(conOff);
        assertEquals(2, resourceRepository.findAllByUserId(u.getId(), now).size());
    }
}
