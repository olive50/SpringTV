package com.tvboot.tivio.config;

import com.tvboot.tivio.entities.TvChannel;
import com.tvboot.tivio.entities.TvChannelCategory;
import com.tvboot.tivio.entities.User;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.language.LanguageRepository;
import com.tvboot.tivio.repository.TvChannelCategoryRepository;
import com.tvboot.tivio.repository.TvChannelRepository;
import com.tvboot.tivio.repository.UserRepository;
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
import java.util.List;
import java.util.Set;

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
    public void run(String... args) throws Exception {
        log.info("=== Starting Database Initialization ===");

        try {
            // Step 1: Verify database connection
            verifyDatabaseConnection();

            // Step 2: Initialize core data (users, languages, categories)
            // Use separate transactions to avoid long-running transactions
            initializeCoreDataSafely();

            // Step 3: Initialize business data (channels, rooms)
            initializeBusinessDataSafely();

            log.info("=== Database Initialization Completed Successfully ===");

        } catch (Exception e) {
            log.error("=== Database Initialization Failed ===", e);
            // Don't rethrow in development - let application start
            if (isProductionProfile()) {
                throw e;
            }
        }
    }

    /**
     * STEP 1: Verify database connection without transaction
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
                log.info("Auto-commit mode: {}", connection.getAutoCommit());
            } else {
                throw new SQLException("Database connection is not valid");
            }
        } catch (SQLException e) {
            log.error("❌ Database connection failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * STEP 2: Initialize core data with separate transactions
     */
    private void initializeCoreDataSafely() {
        log.info("Initializing core data with safe transactions...");

        try {
            initializeUsersWithTransaction();
            initializeLanguagesWithTransaction();
            initializeCategoriesWithTransaction();
            log.info("✅ Core data initialization completed");
        } catch (Exception e) {
            log.error("Error in core data initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Core data initialization failed", e);
        }
    }

    /**
     * Initialize users with proper transaction management
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeUsersWithTransaction() {
        log.info("Initializing users in separate transaction...");

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

            log.info("✅ Users initialization completed successfully");

        } catch (Exception e) {
            log.error("Error initializing users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize users", e);
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
                log.info("Creating comprehensive languages...");

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
                                .build(),

                        Language.builder()
                                .name("Spanish")
                                .nativeName("Español")
                                .iso6391("es")
                                .iso6392("spa")
                                .localeCode("es-ES")
                                .charset("UTF-8")
                                .isRtl(false)
                                .isActive(true)
                                .isDefault(false)
                                .isAdminEnabled(true)
                                .isGuestEnabled(true)
                                .displayOrder(4)
                                .fontFamily("Arial, sans-serif")
                                .currencyCode("EUR")
                                .currencySymbol("€")
                                .dateFormat("dd/MM/yyyy")
                                .timeFormat("HH:mm")
                                .numberFormat("#,##0.00")
                                .decimalSeparator(',')
                                .thousandsSeparator('.')
                                .uiTranslationProgress(95)
                                .channelTranslationProgress(85)
                                .epgTranslationEnabled(true)
                                .welcomeMessage("¡Bienvenido a nuestro sistema de entretenimiento hotelero!")
                                .supportedPlatforms(Set.of("TIZEN", "WEBOS", "ANDROID", "WEB", "IOS"))
                                .build(),

                        Language.builder()
                                .name("German")
                                .nativeName("Deutsch")
                                .iso6391("de")
                                .iso6392("deu")
                                .localeCode("de-DE")
                                .charset("UTF-8")
                                .isRtl(false)
                                .isActive(true)
                                .isDefault(false)
                                .isAdminEnabled(true)
                                .isGuestEnabled(true)
                                .displayOrder(5)
                                .fontFamily("Arial, sans-serif")
                                .currencyCode("EUR")
                                .currencySymbol("€")
                                .dateFormat("dd.MM.yyyy")
                                .timeFormat("HH:mm")
                                .numberFormat("#.##0,00")
                                .decimalSeparator(',')
                                .thousandsSeparator('.')
                                .uiTranslationProgress(92)
                                .channelTranslationProgress(80)
                                .epgTranslationEnabled(true)
                                .welcomeMessage("Willkommen in unserem Hotel-Unterhaltungssystem!")
                                .supportedPlatforms(Set.of("TIZEN", "WEBOS", "ANDROID", "WEB", "IOS"))
                                .build()
                );

                List<Language> savedLanguages = languageRepository.saveAll(languages);
                log.info("✅ Created {} comprehensive languages", savedLanguages.size());
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
     * STEP 3: Initialize business data with safe transactions
     */
    private void initializeBusinessDataSafely() {
        log.info("Initializing business data with safe transactions...");

        try {
            initializeSampleChannelsWithTransaction();
            initializeSampleRoomsWithTransaction();
            log.info("✅ Business data initialization completed");
        } catch (Exception e) {
            log.error("Error in business data initialization: {}", e.getMessage(), e);
            // Don't fail the application for sample data issues
            log.warn("⚠️ Business data initialization failed, but continuing...");
        }
    }

    /**
     * Initialize sample channels with proper transaction management
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeSampleChannelsWithTransaction() {
        // Only create sample data in development
        if (isProductionProfile()) {
            log.info("Skipping sample channels in production profile");
            return;
        }

        log.info("Initializing sample channels in separate transaction...");

        try {
            if (channelRepository.count() == 0) {
                log.info("Creating sample TV channels...");

                // Get references to languages and categories
                Language english = languageRepository.findByIso6391("en")
                        .orElseThrow(() -> new RuntimeException("English language not found"));
                Language arabic = languageRepository.findByIso6391("ar")
                        .orElseThrow(() -> new RuntimeException("Arabic language not found"));

                TvChannelCategory news = categoryRepository.findByName("News")
                        .orElseThrow(() -> new RuntimeException("News category not found"));
                TvChannelCategory sports = categoryRepository.findByName("Sports")
                        .orElseThrow(() -> new RuntimeException("Sports category not found"));
                TvChannelCategory entertainment = categoryRepository.findByName("Entertainment")
                        .orElseThrow(() -> new RuntimeException("Entertainment category not found"));

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
                                .ip("192.168.1.101")
                                .port(8002)
                                .category(news)
                                .language(english)
                                .build(),
                        TvChannel.builder()
                                .channelNumber(103)
                                .name("Al Jazeera English")
                                .description("Qatari international news channel")
                                .ip("192.168.1.102")
                                .port(8003)
                                .category(news)
                                .language(english)
                                .build(),
                        TvChannel.builder()
                                .channelNumber(201)
                                .name("ESPN")
                                .description("Sports entertainment channel")
                                .ip("192.168.1.103")
                                .port(8004)
                                .category(sports)
                                .language(english)
                                .build(),
                        TvChannel.builder()
                                .channelNumber(202)
                                .name("beIN Sports")
                                .description("International sports channel")
                                .ip("192.168.1.104")
                                .port(8005)
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
     * Initialize sample rooms with proper transaction management
     */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeSampleRoomsWithTransaction() {
        // Only create sample data in development
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
                                .roomNumber("102")
                                .roomType(Room.RoomType.STANDARD)
                                .floorNumber(1)
                                .building("Main Building")
                                .capacity(2)
                                .pricePerNight(new BigDecimal("89.99"))
                                .status(Room.RoomStatus.AVAILABLE)
                                .description("Standard room with garden view")
                                .amenities(List.of("WiFi", "TV", "Air Conditioning"))
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
                                .amenities(List.of("WiFi", "Smart TV", "Air Conditioning", "Mini Bar", "Balcony", "Coffee Maker"))
                                .build(),

                        Room.builder()
                                .roomNumber("202")
                                .roomType(Room.RoomType.DELUXE)
                                .floorNumber(2)
                                .building("Main Building")
                                .capacity(3)
                                .pricePerNight(new BigDecimal("139.99"))
                                .status(Room.RoomStatus.OCCUPIED)
                                .description("Deluxe room with ocean view")
                                .amenities(List.of("WiFi", "Smart TV", "Air Conditioning", "Mini Bar", "Ocean View", "Coffee Maker"))
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
                                .amenities(List.of("WiFi", "Smart TV", "Air Conditioning", "Mini Bar", "Living Room", "Kitchenette", "Jacuzzi"))
                                .build(),

                        Room.builder()
                                .roomNumber("302")
                                .roomType(Room.RoomType.JUNIOR_SUITE)
                                .floorNumber(3)
                                .building("Main Building")
                                .capacity(3)
                                .pricePerNight(new BigDecimal("169.99"))
                                .status(Room.RoomStatus.AVAILABLE)
                                .description("Junior suite with separate seating area")
                                .amenities(List.of("WiFi", "Smart TV", "Air Conditioning", "Mini Bar", "Seating Area"))
                                .build(),

                        Room.builder()
                                .roomNumber("401")
                                .roomType(Room.RoomType.PRESIDENTIAL_SUITE)
                                .floorNumber(4)
                                .building("Main Building")
                                .capacity(6)
                                .pricePerNight(new BigDecimal("399.99"))
                                .status(Room.RoomStatus.AVAILABLE)
                                .description("Presidential suite with panoramic views")
                                .amenities(List.of("WiFi", "Multiple Smart TVs", "Air Conditioning", "Full Bar", "Dining Room", "Kitchen", "Jacuzzi", "Private Balcony"))
                                .build(),

                        Room.builder()
                                .roomNumber("501")
                                .roomType(Room.RoomType.FAMILY_ROOM)
                                .floorNumber(5)
                                .building("Main Building")
                                .capacity(5)
                                .pricePerNight(new BigDecimal("159.99"))
                                .status(Room.RoomStatus.MAINTENANCE)
                                .description("Family room with extra beds")
                                .amenities(List.of("WiFi", "TV", "Air Conditioning", "Extra Beds", "Refrigerator"))
                                .build(),

                        Room.builder()
                                .roomNumber("103")
                                .roomType(Room.RoomType.SINGLE)
                                .floorNumber(1)
                                .building("Annex Building")
                                .capacity(1)
                                .pricePerNight(new BigDecimal("69.99"))
                                .status(Room.RoomStatus.AVAILABLE)
                                .description("Single room for solo travelers")
                                .amenities(List.of("WiFi", "TV", "Air Conditioning"))
                                .build(),

                        Room.builder()
                                .roomNumber("104")
                                .roomType(Room.RoomType.DOUBLE)
                                .floorNumber(1)
                                .building("Annex Building")
                                .capacity(2)
                                .pricePerNight(new BigDecimal("79.99"))
                                .status(Room.RoomStatus.CLEANING)
                                .description("Double room with queen bed")
                                .amenities(List.of("WiFi", "TV", "Air Conditioning"))
                                .build(),

                        Room.builder()
                                .roomNumber("105")
                                .roomType(Room.RoomType.TWIN)
                                .floorNumber(1)
                                .building("Annex Building")
                                .capacity(2)
                                .pricePerNight(new BigDecimal("79.99"))
                                .status(Room.RoomStatus.AVAILABLE)
                                .description("Twin room with two single beds")
                                .amenities(List.of("WiFi", "TV", "Air Conditioning"))
                                .build(),

                        Room.builder()
                                .roomNumber("203")
                                .roomType(Room.RoomType.DELUXE)
                                .floorNumber(2)
                                .building("Annex Building")
                                .capacity(3)
                                .pricePerNight(new BigDecimal("119.99"))
                                .status(Room.RoomStatus.OUT_OF_ORDER)
                                .description("Deluxe room currently under renovation")
                                .amenities(List.of("WiFi", "TV", "Air Conditioning"))
                                .build()
                );

                List<Room> savedRooms = roomRepository.saveAll(rooms);
                log.info("✅ Created {} sample rooms", savedRooms.size());

                // Log some statistics
                logRoomStatistics();

            } else {
                log.info("Rooms already exist ({})", roomRepository.count());
                logRoomStatistics();
            }
        } catch (Exception e) {
            log.error("Error initializing sample rooms: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize sample rooms", e);
        }
    }

    private void logRoomStatistics() {
        try {
//            long totalRooms = roomRepository.countAllRooms();
//            long availableRooms = roomRepository.countAvailableRooms();
//            long occupiedRooms = roomRepository.countOccupiedRooms();
//            long maintenanceRooms = roomRepository.countMaintenanceRooms();
//            long cleaningRooms = roomRepository.countCleaningRooms();

            log.info("📊 Room Statistics:");
//            log.info("   Total rooms: {}", totalRooms);
//            log.info("   Available: {}", availableRooms);
//            log.info("   Occupied: {}", occupiedRooms);
//            log.info("   Maintenance: {}", maintenanceRooms);
//            log.info("   Cleaning: {}", cleaningRooms);
//            log.info("   Out of Order: {}", totalRooms - (availableRooms + occupiedRooms + maintenanceRooms + cleaningRooms));
        } catch (Exception e) {
            log.warn("Could not log room statistics: {}", e.getMessage());
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
     * Helper method to create a user with encoded password
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
     * Check if we're running in production profile
     */
    private boolean isProductionProfile() {
        return System.getProperty("spring.profiles.active", "").contains("prod");
    }
}