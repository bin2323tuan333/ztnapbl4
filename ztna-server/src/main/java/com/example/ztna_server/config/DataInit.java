package com.example.ztna_server.config;

import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.entity.Rule;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.enums.ResourceType;
import com.example.ztna_server.enums.Role;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.RuleRepository;
import com.example.ztna_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceRepository resourceRepository;
    private final ConnectorRepository connectorRepository;
    private final RuleRepository ruleRepository; // Thay thế SystemRoleRepository

    @Override
    public void run(String... args) {

        // ==========================================
        // 1. KHỞI TẠO CONNECTOR
        // ==========================================
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

        // ==========================================
        // 2. KHỞI TẠO RESOURCES (APPS)
        // ==========================================
        Resource web, ssh, mysql;
        if (resourceRepository.count() == 0) {
            web = Resource.builder()
                    .name("Demo Web Nginx").type(ResourceType.WEB)
                    .description("Trang web demo chạy Nginx Alpine (Cổng 8082)")
                    .host("localhost").port(8082).enabled(true).connector(defaultConnector).build();

            ssh = Resource.builder()
                    .name("Demo SSH Server").type(ResourceType.SSH)
                    .description("Máy chủ Linux giả lập SSH (Cổng 8083)")
                    .host("localhost").port(8083).enabled(true).connector(defaultConnector).build();

            mysql = Resource.builder()
                    .name("Demo Database MySQL").type(ResourceType.MYSQL)
                    .description("Cơ sở dữ liệu MySQL 8.0 (Cổng 8081)")
                    .host("localhost").port(8081).enabled(true).connector(defaultConnector).build();

            resourceRepository.saveAll(List.of(web, ssh, mysql));
            System.out.println("====== ĐÃ TỰ ĐỘNG THÊM 3 RESOURCE DEMO VÀO DATABASE ======");
        } else {
            List<Resource> existingResources = resourceRepository.findAll();
            web = existingResources.stream().filter(r -> r.getType() == ResourceType.WEB).findFirst().orElse(null);
            ssh = existingResources.stream().filter(r -> r.getType() == ResourceType.SSH).findFirst().orElse(null);
            mysql = existingResources.stream().filter(r -> r.getType() == ResourceType.MYSQL).findFirst().orElse(null);
        }

        // ==========================================
        // 3. KHỞI TẠO USERS
        // ==========================================
        if (userRepository.count() > 0) {
            System.out.println("ℹ️ Users đã có, bỏ qua seed Users.");
            return;
        }

        Instant now = Instant.now();

        User adminUser = User.builder()
                .username("admin").fullName("Quản trị viên").password(passwordEncoder.encode("123"))
                .email("admin@ztna.com").enabled(true).role(Role.ADMIN).createdAt(now).updatedAt(now).build();

        User supervisorUser = User.builder()
                .username("supervisor").fullName("Người Giám Sát").password(passwordEncoder.encode("super123"))
                .email("supervisor@ztna.com").enabled(true).role(Role.SUPERVISOR).createdAt(now).updatedAt(now).build();

        User normalUser = User.builder()
                .username("user").fullName("Người dùng A").password(passwordEncoder.encode("123"))
                .email("user@ztna.com").enabled(true).createdAt(now).updatedAt(now).build();

        User alice = User.builder()
                .username("alice").fullName("Alice Nguyen").password(passwordEncoder.encode("alice123"))
                .email("alice@ztna.com").enabled(true).createdAt(now).updatedAt(now).build();

        User bob = User.builder()
                .username("bob").fullName("Bob Tran").password(passwordEncoder.encode("bob123"))
                .email("bob@ztna.com").enabled(true).createdAt(now).updatedAt(now).build();

        User lockedUser = User.builder()
                .username("locked").fullName("Tài khoản bị khóa").password(passwordEncoder.encode("locked123"))
                .email("locked@ztna.com").enabled(false).createdAt(now).updatedAt(now).build();

        userRepository.saveAll(List.of(adminUser, supervisorUser, normalUser, alice, bob, lockedUser));

        System.out.println("✅ Đã seed 6 users thành công.");
        System.out.println("   - admin      / 123        (ADMIN)");
        System.out.println("   - supervisor / super123   (SUPERVISOR)");
        System.out.println("   - user       / 123        (USER)");
        System.out.println("   - alice      / alice123   (USER)");
        System.out.println("   - bob        / bob123     (USER)");
        System.out.println("   - locked     / locked123  (USER - BỊ KHÓA)");

        // ==========================================
        // 4. KHỞI TẠO RULES (GÓI QUYỀN TRUY CẬP)
        // ==========================================
            if (ruleRepository.count() == 0 && web != null && ssh != null && mysql != null) {

                // Rule 1: Nhóm Dev
                Rule devRule = Rule.builder()
                        // ...
                        .name("Dev Team Access")
                        .users(Set.of(alice, bob)) // Đổi List thành Set
                        .resources(Set.of(web, ssh)) // Đổi List thành Set
                        .build();

                // Rule 2: Nhóm DBA
                Rule dbaRule = Rule.builder()
                        // ...
                        .name("DBA Team Access")
                        .users(Set.of(alice, adminUser))
                        .resources(Set.of(mysql))
                        .build();

                // Rule 3: Admin
                Rule adminRule = Rule.builder()
                        // ...
                        .name("Super Admin Access")
                        .users(Set.of(adminUser))
                        .resources(Set.of(web, ssh, mysql))
                        .build();

                // Rule 4: User Only
                Rule singleUserRule = Rule.builder()
                        // ...
                        .name("Cá nhân - User Only")
                        .users(Set.of(normalUser))
                        .resources(Set.of(web))
                        .build();

                // Lưu tất cả Rules vào Database
                ruleRepository.saveAll(List.of(devRule, dbaRule, adminRule, singleUserRule));

                System.out.println("====== ĐÃ TỰ ĐỘNG THÊM 4 RULES DEMO ======");
                System.out.println("   - Rule 'Dev Team Access' (Dành cho Alice, Bob)");
                System.out.println("   - Rule 'DBA Team Access' (Dành cho Alice, Admin)");
                System.out.println("   - Rule 'Super Admin Access' (Dành cho Admin)");
                System.out.println("   - Rule 'Cá nhân - User Only' (Dành riêng cho user, chỉ vào được Web)");
            }

    }
}