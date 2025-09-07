package com.tvboot.tivio.language;


import com.tvboot.tivio.language.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** DEEPSEEK
 * REST Controller for managing languages in TVBOOT IPTV system
 * Provides endpoints for multi-platform language management
 */
@RestController
@RequestMapping("/api/v2/languages")
//@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class LanguageControllerDs {

    private final LanguageService languageService;

    // ==========================================
    // CREATE ENDPOINTS
    // ==========================================

    /**
     * Create a new language
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LanguageDTO> createLanguage(@Valid @RequestBody LanguageCreateRequest request) {
        log.info("REST request to create language: {}", request.getName());
        LanguageDTO createdLanguage = languageService.createLanguage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLanguage);
    }

    // ==========================================
    // READ ENDPOINTS
    // ==========================================

    /**
     * Get language by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LanguageDTO> getLanguageById(@PathVariable Long id) {
        log.debug("REST request to get language by ID: {}", id);
        LanguageDTO language = languageService.getLanguageById(id);
        return ResponseEntity.ok(language);
    }

    /**
     * Get language by ISO code
     */
    @GetMapping("/iso/{isoCode}")
    public ResponseEntity<LanguageDTO> getLanguageByIsoCode(@PathVariable String isoCode) {
        log.debug("REST request to get language by ISO code: {}", isoCode);
        LanguageDTO language = languageService.getLanguageByIsoCode(isoCode);
        return ResponseEntity.ok(language);
    }

    /**
     * Get all languages with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<LanguageDTO>> getAllLanguages(
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable,
            @ModelAttribute LanguageFilterRequest filter) {
        log.debug("REST request to get all languages with filter: {}", filter);
        Page<LanguageDTO> languages = languageService.getAllLanguages(pageable, filter);
        return ResponseEntity.ok(languages);
    }

    /**
     * Get all active languages for admin platform
     */
    @GetMapping("/admin")
    public ResponseEntity<List<LanguageDTO>> getAdminLanguages() {
        log.debug("REST request to get admin languages");
        List<LanguageDTO> languages = languageService.getAdminLanguages();
        return ResponseEntity.ok(languages);
    }

    /**
     * Get all active languages for guest TV applications
     */
    @GetMapping("/guest")
    public ResponseEntity<List<LanguageDTO>> getGuestLanguages(
            @RequestParam(required = false) String platform) {
        log.debug("REST request to get guest languages for platform: {}", platform);
        List<LanguageDTO> languages = languageService.getGuestLanguages(platform);
        return ResponseEntity.ok(languages);
    }

    /**
     * Get default language
     */
    @GetMapping("/default")
    public ResponseEntity<LanguageDTO> getDefaultLanguage() {
        log.debug("REST request to get default language");
        LanguageDTO defaultLanguage = languageService.getDefaultLanguage();
        return ResponseEntity.ok(defaultLanguage);
    }

    /**
     * Get languages for specific TV platform
     */
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<LanguageMinimalDTO>> getLanguagesForPlatform(
            @PathVariable String platform) {
        log.debug("REST request to get languages for platform: {}", platform);
        List<LanguageMinimalDTO> languages = languageService.getLanguagesForPlatform(platform);
        return ResponseEntity.ok(languages);
    }

    // ==========================================
    // UPDATE ENDPOINTS
    // ==========================================

    /**
     * Update language details
     */
    @PutMapping("/{id}")
    public ResponseEntity<LanguageDTO> updateLanguage(
            @PathVariable Long id,
            @Valid @RequestBody LanguageUpdateRequest request) {
        log.info("REST request to update language ID: {}", id);
        LanguageDTO updatedLanguage = languageService.updateLanguage(id, request);
        return ResponseEntity.ok(updatedLanguage);
    }

    /**
     * Set language as default
     */
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<LanguageDTO> setDefaultLanguage(@PathVariable Long id) {
        log.info("REST request to set language as default: {}", id);
        LanguageDTO updatedLanguage = languageService.setDefaultLanguage(id);
        return ResponseEntity.ok(updatedLanguage);
    }

    /**
     * Update language status (active/inactive)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<LanguageDTO> updateLanguageStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        log.info("REST request to update language status: {} to {}", id, active);
        LanguageDTO updatedLanguage = languageService.updateLanguageStatus(id, active);
        return ResponseEntity.ok(updatedLanguage);
    }

    /**
     * Enable/disable language for guest TV apps
     */
    @PatchMapping("/{id}/guest-availability")
    public ResponseEntity<LanguageDTO> updateGuestAvailability(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        log.info("REST request to update guest availability: {} to {}", id, enabled);
        LanguageDTO updatedLanguage = languageService.updateGuestAvailability(id, enabled);
        return ResponseEntity.ok(updatedLanguage);
    }

    /**
     * Update translation progress
     */
    @PatchMapping("/{id}/translation-progress")
    public ResponseEntity<LanguageDTO> updateTranslationProgress(
            @PathVariable Long id,
            @RequestParam(required = false) Integer uiProgress,
            @RequestParam(required = false) Integer channelProgress) {
        log.info("REST request to update translation progress for {}: UI={}, Channels={}",
                id, uiProgress, channelProgress);
        LanguageDTO updatedLanguage = languageService.updateTranslationProgress(id, uiProgress, channelProgress);
        return ResponseEntity.ok(updatedLanguage);
    }

    /**
     * Update display order for multiple languages
     */
    @PatchMapping("/display-order")
    public ResponseEntity<Void> updateDisplayOrder(
            @RequestBody Map<Long, Integer> orderUpdates) {
        log.info("REST request to update display order for {} languages", orderUpdates.size());
        languageService.updateDisplayOrder(orderUpdates);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add platform support to language
     */
    @PostMapping("/{id}/platforms")
    public ResponseEntity<LanguageDTO> addPlatformSupport(
            @PathVariable Long id,
            @RequestBody Set<String> platforms) {
        log.info("REST request to add platform support for language {}: {}", id, platforms);
        LanguageDTO updatedLanguage = languageService.addPlatformSupport(id, platforms);
        return ResponseEntity.ok(updatedLanguage);
    }

    /**
     * Remove platform support from language
     */
    @DeleteMapping("/{id}/platforms")
    public ResponseEntity<LanguageDTO> removePlatformSupport(
            @PathVariable Long id,
            @RequestBody Set<String> platforms) {
        log.info("REST request to remove platform support for language {}: {}", id, platforms);
        LanguageDTO updatedLanguage = languageService.removePlatformSupport(id, platforms);
        return ResponseEntity.ok(updatedLanguage);
    }

    // ==========================================
    // DELETE ENDPOINTS
    // ==========================================

    /**
     * Delete a language
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteLanguage(@PathVariable Long id) {
        log.info("REST request to delete language: {}", id);
        languageService.deleteLanguage(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // STATISTICS & REPORTING ENDPOINTS
    // ==========================================

    /**
     * Get language statistics for dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<LanguageStatisticsDTO> getStatistics() {
        log.debug("REST request to get language statistics");
        LanguageStatisticsDTO statistics = languageService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get languages needing translation work
     */
    @GetMapping("/needing-translation")
    public ResponseEntity<List<LanguageDTO>> getLanguagesNeedingTranslation() {
        log.debug("REST request to get languages needing translation");
        List<LanguageDTO> languages = languageService.getLanguagesNeedingTranslation();
        return ResponseEntity.ok(languages);
    }

    /**
     * Get languages ready to be enabled for guests
     */
    @GetMapping("/ready-for-guests")
    public ResponseEntity<List<LanguageDTO>> getLanguagesReadyForGuests() {
        log.debug("REST request to get languages ready for guests");
        List<LanguageDTO> languages = languageService.getLanguagesReadyForGuests();
        return ResponseEntity.ok(languages);
    }

    /**
     * Get RTL languages for special handling
     */
    @GetMapping("/rtl")
    public ResponseEntity<List<LanguageDTO>> getRtlLanguages() {
        log.debug("REST request to get RTL languages");
        List<LanguageDTO> languages = languageService.getRtlLanguages();
        return ResponseEntity.ok(languages);
    }

    // ==========================================
    // MAINTENANCE & ADMIN ENDPOINTS
    // ==========================================

    /**
     * Initialize default languages for new installation
     */
    @PostMapping("/initialize")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> initializeDefaultLanguages() {
        log.info("REST request to initialize default languages");
        languageService.initializeDefaultLanguages();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Validate language data integrity
     */
    @GetMapping("/validate")
    public ResponseEntity<ValidationReportDTO> validateLanguageData() {
        log.info("REST request to validate language data");
        ValidationReportDTO report = languageService.validateLanguageData();
        return ResponseEntity.ok(report);
    }

    /**
     * Clear all language caches
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<Void> clearAllCaches() {
        log.info("REST request to clear language caches");
        languageService.clearAllCaches();
        return ResponseEntity.ok().build();
    }

    /**
     * Export languages to JSON for backup
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportLanguagesToJson() {
        log.info("REST request to export languages to JSON");
        String jsonData = languageService.exportLanguagesToJson();
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("Content-Disposition", "attachment; filename=languages-backup.json")
                .body(jsonData);
    }

    /**
     * Import languages from JSON backup
     */
    @PostMapping("/import")
    public ResponseEntity<ImportResultDTO> importLanguagesFromJson(@RequestBody String jsonData) {
        log.info("REST request to import languages from JSON");
        int importedCount = languageService.importLanguagesFromJson(jsonData);

        ImportResultDTO result = ImportResultDTO.builder()
                .importedCount(importedCount)
                .message("Successfully imported " + importedCount + " languages")
                .build();

        return ResponseEntity.ok(result);
    }

    // ==========================================
    // DTO CLASSES FOR RESPONSES
    // ==========================================

    /**
     * DTO for import result
     */
    @lombok.Data
    @lombok.Builder
    public static class ImportResultDTO {
        private int importedCount;
        private String message;
    }
}