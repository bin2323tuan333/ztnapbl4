package com.example.ztna_server.config;

import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.entity.SystemRole;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.entity.UserRole;
import com.example.ztna_server.enums.ResourceType;
import com.example.ztna_server.enums.Role;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.SystemRoleRepository;
import com.example.ztna_server.repository.UserRepository;
import com.example.ztna_server.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SystemRoleRepository systemRoleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceRepository resourceRepository;

    private final ConnectorRepository connectorRepository;

    @Override
    public void run(String... args) {

        Connector defaultConnector;
        if (connectorRepository.count() == 0) {
            defaultConnector = Connector.builder()
                    .name("Local Agent Demo")
                    .connectionString("toilaconnector1")
                    .enabled(true)
                    .build();
            defaultConnector = connectorRepository.save(defaultConnector);
            System.out.println("====== ĐÃ TỰ ĐỘNG THÊM CONNECTOR DEMO VÀO DATABASE ======");
        } else {
            defaultConnector = connectorRepository.findAll().get(0);
        }

        if (resourceRepository.count() == 0) {

            // 1. Resource Nginx
            Resource web = Resource.builder()
                    .name("Demo Web Nginx")
                    .type(ResourceType.WEB)
                    .description("Trang web demo chạy Nginx Alpine (Cổng 8082)")
                    .host("localhost") // Đã tách host
                    .port(8082)        // Đã tách port
                    .enabled(true)
                    .connector(defaultConnector) // Gán Connector
                    .build();

            // 2. Resource SSH
            Resource ssh = Resource.builder()
                    .name("Demo SSH Server")
                    .type(ResourceType.SSH)
                    .description("Máy chủ Linux giả lập SSH (Cổng 8083)")
                    .host("localhost")
                    .port(8083)
                    .enabled(true)
                    .connector(defaultConnector) // Gán Connector
                    .build();

            // 3. Resource MySQL
            Resource mysql = Resource.builder()
                    .name("Demo Database MySQL")
                    .type(ResourceType.MYSQL)
                    .description("Cơ sở dữ liệu MySQL 8.0 (Cổng 8081)")
                    .host("localhost")
                    .port(8081)
                    .enabled(true)
                    .connector(defaultConnector) // Gán Connector
                    .build();

            // Lưu tất cả vào Database
            resourceRepository.saveAll(List.of(web, ssh, mysql));

            System.out.println("====== ĐÃ TỰ ĐỘNG THÊM 3 RESOURCE DEMO VÀO DATABASE ======");
        }


        // ==========================================
        // 3. KHỞI TẠO USER (Giữ nguyên logic cũ của bạn)
        // ==========================================
        if (systemRoleRepository.count() == 0) {
            Instant t = Instant.now();
            systemRoleRepository.saveAll(List.of(
                    SystemRole.builder().name("DEV").displayName("Lập trình viên")
                            .description("Nhóm phát triển, được vào web và SSH demo").createdAt(t).updatedAt(t).build(),
                    SystemRole.builder().name("DBA").displayName("Quản trị CSDL")
                            .description("Nhóm quản trị cơ sở dữ liệu, được vào MySQL demo").createdAt(t).updatedAt(t).build(),
                    SystemRole.builder().name("KE_TOAN").displayName("Kế toán")
                            .description("Nhóm kế toán, chỉ được vào web demo").createdAt(t).updatedAt(t).build()
            ));
            System.out.println("====== ĐÃ TỰ ĐỘNG THÊM 3 ROLE DEMO: DEV, DBA, KE_TOAN ======");
        }

        if (userRepository.count() > 0) {
            System.out.println("ℹ️  Users đã có, bỏ qua seed.");
            return;
        }

        Instant now = Instant.now();

        User adminUser = User.builder()
                .username("admin")
                .fullName("Quản trị viên")
                .password(passwordEncoder.encode("123"))
                .email("admin@ztna.com")
                .enabled(true)
                .role(Role.ADMIN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User supervisorUser = User.builder()
                .username("supervisor")
                .fullName("Người Giám Sát")
                .password(passwordEncoder.encode("super123"))
                .email("supervisor@ztna.com")
                .enabled(true)
                .role(Role.SUPERVISOR)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User normalUser = User.builder()
                .username("user")
                .fullName("Người dùng A")
                .password(passwordEncoder.encode("123"))
                .email("user@ztna.com")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User alice = User.builder()
                .username("alice")
                .fullName("Alice Nguyen")
                .password(passwordEncoder.encode("alice123"))
                .email("alice@ztna.com")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User bob = User.builder()
                .username("bob")
                .fullName("Bob Tran")
                .password(passwordEncoder.encode("bob123"))
                .email("bob@ztna.com")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User lockedUser = User.builder()
                .username("locked")
                .fullName("Tài khoản bị khóa")
                .password(passwordEncoder.encode("locked123"))
                .email("locked@ztna.com")
                .enabled(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.saveAll(List.of(adminUser, supervisorUser, normalUser, alice, bob, lockedUser));

        System.out.println("✅ Đã seed 6 users và gán quyền thành công:");
        System.out.println("   - admin      / 123        (ADMIN)");
        System.out.println("   - supervisor / super123   (AUDITOR - Giám sát)");
        System.out.println("   - user       / 123        (USER)");
        System.out.println("   - alice      / alice123   (USER)");
        System.out.println("   - bob        / bob123     (USER)");
        System.out.println("   - locked     / locked123  (USER - BỊ KHÓA)");
    }
}