package com.tvboot.tivio.config;

import com.tvboot.tivio.entities.*;
import com.tvboot.tivio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(100) // Run after other initializers
@DependsOn("entityManagerFactory") // Wait for JPA to be ready
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final TvChannelCategoryRepository categoryRepository;
    private final TvChannelRepository channelRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== Starting Database Initialization ===");

        try {
            // Step 1: Verify database connection
            verifyDatabaseConnection();

            // Step 2: Initialize core data (users, languages, categories)
            initializeCoreData();

            // Step 3: Initialize business data (channels, rooms)
            initializeBusinessData();

            log.info("=== Database Initialization Completed Successfully ===");

        } catch (Exception e) {
            log.error("=== Database Initialization Failed ===", e);
            // Don't rethrow - let application start but log the issue
            // In production, you might want to fail fast
        }
    }

    /**
     * STEP 1: Verify database connection
     * WHY: Ensures database is accessible before trying to initialize data
     */
    private void verifyDatabaseConnection() throws SQLException {
        log.info("Verifying database connection...");

        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) { // 5 second timeout
                log.info("✅ Database connection verified successfully");
                log.info("Database URL: {}", connection.getMetaData().getURL());
                log.info("Database Product: {} {}",
                        connection.getMetaData().getDatabaseProductName(),
                        connection.getMetaData().getDatabaseProductVersion());
            } else {
                throw new SQLException("Database connection is not valid");
            }
        } catch (SQLException e) {
            log.error("❌ Database connection failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * STEP 2: Initialize core data (Users, Languages, Categories)
     * WHY: These are foundation entities that other entities depend on
     */
    private void initializeCoreData() {
        log.info("Initializing core data...");

        // Initialize users first
        initializeUsers();

        // Initialize languages
        initializeLanguages();

        // Initialize categories
        initializeCategories();

        log.info("✅ Core data initialization completed");
    }

    /**
     * Initialize default users with proper error handling
     */
    private void initializeUsers() {
        log.info("Initializing users...");

        try {
            long userCount = userRepository.count();
            log.info("Current users in database: {}", userCount);

            if (userCount == 0) {
                log.info("No users found, creating default users...");
                createDefaultUsers();
            } else {
                log.info("Users already exist, verifying admin user...");
                verifyAdminUser();
            }

        } catch (Exception e) {
            log.error("Error initializing users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize users", e);
        }
    }

    /**
     * Create default users
     */
    private void createDefaultUsers() {
        List<User> defaultUsers = List.of(
                createUser("admin", "admin@tvboot.com", "admin123", "System", "Administrator", User.Role.ADMIN),
                createUser("manager", "manager@tvboot.com", "admin123", "Hotel", "Manager", User.Role.MANAGER),
                createUser("receptionist", "receptionist@tvboot.com", "admin123", "Front", "Desk", User.Role.RECEPTIONIST),
                createUser("technician", "technician@tvboot.com", "admin123", "IT", "Technician", User.Role.TECHNICIAN)
        );

        for (User user : defaultUsers) {
            try {
                User saved = userRepository.save(user);
                log.info("✅ Created user: {} (ID: {})", saved.getUsername(), saved.getId());

                // Verify password encoding worked
                boolean passwordMatches = passwordEncoder.matches("admin123", saved.getPassword());
                if (!passwordMatches) {
                    log.error("❌ Password encoding failed for user: {}", saved.getUsername());
                } else {
                    log.debug("✅ Password verification successful for user: {}", saved.getUsername());
                }

            } catch (Exception e) {
                log.error("❌ Failed to create user: {} - {}", user.getUsername(), e.getMessage());
            }
        }
    }

    /**
     * Helper method to create a user
     */
    private User createUser(String username, String email, String password,
                            String firstName, String lastName, User.Role role) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isActive(true)
                .build();
    }

    /**
     * Verify admin user exists and password works
     */
    private void verifyAdminUser() {
        userRepository.findByUsername("admin")
                .ifPresentOrElse(
                        admin -> {
                            boolean passwordMatches = passwordEncoder.matches("admin123", admin.getPassword());
                            if (passwordMatches) {
                                log.info("✅ Admin user verified successfully");
                            } else {
                                log.warn("⚠️ Admin password verification failed, updating...");
                                admin.setPassword(passwordEncoder.encode("admin123"));
                                userRepository.save(admin);
                                log.info("✅ Admin password updated successfully");
                            }
                        },
                        () -> {
                            log.warn("⚠️ Admin user not found, creating...");
                            User admin = createUser("admin", "admin@tvboot.com", "admin123",
                                    "System", "Administrator", User.Role.ADMIN);
                            userRepository.save(admin);
                            log.info("✅ Admin user created successfully");
                        }
                );
    }

    /**
     * Initialize languages
     */
    private void initializeLanguages() {
        if (languageRepository.count() == 0) {
            log.info("Initializing languages...");

            List<Language> languages = List.of(
                    Language.builder().name("English").code("EN").build(),
                    Language.builder().name("French").code("FR").build(),
                    Language.builder().name("Arabic").code("AR").build(),
                    Language.builder().name("Spanish").code("ES").build(),
                    Language.builder().name("German").code("DE").build()
            );

            languageRepository.saveAll(languages);
            log.info("✅ Created {} languages", languages.size());
        } else {
            log.info("Languages already initialized ({})", languageRepository.count());
        }
    }

    /**
     * Initialize TV channel categories
     */
    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            log.info("Initializing TV channel categories...");

            List<TvChannelCategory> categories = List.of(
                    TvChannelCategory.builder()
                            .name("News")
                            .description("News and current affairs channels")
                            .iconUrl("fas fa-newspaper")
                            .build(),
                    TvChannelCategory.builder()
                            .name("Sports")
                            .description("Sports and athletics channels")
                            .iconUrl("fas fa-football-ball")
                            .build(),
                    TvChannelCategory.builder()
                            .name("Entertainment")
                            .description("Movies and entertainment channels")
                            .iconUrl("fas fa-film")
                            .build(),
                    TvChannelCategory.builder()
                            .name("Kids")
                            .description("Children and family channels")
                            .iconUrl("fas fa-child")
                            .build(),
                    TvChannelCategory.builder()
                            .name("Documentary")
                            .description("Documentary and educational channels")
                            .iconUrl("fas fa-graduation-cap")
                            .build()
            );

            categoryRepository.saveAll(categories);
            log.info("✅ Created {} categories", categories.size());
        } else {
            log.info("Categories already initialized ({})", categoryRepository.count());
        }
    }

    /**
     * STEP 3: Initialize business data
     * WHY: These depend on core data being present
     */
    private void initializeBusinessData() {
        log.info("Initializing business data...");

        try {
            // Initialize sample channels
            initializeSampleChannels();

            // Initialize sample rooms
            initializeSampleRooms();

            log.info("✅ Business data initialization completed");

        } catch (Exception e) {
            log.error("Error initializing business data: {}", e.getMessage(), e);
            // Don't fail the application, just log the error
        }
    }

    /**
     * Initialize sample TV channels
     */
    @Profile("!prod") // Only in non-production environments
    private void initializeSampleChannels() {
        if (channelRepository.count() == 0) {
            log.info("Initializing sample TV channels...");

            // Get references to languages and categories
            Language english = languageRepository.findByCode("EN").orElse(null);
            Language arabic = languageRepository.findByCode("AR").orElse(null);
            TvChannelCategory news = categoryRepository.findByName("News").orElse(null);
            TvChannelCategory sports = categoryRepository.findByName("Sports").orElse(null);
            TvChannelCategory entertainment = categoryRepository.findByName("Entertainment").orElse(null);

            if (english != null && news != null) {
                List<TvChannel> channels = List.of(
                        TvChannel.builder()
                                .channelNumber(101)
                                .name("CNN International")
                                .description("International news channel")
                                .ip("192.168.1.100")
                                .port(8001)
                                .category(news)
                                .language(english)
                                .build(),
                        TvChannel.builder()
                                .channelNumber(102)
                                .name("BBC World News")
                                .description("British news channel")
                                .ip("192.168.1.100")
                                .port(8002)
                                .category(news)
                                .language(english)
                                .build(),
                        TvChannel.builder()
                                .channelNumber(201)
                                .name("ESPN")
                                .description("Sports entertainment channel")
                                .ip("192.168.1.101")
                                .port(8001)
                                .category(sports)
                                .language(english)
                                .build()
                );

                channelRepository.saveAll(channels);
                log.info("✅ Created {} sample channels", channels.size());
            } else {
                log.warn("⚠️ Cannot create sample channels - missing dependencies");
            }
        } else {
            log.info("TV channels already exist ({})", channelRepository.count());
        }
    }

    /**
     * Initialize sample rooms
     */
    @Profile("!prod") // Only in non-production environments
    private void initializeSampleRooms() {
        if (roomRepository.count() == 0) {
            log.info("Initializing sample rooms...");

            List<Room> rooms = List.of(
                    Room.builder()
                            .roomNumber("101")
                            .roomType(Room.RoomType.STANDARD)
                            .floorNumber(1)
                            .building("Main Building")
                            .maxOccupancy(2)
                            .pricePerNight(new BigDecimal("89.99"))
                            .status(Room.RoomStatus.AVAILABLE)
                            .description("Standard room with city view")
                            .build(),
                    Room.builder()
                            .roomNumber("201")
                            .roomType(Room.RoomType.DELUXE)
                            .floorNumber(2)
                            .building("Main Building")
                            .maxOccupancy(3)
                            .pricePerNight(new BigDecimal("129.99"))
                            .status(Room.RoomStatus.AVAILABLE)
                            .description("Deluxe room with balcony")
                            .build(),
                    Room.builder()
                            .roomNumber("301")
                            .roomType(Room.RoomType.SUITE)
                            .floorNumber(3)
                            .building("Main Building")
                            .maxOccupancy(4)
                            .pricePerNight(new BigDecimal("199.99"))
                            .status(Room.RoomStatus.AVAILABLE)
                            .description("Executive suite with living room")
                            .build()
            );

            roomRepository.saveAll(rooms);
            log.info("✅ Created {} sample rooms", rooms.size());
        } else {
            log.info("Rooms already exist ({})", roomRepository.count());
        }
    }
}