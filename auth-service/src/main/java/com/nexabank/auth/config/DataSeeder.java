package com.nexabank.auth.config;

import com.nexabank.auth.entity.User;
import com.nexabank.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                .username("admin")
                .email("admin@nexabank.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(User.Role.ROLE_ADMIN)
                .enabled(true)
                .build());
            log.info("Demo admin user created: admin / Admin@1234");
        }

        if (!userRepository.existsByUsername("user1")) {
            userRepository.save(User.builder()
                .username("user1")
                .email("user1@nexabank.com")
                .password(passwordEncoder.encode("User@1234"))
                .role(User.Role.ROLE_CUSTOMER)
                .enabled(true)
                .build());
            log.info("Demo customer created: user1 / User@1234");
        }
    }
}
