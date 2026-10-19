package com.example.ztna_server.config;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("ℹ️  Users đã có, bỏ qua seed.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<User> users = List.of(
                User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("123"))
                        .role("ADMIN")
                        .email("admin@ztna.com")
                        .enabled(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("123"))
                        .role("USER")
                        .email("user@ztna.com")
                        .enabled(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                User.builder()
                        .username("alice")
                        .password(passwordEncoder.encode("alice123"))
                        .role("USER")
                        .email("alice@ztna.com")
                        .enabled(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                User.builder()
                        .username("bob")
                        .password(passwordEncoder.encode("bob123"))
                        .role("USER")
                        .email("bob@ztna.com")
                        .enabled(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                User.builder()
                        .username("locked")
                        .password(passwordEncoder.encode("locked123"))
                        .role("USER")
                        .email("locked@ztna.com")
                        .enabled(false)                 // 👈 tài khoản bị khóa
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        userRepository.saveAll(users);

        System.out.println("✅ Đã seed " + users.size() + " users:");
        System.out.println("   - admin  / 123        (ADMIN)");
        System.out.println("   - user   / 123        (USER)");
        System.out.println("   - alice  / alice123   (USER)");
        System.out.println("   - bob    / bob123     (USER)");
        System.out.println("   - locked / locked123  (USER - BỊ KHÓA)");
    }
}
