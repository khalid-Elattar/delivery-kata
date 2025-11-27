package com.crafteam.delivery.infrastructure.config;

import com.crafteam.delivery.domain.model.user.UserRole;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.repository.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

/**
 * Configuration to initialize default users in the database.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeUsers(UserR2dbcRepository userRepository,
                                              PasswordEncoder passwordEncoder) {
        return args -> {
            // Create admin user if not exists
            userRepository.existsByEmail("admin@delivery.com")
                    .filter(exists -> !exists)
                    .flatMap(notExists -> {
                        log.info("Creating default admin user: admin@delivery.com");
                        UserEntity admin = UserEntity.builder()
                                .id(UUID.randomUUID())
                                .firstName("Admin")
                                .lastName("System")
                                .email("admin@delivery.com")
                                .password(passwordEncoder.encode("admin123"))
                                .role(UserRole.ADMIN.name())
                                .active(true)
                                .createdAt(Instant.now())
                                .isNew(true)
                                .build();
                        return userRepository.save(admin);
                    })
                    .doOnSuccess(user -> {
                        if (user != null) {
                            log.info("Admin user created successfully: id={}", user.getId());
                        }
                    })
                    .doOnError(error -> log.error("Failed to create admin user", error))
                    .subscribe();

            // Create regular user if not exists
            userRepository.existsByEmail("user@delivery.com")
                    .filter(exists -> !exists)
                    .flatMap(notExists -> {
                        log.info("Creating default user: user@delivery.com");
                        UserEntity user = UserEntity.builder()
                                .id(UUID.randomUUID())
                                .firstName("John")
                                .lastName("Doe")
                                .email("user@delivery.com")
                                .password(passwordEncoder.encode("user123"))
                                .street("123 Main St")
                                .city("Paris")
                                .zipCode("75001")
                                .country("France")
                                .phoneNumber("+33612345678")
                                .role(UserRole.USER.name())
                                .active(true)
                                .createdAt(Instant.now())
                                .isNew(true)
                                .build();
                        return userRepository.save(user);
                    })
                    .doOnSuccess(user -> {
                        if (user != null) {
                            log.info("Regular user created successfully: id={}", user.getId());
                        }
                    })
                    .doOnError(error -> log.error("Failed to create regular user", error))
                    .subscribe();
        };
    }
}
