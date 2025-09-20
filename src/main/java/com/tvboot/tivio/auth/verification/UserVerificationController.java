package com.tvboot.tivio.auth.verification;

import com.tvboot.tivio.auth.User;
import com.tvboot.tivio.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for verifying user authentication setup
 * This is a development/debugging controller - remove in production
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserVerificationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Verify all users can authenticate with default password
     */
    @GetMapping("/verify-all")
    public ResponseEntity<Map<String, Object>> verifyAllUsers() {
        log.info("Verifying all users authentication...");

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> userVerification = new HashMap<>();
        List<String> issues = new ArrayList<>();

        // Test users with default password
        String defaultPassword = "admin123";
        List<String> testUsers = Arrays.asList("admin", "manager", "receptionist", "technician");

        for (String username : testUsers) {
            Map<String, Object> userStatus = new HashMap<>();

            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Check if user exists
                userStatus.put("exists", true);
                userStatus.put("id", user.getId());
                userStatus.put("email", user.getEmail());
                userStatus.put("role", user.getRole().toString());
                userStatus.put("active", user.isActive());
                userStatus.put("lastLogin", user.getLastLogin());

                // Test password
                boolean passwordMatches = passwordEncoder.matches(defaultPassword, user.getPassword());
                userStatus.put("passwordValid", passwordMatches);

                // Check if password is properly encoded (should start with $2a$ for BCrypt)
                boolean properlyEncoded = user.getPassword().startsWith("$2a$") ||
                        user.getPassword().startsWith("$2b$") ||
                        user.getPassword().startsWith("$2y$");
                userStatus.put("passwordEncoded", properlyEncoded);

                if (!passwordMatches) {
                    issues.add("User " + username + " cannot authenticate with default password");
                }

                if (!properlyEncoded) {
                    issues.add("User " + username + " password is not properly encoded");
                }

                if (!user.isActive()) {
                    issues.add("User " + username + " is not active");
                }

            } else {
                userStatus.put("exists", false);
                issues.add("User " + username + " does not exist");
            }

            userVerification.put(username, userStatus);
        }

        // Overall status
        boolean allGood = issues.isEmpty();

        response.put("success", allGood);
        response.put("message", allGood ? "All users verified successfully" : "Issues found with user authentication");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("totalUsers", userRepository.count());
        response.put("testedUsers", testUsers.size());
        response.put("users", userVerification);

        if (!issues.isEmpty()) {
            response.put("issues", issues);
        }

        log.info("User verification completed. Success: {}, Issues: {}", allGood, issues.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Fix password for a specific user
     */
    @PostMapping("/{username}/fix-password")
    public ResponseEntity<Map<String, Object>> fixUserPassword(@PathVariable String username) {
        log.info("Fixing password for user: {}", username);

        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Encode the default password
            String encodedPassword = passwordEncoder.encode("admin123");
            user.setPassword(encodedPassword);
            user.setActive(true); // Ensure user is active

            User savedUser = userRepository.save(user);

            // Verify the fix worked
            boolean passwordMatches = passwordEncoder.matches("admin123", savedUser.getPassword());

            response.put("success", passwordMatches);
            response.put("message", passwordMatches ?
                    "Password fixed successfully" : "Password fix failed");
            response.put("username", username);
            response.put("passwordValid", passwordMatches);
            response.put("timestamp", LocalDateTime.now().toString());

            log.info("Password fix for user {}: {}", username, passwordMatches ? "SUCCESS" : "FAILED");

        } else {
            response.put("success", false);
            response.put("message", "User not found");
            response.put("username", username);
            response.put("timestamp", LocalDateTime.now().toString());

            log.warn("User not found for password fix: {}", username);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Fix all users passwords
     */
    @PostMapping("/fix-all-passwords")
    public ResponseEntity<Map<String, Object>> fixAllPasswords() {
        log.info("Fixing passwords for all users...");

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        List<String> fixed = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        List<String> usernames = Arrays.asList("admin", "manager", "receptionist", "technician");

        for (String username : usernames) {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                try {
                    // Encode the default password
                    String encodedPassword = passwordEncoder.encode("admin123");
                    user.setPassword(encodedPassword);
                    user.setActive(true); // Ensure user is active

                    User savedUser = userRepository.save(user);

                    // Verify the fix worked
                    boolean passwordMatches = passwordEncoder.matches("admin123", savedUser.getPassword());

                    if (passwordMatches) {
                        fixed.add(username);
                        results.put(username, "Fixed successfully");
                    } else {
                        failed.add(username);
                        results.put(username, "Fix failed - password verification failed");
                    }

                } catch (Exception e) {
                    failed.add(username);
                    results.put(username, "Fix failed - " + e.getMessage());
                    log.error("Error fixing password for user {}: {}", username, e.getMessage());
                }
            } else {
                failed.add(username);
                results.put(username, "User not found");
            }
        }

        boolean allFixed = failed.isEmpty();

        response.put("success", allFixed);
        response.put("message", String.format("Fixed: %d, Failed: %d", fixed.size(), failed.size()));
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("fixed", fixed);
        response.put("failed", failed);
        response.put("results", results);

        log.info("Password fix completed. Fixed: {}, Failed: {}", fixed.size(), failed.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get detailed user information
     */
    @GetMapping("/{username}/details")
    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable String username) {
        log.info("Getting details for user: {}", username);

        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            response.put("success", true);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole().toString(),
                    "active", user.isActive(),
                    "lastLogin", user.getLastLogin(),
                    "createdAt", user.getCreatedAt(),
                    "updatedAt", user.getUpdatedAt()
            ));

            // Password analysis (without revealing the actual password)
            String hashedPassword = user.getPassword();
            response.put("passwordInfo", Map.of(
                    "length", hashedPassword.length(),
                    "startsWithBCrypt", hashedPassword.startsWith("$2a$") ||
                            hashedPassword.startsWith("$2b$") ||
                            hashedPassword.startsWith("$2y$"),
                    "canAuthenticateWithDefault", passwordEncoder.matches("admin123", hashedPassword)
            ));

        } else {
            response.put("success", false);
            response.put("message", "User not found");
        }

        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Test authentication for a specific user
     */
    @PostMapping("/{username}/test-auth")
    public ResponseEntity<Map<String, Object>> testUserAuth(
            @PathVariable String username,
            @RequestParam(defaultValue = "admin123") String password) {

        log.info("Testing authentication for user: {}", username);

        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            boolean isActive = user.isActive();
            boolean canAuthenticate = passwordMatches && isActive;

            response.put("success", canAuthenticate);
            response.put("username", username);
            response.put("passwordMatches", passwordMatches);
            response.put("isActive", isActive);
            response.put("canAuthenticate", canAuthenticate);
            response.put("role", user.getRole().toString());

            if (!canAuthenticate) {
                List<String> reasons = new ArrayList<>();
                if (!passwordMatches) reasons.add("Password does not match");
                if (!isActive) reasons.add("User is not active");
                response.put("failureReasons", reasons);
            }

        } else {
            response.put("success", false);
            response.put("message", "User not found");
            response.put("username", username);
        }

        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Create a missing user
     */
    @PostMapping("/{username}/create")
    public ResponseEntity<Map<String, Object>> createUser(@PathVariable String username) {
        log.info("Creating user: {}", username);

        Map<String, Object> response = new HashMap<>();

        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            response.put("success", false);
            response.put("message", "User already exists");
            response.put("username", username);
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        }

        try {
            User user = null;

            switch (username.toLowerCase()) {
                case "admin":
                    user = createUserEntity("admin", "admin@tvboot.com", "System", "Administrator", User.Role.ADMIN);
                    break;
                case "manager":
                    user = createUserEntity("manager", "manager@tvboot.com", "Hotel", "Manager", User.Role.MANAGER);
                    break;
                case "receptionist":
                    user = createUserEntity("receptionist", "receptionist@tvboot.com", "Front", "Desk", User.Role.RECEPTIONIST);
                    break;
                case "technician":
                    user = createUserEntity("technician", "technician@tvboot.com", "IT", "Technician", User.Role.TECHNICIAN);
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "Unknown user type. Supported: admin, manager, receptionist, technician");
                    response.put("username", username);
                    response.put("timestamp", LocalDateTime.now().toString());
                    return ResponseEntity.ok(response);
            }

            User savedUser = userRepository.save(user);

            // Verify the user was created correctly
            boolean passwordMatches = passwordEncoder.matches("admin123", savedUser.getPassword());

            response.put("success", passwordMatches);
            response.put("message", passwordMatches ? "User created successfully" : "User created but password verification failed");
            response.put("username", username);
            response.put("userId", savedUser.getId());
            response.put("role", savedUser.getRole().toString());
            response.put("passwordValid", passwordMatches);

            log.info("User {} created successfully: {}", username, passwordMatches);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create user: " + e.getMessage());
            response.put("username", username);
            log.error("Error creating user {}: {}", username, e.getMessage());
        }

        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create user entity
     */
    private User createUserEntity(String username, String email, String firstName, String lastName, User.Role role) {
        String encodedPassword = passwordEncoder.encode("admin123");

        return User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isActive(true)
                .build();
    }

    /**
     * Get all users summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getUsersSummary() {
        log.info("Getting users summary...");

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> usersList = new ArrayList<>();

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole().toString());
            userInfo.put("active", user.isActive());
            userInfo.put("lastLogin", user.getLastLogin());
            userInfo.put("canAuthenticate", passwordEncoder.matches("admin123", user.getPassword()) && user.isActive());

            usersList.add(userInfo);
        }

        response.put("success", true);
        response.put("totalUsers", allUsers.size());
        response.put("users", usersList);
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}