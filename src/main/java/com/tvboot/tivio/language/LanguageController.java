package com.tvboot.tivio.language;

import com.tvboot.tivio.language.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for Language management in TVBOOT IPTV system
 * Provides endpoints for admin platform and TV applications
 */
@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Language Management", description = "APIs for managing languages in TVBOOT IPTV system")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"}, maxAge = 3600)
public class LanguageController {

    private final LanguageService languageService;

    // ==========================================
    // PUBLIC ENDPOINTS (TV Apps & Guest Access)
    // ==========================================

    /**
     * Get languages available for guest TV applications
     * Used by Samsung Tizen and LG WebOS apps
     */
    @GetMapping("/guest")
    @Operation(summary = "Get guest languages",
            description = "Retrieve languages available for hotel guests on TV applications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Languages retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid platform parameter")
    })
    public ResponseEntity<ResponseWrapper<List<LanguageDTO>>> getGuestLanguages(
            @Parameter(description = "TV platform (TIZEN, WEBOS, ANDROID)")
            @RequestParam(required = false) String platform) {

        log.info("Getting guest languages for platform: {}", platform);
        List<LanguageDTO> languages = languageService.getGuestLanguages(platform);

        return ResponseEntity.ok(ResponseWrapper.success(languages,
                String.format("Found %d guest languages", languages.size())));
    }

    /**
     * Get minimal language data for TV apps (optimized)
     * Reduced payload for better TV performance
     */
    @GetMapping("/guest/minimal")
    @Operation(summary = "Get minimal language data for TV",
            description = "Optimized endpoint for TV applications with minimal data")
    public ResponseEntity<List<LanguageMinimalDTO>> getMinimalGuestLanguages(
            @RequestParam(required = false) String platform) {

        log.info("Getting minimal guest languages for platform: {}", platform);
        List<LanguageMinimalDTO> languages = languageService.getLanguagesForPlatform(platform);

        return ResponseEntity.ok(languages);
    }

    /**
     * Get default language
     * Used by TV apps for initial language selection
     */
    @GetMapping("/default")
    @Operation(summary = "Get default language",
            description = "Retrieve the system's default language")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> getDefaultLanguage() {
        log.info("Getting default language");
        LanguageDTO language = languageService.getDefaultLanguage();

        return ResponseEntity.ok(ResponseWrapper.success(language));
    }

    /**
     * Get RTL languages
     * Used by TV apps for special RTL handling
     */
    @GetMapping("/rtl")
    @Operation(summary = "Get RTL languages",
            description = "Retrieve all right-to-left languages for special handling")
    public ResponseEntity<ResponseWrapper<List<LanguageDTO>>> getRtlLanguages() {
        log.info("Getting RTL languages");
        List<LanguageDTO> languages = languageService.getRtlLanguages();

        return ResponseEntity.ok(ResponseWrapper.success(languages));
    }

    // ==========================================
    // ADMIN ENDPOINTS (Angular Admin Platform)
    // ==========================================

    /**
     * Get all languages with pagination and filtering
     * Used by admin dashboard
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all languages",
            description = "Retrieve all languages with pagination and filtering")
    public ResponseEntity<Page<LanguageDTO>> getAllLanguages(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "displayOrder") String sortBy,

            @Parameter(description = "Sort direction (ASC/DESC)")
            @RequestParam(defaultValue = "ASC") String sortDirection,

            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive,

            @Parameter(description = "Filter by guest enabled status")
            @RequestParam(required = false) Boolean isGuestEnabled,

            @Parameter(description = "Filter by admin enabled status")
            @RequestParam(required = false) Boolean isAdminEnabled,

            @Parameter(description = "Filter by RTL status")
            @RequestParam(required = false) Boolean isRtl,

            @Parameter(description = "Search term (searches in name, native name, ISO codes)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Minimum translation progress")
            @RequestParam(required = false) Integer minTranslation) {

        log.info("Getting all languages - page: {}, size: {}, sort: {} {}",
                page, size, sortBy, sortDirection);

        // Build filter
        LanguageFilterRequest filter = LanguageFilterRequest.builder()
                .isActive(isActive)
                .isGuestEnabled(isGuestEnabled)
                .isAdminEnabled(isAdminEnabled)
                .isRtl(isRtl)
                .searchTerm(search)
                .minTranslationProgress(minTranslation)
                .build();

        // Build pageable
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LanguageDTO> languages = languageService.getAllLanguages(pageable, filter);

        return ResponseEntity.ok(languages);
    }

    /**
     * Get languages for admin platform
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get admin languages",
            description = "Retrieve languages available in admin platform")
    public ResponseEntity<ResponseWrapper<List<LanguageDTO>>> getAdminLanguages() {
        log.info("Getting admin languages");
        List<LanguageDTO> languages = languageService.getAdminLanguages();

        return ResponseEntity.ok(ResponseWrapper.success(languages));
    }

    /**
     * Get language by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Get language by ID",
            description = "Retrieve a specific language by its ID")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> getLanguageById(
            @Parameter(description = "Language ID", required = true)
            @PathVariable Long id) {

        log.info("Getting language by ID: {}", id);
        LanguageDTO language = languageService.getLanguageById(id);

        return ResponseEntity.ok(ResponseWrapper.success(language));
    }

    /**
     * Get language by ISO code
     */
    @GetMapping("/code/{isoCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Get language by ISO code",
            description = "Retrieve a language by its ISO 639-1 code")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> getLanguageByIsoCode(
            @Parameter(description = "ISO 639-1 code", required = true)
            @PathVariable String isoCode) {

        log.info("Getting language by ISO code: {}", isoCode);
        LanguageDTO language = languageService.getLanguageByIsoCode(isoCode);

        return ResponseEntity.ok(ResponseWrapper.success(language));
    }

    /**
     * Create a new language
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create language",
            description = "Create a new language in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Language created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Language already exists")
    })
    public ResponseEntity<ResponseWrapper<LanguageDTO>> createLanguage(
            @Valid @RequestBody LanguageCreateRequest request) {

        log.info("Creating new language: {}", request.getName());
        LanguageDTO language = languageService.createLanguage(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(language, "Language created successfully"));
    }

    /**
     * Update language details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update language",
            description = "Update an existing language")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> updateLanguage(
            @PathVariable Long id,
            @Valid @RequestBody LanguageUpdateRequest request) {

        log.info("Updating language ID: {}", id);
        LanguageDTO language = languageService.updateLanguage(id, request);

        return ResponseEntity.ok(ResponseWrapper.success(language, "Language updated successfully"));
    }

    /**
     * Set default language
     */
    @PutMapping("/{id}/default")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set default language",
            description = "Set a language as the system default")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> setDefaultLanguage(
            @PathVariable Long id) {

        log.info("Setting language {} as default", id);
        LanguageDTO language = languageService.setDefaultLanguage(id);

        return ResponseEntity.ok(ResponseWrapper.success(language,
                "Language set as default successfully"));
    }

    /**
     * Update language status (active/inactive)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update language status",
            description = "Activate or deactivate a language")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> updateLanguageStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        log.info("Updating language {} status to: {}", id, active);
        LanguageDTO language = languageService.updateLanguageStatus(id, active);

        String message = active ? "Language activated successfully" : "Language deactivated successfully";
        return ResponseEntity.ok(ResponseWrapper.success(language, message));
    }

    /**
     * Update guest availability
     */
    @PatchMapping("/{id}/guest-availability")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update guest availability",
            description = "Enable or disable language for guest TV applications")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> updateGuestAvailability(
            @PathVariable Long id,
            @Valid @RequestBody GuestAvailabilityRequest request) {

        log.info("Updating guest availability for language {}: {}", id, request.getIsGuestEnabled());
        LanguageDTO language = languageService.updateGuestAvailability(id, request.getIsGuestEnabled());

        String message = request.getIsGuestEnabled() ?
                "Language enabled for guests" : "Language disabled for guests";
        return ResponseEntity.ok(ResponseWrapper.success(language, message));
    }

    /**
     * Update translation progress
     */
    @PatchMapping("/{id}/translation-progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSLATOR')")
    @Operation(summary = "Update translation progress",
            description = "Update UI and channel translation progress")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> updateTranslationProgress(
            @PathVariable Long id,
            @Valid @RequestBody TranslationProgressRequest request) {

        log.info("Updating translation progress for language {}", id);
        LanguageDTO language = languageService.updateTranslationProgress(
                id, request.getUiTranslationProgress(), request.getChannelTranslationProgress());

        return ResponseEntity.ok(ResponseWrapper.success(language,
                "Translation progress updated successfully"));
    }

    /**
     * Update display order for multiple languages
     */
    @PatchMapping("/display-order")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update display order",
            description = "Batch update display order for multiple languages")
    public ResponseEntity<ResponseWrapper<String>> updateDisplayOrder(
            @Valid @RequestBody BatchDisplayOrderRequest request) {

        log.info("Updating display order for {} languages", request.getUpdates().size());

        Map<Long, Integer> orderMap = new HashMap<>();
        request.getUpdates().forEach(update ->
                orderMap.put(update.getLanguageId(), update.getDisplayOrder()));

        languageService.updateDisplayOrder(orderMap);

        return ResponseEntity.ok(ResponseWrapper.success(null,
                String.format("Display order updated for %d languages", request.getUpdates().size())));
    }

    /**
     * Add platform support
     */
    @PostMapping("/{id}/platforms")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add platform support",
            description = "Add TV platform support to a language")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> addPlatformSupport(
            @PathVariable Long id,
            @RequestBody Set<String> platforms) {

        log.info("Adding platform support for language {}: {}", id, platforms);
        LanguageDTO language = languageService.addPlatformSupport(id, platforms);

        return ResponseEntity.ok(ResponseWrapper.success(language,
                "Platform support added successfully"));
    }

    /**
     * Remove platform support
     */
    @DeleteMapping("/{id}/platforms")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove platform support",
            description = "Remove TV platform support from a language")
    public ResponseEntity<ResponseWrapper<LanguageDTO>> removePlatformSupport(
            @PathVariable Long id,
            @RequestBody Set<String> platforms) {

        log.info("Removing platform support for language {}: {}", id, platforms);
        LanguageDTO language = languageService.removePlatformSupport(id, platforms);

        return ResponseEntity.ok(ResponseWrapper.success(language,
                "Platform support removed successfully"));
    }

    /**
     * Delete a language
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete language",
            description = "Delete a language from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Language deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete default language"),
            @ApiResponse(responseCode = "404", description = "Language not found")
    })
    public ResponseEntity<Void> deleteLanguage(@PathVariable Long id) {
        log.info("Deleting language ID: {}", id);
        languageService.deleteLanguage(id);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // STATISTICS & REPORTING ENDPOINTS
    // ==========================================

    /**
     * Get language statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get language statistics",
            description = "Retrieve language statistics for dashboard")
    public ResponseEntity<ResponseWrapper<LanguageStatisticsDTO>> getStatistics() {
        log.info("Getting language statistics");
        LanguageStatisticsDTO statistics = languageService.getStatistics();

        return ResponseEntity.ok(ResponseWrapper.success(statistics));
    }

    /**
     * Get languages needing translation
     */
    @GetMapping("/needs-translation")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSLATOR')")
    @Operation(summary = "Get languages needing translation",
            description = "Retrieve languages that need translation work")
    public ResponseEntity<ResponseWrapper<List<LanguageDTO>>> getLanguagesNeedingTranslation() {
        log.info("Getting languages needing translation");
        List<LanguageDTO> languages = languageService.getLanguagesNeedingTranslation();

        return ResponseEntity.ok(ResponseWrapper.success(languages,
                String.format("%d languages need translation", languages.size())));
    }

    /**
     * Get languages ready for guest enablement
     */
    @GetMapping("/ready-for-guests")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get languages ready for guests",
            description = "Retrieve languages that meet criteria for guest enablement")
    public ResponseEntity<ResponseWrapper<List<LanguageDTO>>> getLanguagesReadyForGuests() {
        log.info("Getting languages ready for guest enablement");
        List<LanguageDTO> languages = languageService.getLanguagesReadyForGuests();

        return ResponseEntity.ok(ResponseWrapper.success(languages,
                String.format("%d languages ready for guest enablement", languages.size())));
    }

    /**
     * Validate language data integrity
     */
    @GetMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validate language data",
            description = "Run validation checks on language data")
    public ResponseEntity<ResponseWrapper<ValidationReportDTO>> validateLanguageData() {
        log.info("Running language data validation");
        ValidationReportDTO report = languageService.validateLanguageData();

        if (report.isValid()) {
            return ResponseEntity.ok(ResponseWrapper.success(report, "Validation passed"));
        } else {
            return ResponseEntity.ok(ResponseWrapper.success(report,
                    String.format("Validation found %d errors and %d warnings",
                            report.getErrors().size(), report.getWarnings().size())));
        }
    }

    // ==========================================
    // INITIALIZATION & MAINTENANCE ENDPOINTS
    // ==========================================

    /**
     * Initialize default languages
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initialize default languages",
            description = "Initialize system with default languages (only works if no languages exist)")
    public ResponseEntity<ResponseWrapper<String>> initializeDefaultLanguages() {
        log.info("Initializing default languages");
        languageService.initializeDefaultLanguages();

        return ResponseEntity.ok(ResponseWrapper.success(null,
                "Default languages initialized successfully"));
    }

    /**
     * Clear language caches
     */
    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear language caches",
            description = "Clear all language-related caches")
    public ResponseEntity<ResponseWrapper<String>> clearCaches() {
        log.info("Clearing language caches");
        languageService.clearAllCaches();

        return ResponseEntity.ok(ResponseWrapper.success(null, "Caches cleared successfully"));
    }

    /**
     * Export languages to JSON
     */
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export languages",
            description = "Export all languages to JSON format")
    public ResponseEntity<String> exportLanguages() {
        log.info("Exporting languages to JSON");
        String json = languageService.exportLanguagesToJson();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=languages-export.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    /**
     * Import languages from JSON
     */
    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Import languages",
            description = "Import languages from JSON format")
    public ResponseEntity<ResponseWrapper<String>> importLanguages(
            @RequestBody String jsonData) {

        log.info("Importing languages from JSON");
        int count = languageService.importLanguagesFromJson(jsonData);

        return ResponseEntity.ok(ResponseWrapper.success(null,
                String.format("%d languages imported successfully", count)));
    }

    // ==========================================
    // WEBSOCKET ENDPOINTS (for real-time updates)
    // ==========================================

    /**
     * Get WebSocket connection info for language updates
     * TV apps can connect to receive real-time language changes
     */
    @GetMapping("/websocket/info")
    @Operation(summary = "Get WebSocket info",
            description = "Get WebSocket connection details for real-time language updates")
    public ResponseEntity<Map<String, String>> getWebSocketInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("url", "ws://localhost:8080/ws/languages");
        info.put("topic", "/topic/language-updates");
        info.put("protocol", "STOMP");

        return ResponseEntity.ok(info);
    }

    // Exception handlers removed - using GlobalExceptionHandler instead
}