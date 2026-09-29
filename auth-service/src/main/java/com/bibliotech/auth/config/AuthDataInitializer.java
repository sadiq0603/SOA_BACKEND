package com.bibliotech.auth.config;

import com.bibliotech.auth.entity.Role;
import com.bibliotech.auth.entity.User;
import com.bibliotech.auth.entity.UserStatus;
import com.bibliotech.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@bibliotech.com")) {
            log.info("Creating default admin account (admin@bibliotech.com)...");
            userRepository.save(User.builder()
                    .name("Administrator")
                    .email("admin@bibliotech.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build());
        }

        if (!userRepository.existsByEmail("student@bibliotech.com")) {
            log.info("Creating default student account (student@bibliotech.com)...");
            userRepository.save(User.builder()
                    .name("Demo Student")
                    .email("student@bibliotech.com")
                    .password(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .status(UserStatus.ACTIVE)
                    .build());
        }
    }
}
