package com.tvboot.tivio.common.config;

import com.tvboot.tivio.tv.TvChannel;
import com.tvboot.tivio.tv.tvcategory.TvChannelCategory;
import com.tvboot.tivio.auth.User;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.language.LanguageRepository;
import com.tvboot.tivio.tv.tvcategory.TvChannelCategoryRepository;
import com.tvboot.tivio.tv.TvChannelRepository;
import com.tvboot.tivio.auth.UserRepository;
import com.tvboot.tivio.room.Room;
import com.tvboot.tivio.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(100)
@DependsOn("entityManagerFactory")
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final TvChannelCategoryRepository categoryRepository;
    private final TvChannelRepository channelRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    // Default password for all users (can be changed later)
    private static final String DEFAULT_PASSWORD = "admin123";

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Starting Database Initialization ===");

        try {
            // Step 1: Verify database connection
            verifyDatabaseConnection();

            // Step 2: Initialize all users properly
            initializeUsersWithTransaction();

            // Step 3: Initialize other core data
            initializeLanguagesWithTransaction();
            initializeCategoriesWithTransaction();

            // Step 4: Initialize business data
            initializeBusinessDataSafely();

            log.info("=== Database Initialization Completed Successfully ===");

        } catch (Exception e) {
            log.error("=== Database Initialization Failed ===", e);
            if (isProductionProfile()) {
                throw e;
            }
        }
    }

    /**
     * Verify database connection
     */
    private void verifyDatabaseConnection() throws SQLException {
        log.info("Verifying database connection...");

        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
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
     * Initialize all users with proper transaction management
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeUsersWithTransaction() {
        log.info("Initializing all users in separate transaction...");

        try {
            long userCount = userRepository.count();
            log.info("Current users in database: {}", userCount);

            if (userCount == 0) {
                log.info("No users found, creating all default users...");
                createAllDefaultUsers();
            } else {
                log.info("Users exist, verifying and updating all users...");
                verifyAndUpdateAllUsers();
            }

            // Final verification
            verifyAllUsersCanAuthenticate();

            log.info("✅ All users initialization completed successfully");

        } catch (Exception e) {
            log.error("Error initializing users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize users", e);
        }
    }

    /**
     * Create all default users
     */
    private void createAllDefaultUsers() {
        List<UserData> defaultUsers = Arrays.asList(
                new UserData("admin", "admin@tvboot.com", "System", "Administrator", User.Role.ADMIN),
                new UserData("manager", "manager@tvboot.com", "Hotel", "Manager", User.Role.MANAGER),
                new UserData("receptionist", "receptionist@tvboot.com", "Front", "Desk", User.Role.RECEPTIONIST),
                new UserData("technician", "technician@tvboot.com", "IT", "Technician", User.Role.TECHNICIAN)
        );

        for (UserData userData : defaultUsers) {
            try {
                User user = createAndSaveUser(userData);
                log.info("✅ Created user: {} (ID: {}, Role: {})",
                        user.getUsername(), user.getId(), user.getRole());

                // Immediately verify password encoding
                verifyUserPassword(user, DEFAULT_PASSWORD);

            } catch (Exception e) {
                log.error("❌ Failed to create user: {} - {}", userData.username, e.getMessage());
                throw new RuntimeException("Failed to create user: " + userData.username, e);
            }
        }
    }

    /**
     * Verify and update all existing users
     */
    private void verifyAndUpdateAllUsers() {
        List<String> usernames = Arrays.asList("admin", "manager", "receptionist", "technician");

        for (String username : usernames) {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Check if password needs updating
                if (!passwordEncoder.matches(DEFAULT_PASSWORD, user.getPassword())) {
                    log.warn("⚠️ Password verification failed for user: {}, updating...", username);
                    user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                    userRepository.save(user);
                    log.info("✅ Password updated for user: {}", username);
                } else {
                    log.info("✅ Password verified for user: {}", username);
                }

                // Ensure user is active
                if (!user.isActive()) {
                    user.setActive(true);
                    userRepository.save(user);
                    log.info("✅ Activated user: {}", username);
                }

            } else {
                log.warn("⚠️ User {} not found, creating...", username);
                createMissingUser(username);
            }
        }
    }

    /**
     * Create a missing user based on username
     */
    private void createMissingUser(String username) {
        UserData userData;

        switch (username) {
            case "admin":
                userData = new UserData("admin", "admin@tvboot.com", "System", "Administrator", User.Role.ADMIN);
                break;
            case "manager":
                userData = new UserData("manager", "manager@tvboot.com", "Hotel", "Manager", User.Role.MANAGER);
                break;
            case "receptionist":
                userData = new UserData("receptionist", "receptionist@tvboot.com", "Front", "Desk", User.Role.RECEPTIONIST);
                break;
            case "technician":
                userData = new UserData("technician", "technician@tvboot.com", "IT", "Technician", User.Role.TECHNICIAN);
                break;
            default:
                log.error("❌ Unknown username for creation: {}", username);
                return;
        }

        User user = createAndSaveUser(userData);
        log.info("✅ Created missing user: {} (ID: {})", user.getUsername(), user.getId());
    }

    /**
     * Create and save a user with proper password encoding
     */
    private User createAndSaveUser(UserData userData) {
        // Encode password
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        User user = User.builder()
                .username(userData.username)
                .email(userData.email)
                .password(encodedPassword)
                .firstName(userData.firstName)
                .lastName(userData.lastName)
                .role(userData.role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Verify immediately after saving
        if (!verifyUserPassword(savedUser, DEFAULT_PASSWORD)) {
            throw new RuntimeException("Failed to verify password after creation for user: " + userData.username);
        }

        return savedUser;
    }

    /**
     * Verify user password
     */
    private boolean verifyUserPassword(User user, String plainPassword) {
        boolean matches = passwordEncoder.matches(plainPassword, user.getPassword());

        if (matches) {
            log.debug("✅ Password verification successful for user: {}", user.getUsername());
        } else {
            log.error("❌ Password verification failed for user: {}", user.getUsername());
        }

        return matches;
    }

    /**
     * Final verification that all users can authenticate
     */
    private void verifyAllUsersCanAuthenticate() {
        log.info("Performing final authentication verification for all users...");

        List<String> usernames = Arrays.asList("admin", "manager", "receptionist", "technician");

        for (String username : usernames) {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (verifyUserPassword(user, DEFAULT_PASSWORD)) {
                    log.info("✅ Authentication verified for user: {} (Role: {})",
                            username, user.getRole());
                } else {
                    log.error("❌ Authentication failed for user: {}", username);
                    throw new RuntimeException("Authentication verification failed for user: " + username);
                }
            } else {
                log.error("❌ User not found during verification: {}", username);
                throw new RuntimeException("User not found during verification: " + username);
            }
        }

        log.info("✅ All users can authenticate successfully");
    }

    /**
     * Helper class for user data
     */
    private static class UserData {
        final String username;
        final String email;
        final String firstName;
        final String lastName;
        final User.Role role;

        UserData(String username, String email, String firstName, String lastName, User.Role role) {
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
        }
    }

    /**
     * Initialize languages with proper transaction management
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeLanguagesWithTransaction() {
        log.info("Initializing languages in separate transaction...");

        try {
            if (languageRepository.count() == 0) {
                log.info("Creating default languages...");

                List<Language> languages = List.of(
                        Language.builder()
                                .name("English")
                                .nativeName("English")
                                .iso6391("en")
                                .iso6392("eng")
                                .localeCode("en-US")
                                .charset("UTF-8")
                                .isRtl(false)
                                .isActive(true)
                                .isDefault(true)
                                .isAdminEnabled(true)
                                .isGuestEnabled(true)
                                .displayOrder(1)
                                .fontFamily("Arial, sans-serif")
                                .currencyCode("USD")
                                .currencySymbol("$")
                                .dateFormat("MM/dd/yyyy")
                                .timeFormat("hh:mm a")
                                .numberFormat("#,##0.00")
                                .decimalSeparator('.')
                                .thousandsSeparator(',')
                                .uiTranslationProgress(100)
                                .channelTranslationProgress(95)
                                .epgTranslationEnabled(true)
                                .welcomeMessage("Welcome to our hotel entertainment system!")
                                .supportedPlatforms(Set.of("TIZEN", "WEBOS", "ANDROID", "WEB", "IOS"))
                                .build(),

                        Language.builder()
                                .name("Arabic")
                                .nativeName("العربية")
                                .iso6391("ar")
                                .iso6392("ara")
                                .localeCode("ar-SA")
                                .charset("UTF-8")
                                .isRtl(true)
                                .isActive(true)
                                .isDefault(false)
                                .isAdminEnabled(true)
                                .isGuestEnabled(true)
                                .displayOrder(2)
                                .fontFamily("Arial, Noto Sans Arabic")
                                .currencyCode("SAR")
                                .currencySymbol("ر.س")
                                .dateFormat("yyyy/MM/dd")
                                .timeFormat("HH:mm")
                                .numberFormat("#,##0.00")
                                .decimalSeparator('.')
                                .thousandsSeparator(',')
                                .uiTranslationProgress(98)
                                .channelTranslationProgress(90)
                                .epgTranslationEnabled(true)
                                .welcomeMessage("مرحباً بكم في نظام الترفيه بالفندق!")
                                .supportedPlatforms(Set.of("TIZEN", "WEBOS", "ANDROID", "WEB", "IOS"))
                                .build(),

                        Language.builder()
                                .name("French")
                                .nativeName("Français")
                                .iso6391("fr")
                                .iso6392("fra")
                                .localeCode("fr-FR")
                                .charset("UTF-8")
                                .isRtl(false)
                                .isActive(true)
                                .isDefault(false)
                                .isAdminEnabled(true)
                                .isGuestEnabled(true)
                                .displayOrder(3)
                                .fontFamily("Arial, sans-serif")
                                .currencyCode("EUR")
                                .currencySymbol("€")
                                .dateFormat("dd/MM/yyyy")
                                .timeFormat("HH:mm")
                                .numberFormat("# ##0,00")
                                .decimalSeparator(',')
                                .thousandsSeparator(' ')
                                .uiTranslationProgress(100)
                                .channelTranslationProgress(88)
                                .epgTranslationEnabled(true)
                                .welcomeMessage("Bienvenue dans notre système de divertissement hôtelier!")
                                .supportedPlatforms(Set.of("TIZEN", "WEBOS", "ANDROID", "WEB", "IOS"))
                                .build()
                );

                List<Language> savedLanguages = languageRepository.saveAll(languages);
                log.info("✅ Created {} languages", savedLanguages.size());
            } else {
                log.info("Languages already initialized ({})", languageRepository.count());
            }
        } catch (Exception e) {
            log.error("Error initializing languages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize languages", e);
        }
    }

    /**
     * Initialize categories with proper transaction management
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeCategoriesWithTransaction() {
        log.info("Initializing categories in separate transaction...");

        try {
            if (categoryRepository.count() == 0) {
                log.info("Creating default categories...");

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

                List<TvChannelCategory> savedCategories = categoryRepository.saveAll(categories);
                log.info("✅ Created {} categories", savedCategories.size());
            } else {
                log.info("Categories already initialized ({})", categoryRepository.count());
            }
        } catch (Exception e) {
            log.error("Error initializing categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize categories", e);
        }
    }

    /**
     * Initialize business data safely
     */
    private void initializeBusinessDataSafely() {
        log.info("Initializing business data...");

        try {
            initializeSampleChannelsWithTransaction();
            initializeSampleRoomsWithTransaction();
            log.info("✅ Business data initialization completed");
        } catch (Exception e) {
            log.error("Error in business data initialization: {}", e.getMessage(), e);
            log.warn("⚠️ Business data initialization failed, but continuing...");
        }
    }

    /**
     * Initialize sample channels
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeSampleChannelsWithTransaction() {
        if (isProductionProfile()) {
            log.info("Skipping sample channels in production profile");
            return;
        }

        log.info("Initializing sample channels in separate transaction...");

        try {
            if (channelRepository.count() == 0) {
                log.info("Creating sample TV channels...");

                Language english = languageRepository.findByIso6391("en")
                        .orElseThrow(() -> new RuntimeException("English language not found"));

                TvChannelCategory news = categoryRepository.findByName("News")
                        .orElseThrow(() -> new RuntimeException("News category not found"));
                TvChannelCategory sports = categoryRepository.findByName("Sports")
                        .orElseThrow(() -> new RuntimeException("Sports category not found"));

                List<TvChannel> channels = List.of(
                        TvChannel.builder()
                                .channelNumber(101)
                                .name("CNN International")
                                .description("International news channel")
                                .ip("192.168.1.100")
                                .port(8001)
                                .sortOrder(1)
                                .category(news)
                                .language(english)
                                .build(),
                        TvChannel.builder()
                                .channelNumber(102)
                                .name("BBC World News")
                                .description("British news channel")
                                .ip("192.168.1.101")
                                .port(8002)
                                .sortOrder(2)
                                .category(news)
                                .language(english)
                                .build(),
                        TvChannel.builder()
                                .channelNumber(201)
                                .name("ESPN")
                                .description("Sports entertainment channel")
                                .ip("192.168.1.103")
                                .port(8004)
                                .sortOrder(3)
                                .category(sports)
                                .language(english)
                                .build()
                );

                List<TvChannel> savedChannels = channelRepository.saveAll(channels);
                log.info("✅ Created {} sample channels", savedChannels.size());
            } else {
                log.info("TV channels already exist ({})", channelRepository.count());
            }
        } catch (Exception e) {
            log.error("Error initializing sample channels: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize sample channels", e);
        }
    }

    /**
     * Initialize sample rooms
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeSampleRoomsWithTransaction() {
        if (isProductionProfile()) {
            log.info("Skipping sample rooms in production profile");
            return;
        }

        log.info("Initializing sample rooms in separate transaction...");

        try {
            if (roomRepository.count() == 0) {
                log.info("Creating sample rooms...");

                List<Room> rooms = List.of(
                        Room.builder()
                                .roomNumber("101")
                                .roomType(Room.RoomType.STANDARD)
                                .floorNumber(1)
                                .building("Main Building")
                                .capacity(2)
                                .pricePerNight(new BigDecimal("89.99"))
                                .status(Room.RoomStatus.AVAILABLE)
                                .description("Standard room with city view")
                                .amenities(List.of("WiFi", "TV", "Air Conditioning", "Mini Bar"))
                                .build(),

                        Room.builder()
                                .roomNumber("201")
                                .roomType(Room.RoomType.DELUXE)
                                .floorNumber(2)
                                .building("Main Building")
                                .capacity(3)
                                .pricePerNight(new BigDecimal("129.99"))
                                .status(Room.RoomStatus.AVAILABLE)
                                .description("Deluxe room with balcony")
                                .amenities(List.of("WiFi", "Smart TV", "Air Conditioning", "Mini Bar", "Balcony"))
                                .build(),

                        Room.builder()
                                .roomNumber("301")
                                .roomType(Room.RoomType.SUITE)
                                .floorNumber(3)
                                .building("Main Building")
                                .capacity(4)
                                .pricePerNight(new BigDecimal("199.99"))
                                .status(Room.RoomStatus.AVAILABLE)
                                .description("Executive suite with living room")
                                .amenities(List.of("WiFi", "Smart TV", "Air Conditioning", "Mini Bar", "Living Room"))
                                .build()
                );

                List<Room> savedRooms = roomRepository.saveAll(rooms);
                log.info("✅ Created {} sample rooms", savedRooms.size());

            } else {
                log.info("Rooms already exist ({})", roomRepository.count());
            }
        } catch (Exception e) {
            log.error("Error initializing sample rooms: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize sample rooms", e);
        }
    }

    /**
     * Check if we're running in production profile
     */
    private boolean isProductionProfile() {
        return System.getProperty("spring.profiles.active", "").contains("prod");
    }
}