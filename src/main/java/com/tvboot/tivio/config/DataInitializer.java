package com.tvboot.tivio.config;

import com.tvboot.tivio.entities.User;
import com.tvboot.tivio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Starting Data Initialization ===");

        try {
            // Check database connection
            long userCount = userRepository.count();
            log.info("Current users in database: {}", userCount);

            // Create admin user if it doesn't exist
            if (!userRepository.existsByUsername("admin")) {
                log.info("Creating default admin user...");

                String rawPassword = "admin123";
                String encodedPassword = passwordEncoder.encode(rawPassword);

                log.info("Raw password: {}", rawPassword);
                log.info("Encoded password: {}", encodedPassword);

                User admin = User.builder()
                        .username("admin")
                        .email("admin@tvboot.com")
                        .password(encodedPassword)
                        .firstName("System")
                        .lastName("Administrator")
                        .role(User.Role.ADMIN)
                        .isActive(true)
                        .build();

                User savedAdmin = userRepository.save(admin);
                log.info("Admin user created successfully with ID: {}", savedAdmin.getId());

                // Test password verification
                boolean passwordMatches = passwordEncoder.matches(rawPassword, savedAdmin.getPassword());
                log.info("Password verification test: {}", passwordMatches ? "SUCCESS" : "FAILED");

            } else {
                log.info("Admin user already exists");

                // Get existing admin and test password
                User existingAdmin = userRepository.findByUsername("admin").orElse(null);
                if (existingAdmin != null) {
                    log.info("Existing admin user found: {}", existingAdmin.getUsername());
                    log.info("Admin user active: {}", existingAdmin.isActive());
                    log.info("Admin user role: {}", existingAdmin.getRole());

                    // Test password verification with existing user
                    boolean passwordMatches = passwordEncoder.matches("admin123", existingAdmin.getPassword());
                    log.info("Existing admin password verification: {}", passwordMatches ? "SUCCESS" : "FAILED");

                    if (!passwordMatches) {
                        log.warn("Existing admin password doesn't match! Updating password...");
                        existingAdmin.setPassword(passwordEncoder.encode("admin123"));
                        userRepository.save(existingAdmin);
                        log.info("Admin password updated successfully");
                    }
                }
            }

            // Final count
            long finalUserCount = userRepository.count();
            log.info("Final users in database: {}", finalUserCount);

        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
            throw e;
        }

        log.info("=== Data Initialization Complete ===");
    }
}