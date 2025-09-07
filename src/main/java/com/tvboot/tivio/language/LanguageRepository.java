package com.tvboot.tivio.language;

import com.tvboot.tivio.language.dto.LanguageMinimalProjection;
import com.tvboot.tivio.language.dto.LanguageStatisticsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Language entity
 * Provides data access methods for language management in TVBOOT IPTV system
 */
@Repository
public interface LanguageRepository extends JpaRepository<Language, Long>, JpaSpecificationExecutor<Language> {

    // ==========================================
    // BASIC QUERIES
    // ==========================================

    /**
     * Find language by ISO 639-1 code
     * @param iso6391 Two-letter language code (e.g., "en", "fr", "ar")
     * @return Optional containing the language if found
     */
    Optional<Language> findByIso6391(String iso6391);

    /**
     * Find language by ISO 639-2 code
     * @param iso6392 Three-letter language code (e.g., "eng", "fra", "ara")
     * @return Optional containing the language if found
     */
    Optional<Language> findByIso6392(String iso6392);

    /**
     * Find language by locale code
     * @param localeCode Locale code (e.g., "en-US", "fr-FR", "ar-DZ")
     * @return Optional containing the language if found
     */
    Optional<Language> findByLocaleCode(String localeCode);

    /**
     * Find the default language
     * @param isDefault Should be true
     * @return Optional containing the default language
     */
    Optional<Language> findByIsDefault(Boolean isDefault);

    /**
     * Check if a language with given ISO code exists
     * @param iso6391 Two-letter language code
     * @return true if exists
     */
    boolean existsByIso6391(String iso6391);

    /**
     * Check if a default language exists
     * @return true if a default language is set
     */
    boolean existsByIsDefaultTrue();

    // ==========================================
    // GUEST TV APP QUERIES
    // ==========================================

    /**
     * Find all active languages enabled for guest TV applications
     * Used by Samsung Tizen and LG WebOS apps
     * @param isActive Active status
     * @param isGuestEnabled Guest availability status
     * @return List of languages available for guests
     */
    List<Language> findByIsActiveAndIsGuestEnabled(Boolean isActive, Boolean isGuestEnabled);

    /**
     * Find active guest languages ordered by display order
     * @return List of languages sorted by display order
     */
    @Query("SELECT l FROM Language l WHERE l.isActive = true AND l.isGuestEnabled = true " +
            "ORDER BY l.isDefault DESC, l.displayOrder ASC")
    List<Language> findActiveGuestLanguagesOrdered();

    /**
     * Find guest languages with minimum translation progress
     * @param minProgress Minimum UI translation progress percentage
     * @return List of sufficiently translated languages
     */
    @Query("SELECT l FROM Language l WHERE l.isActive = true AND l.isGuestEnabled = true " +
            "AND l.uiTranslationProgress >= :minProgress ORDER BY l.displayOrder")
    List<Language> findGuestLanguagesWithMinTranslation(@Param("minProgress") Integer minProgress);

    /**
     * Find guest languages for specific platform
     * @param platform Platform name (TIZEN, WEBOS, ANDROID)
     * @return List of languages supporting the platform
     */
    @Query("SELECT l FROM Language l JOIN l.supportedPlatforms p " +
            "WHERE l.isActive = true AND l.isGuestEnabled = true " +
            "AND UPPER(p) = UPPER(:platform) ORDER BY l.displayOrder")
    List<Language> findGuestLanguagesByPlatform(@Param("platform") String platform);

    // ==========================================
    // ADMIN PLATFORM QUERIES
    // ==========================================

    /**
     * Find all active languages enabled for admin platform
     * Used by Angular admin application
     * @param isActive Active status
     * @param isAdminEnabled Admin availability status
     * @return List of languages available for admin
     */
    List<Language> findByIsActiveAndIsAdminEnabled(Boolean isActive, Boolean isAdminEnabled);

    /**
     * Find all active languages
     * @return List of active languages
     */
    List<Language> findByIsActiveTrue();

    /**
     * Find inactive languages
     * @return List of inactive languages
     */
    List<Language> findByIsActiveFalse();

    // ==========================================
    // RTL LANGUAGE QUERIES
    // ==========================================

    /**
     * Find all RTL (Right-to-Left) languages
     * @return List of RTL languages like Arabic, Hebrew
     */
    List<Language> findByIsRtlTrue();

    /**
     * Find active RTL languages for guests
     * @return List of active RTL languages enabled for guests
     */
    @Query("SELECT l FROM Language l WHERE l.isRtl = true " +
            "AND l.isActive = true AND l.isGuestEnabled = true")
    List<Language> findActiveRtlGuestLanguages();

    // ==========================================
    // TRANSLATION PROGRESS QUERIES
    // ==========================================

    /**
     * Find languages needing translation work
     * @param maxProgress Maximum translation progress to be considered incomplete
     * @return List of languages needing translation
     */
    @Query("SELECT l FROM Language l WHERE l.isActive = true " +
            "AND (l.uiTranslationProgress < :maxProgress OR l.channelTranslationProgress < :maxProgress) " +
            "ORDER BY l.uiTranslationProgress ASC")
    List<Language> findLanguagesNeedingTranslation(@Param("maxProgress") Integer maxProgress);

    /**
     * Find fully translated languages
     * @return List of 100% translated languages
     */
    @Query("SELECT l FROM Language l WHERE l.uiTranslationProgress = 100 " +
            "AND l.channelTranslationProgress = 100")
    List<Language> findFullyTranslatedLanguages();

    /**
     * Find languages ready to be enabled for guests (80%+ translated)
     * @return List of languages ready for guest enablement
     */
    @Query("SELECT l FROM Language l WHERE l.isActive = true " +
            "AND l.isGuestEnabled = false AND l.uiTranslationProgress >= 80 " +
            "ORDER BY l.uiTranslationProgress DESC")
    List<Language> findLanguagesReadyForGuests();

    // ==========================================
    // PAGINATED QUERIES
    // ==========================================

    /**
     * Find all active languages with pagination
     * @param pageable Pagination information
     * @return Page of active languages
     */
    Page<Language> findByIsActiveTrue(Pageable pageable);

    /**
     * Find guest languages with pagination
     * @param pageable Pagination information
     * @return Page of guest-enabled languages
     */
    Page<Language> findByIsGuestEnabledTrue(Pageable pageable);

    List<Language> findByIsGuestEnabledTrue();

    /**
     * Search languages by name (case-insensitive)
     * @param name Language name or partial name
     * @param pageable Pagination information
     * @return Page of matching languages
     */
    Page<Language> findByNameContainingIgnoreCaseOrNativeNameContainingIgnoreCase(
            String name, String nativeName, Pageable pageable);

    // ==========================================
    // STATISTICS QUERIES
    // ==========================================

    /**
     * Count active languages
     * @return Number of active languages
     */
    long countByIsActiveTrue();

    /**
     * Count guest-enabled languages
     * @return Number of languages available for guests
     */
    long countByIsGuestEnabledTrue();

    /**
     * Count languages by platform support
     * @param platform Platform name
     * @return Number of languages supporting the platform
     */
    @Query("SELECT COUNT(DISTINCT l) FROM Language l JOIN l.supportedPlatforms p " +
            "WHERE UPPER(p) = UPPER(:platform)")
    long countByPlatform(@Param("platform") String platform);

    /**
     * Get average translation progress
     * @return Average UI translation progress across all active languages
     */
    @Query("SELECT AVG(l.uiTranslationProgress) FROM Language l WHERE l.isActive = true")
    Double getAverageUiTranslationProgress();

    /**
     * Get language statistics grouped by status
     * @return Statistics for dashboard
     */
    @Query("SELECT " +
            "COUNT(l) as total, " +
            "SUM(CASE WHEN l.isActive = true THEN 1 ELSE 0 END) as active, " +
            "SUM(CASE WHEN l.isGuestEnabled = true THEN 1 ELSE 0 END) as guestEnabled, " +
            "SUM(CASE WHEN l.isAdminEnabled = true THEN 1 ELSE 0 END) as adminEnabled, " +
            "SUM(CASE WHEN l.uiTranslationProgress = 100 THEN 1 ELSE 0 END) as fullyTranslated " +
            "FROM Language l")
    LanguageStatisticsProjection getLanguageStatistics();

    // ==========================================
    // UPDATE QUERIES
    // ==========================================

    /**
     * Update display order for a language
     * @param id Language ID
     * @param displayOrder New display order
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE Language l SET l.displayOrder = :displayOrder WHERE l.id = :id")
    int updateDisplayOrder(@Param("id") Long id, @Param("displayOrder") Integer displayOrder);

    /**
     * Update translation progress
     * @param id Language ID
     * @param uiProgress UI translation progress
     * @param channelProgress Channel translation progress
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE Language l SET l.uiTranslationProgress = :uiProgress, " +
            "l.channelTranslationProgress = :channelProgress, l.updatedAt = :now " +
            "WHERE l.id = :id")
    int updateTranslationProgress(@Param("id") Long id,
                                  @Param("uiProgress") Integer uiProgress,
                                  @Param("channelProgress") Integer channelProgress,
                                  @Param("now") LocalDateTime now);

    /**
     * Enable/disable language for guests
     * @param id Language ID
     * @param enabled Guest availability status
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE Language l SET l.isGuestEnabled = :enabled WHERE l.id = :id")
    int updateGuestAvailability(@Param("id") Long id, @Param("enabled") Boolean enabled);

    /**
     * Unset all default languages (used before setting a new default)
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE Language l SET l.isDefault = false WHERE l.isDefault = true")
    int unsetAllDefaults();

    // ==========================================
    // COMPLEX QUERIES
    // ==========================================

    /**
     * Find languages with specific currency
     * @param currencyCode ISO 4217 currency code
     * @return List of languages using the currency
     */
    List<Language> findByCurrencyCode(String currencyCode);

    /**
     * Find languages modified after a certain date
     * Used for synchronization with TV apps
     * @param date Date threshold
     * @return List of recently modified languages
     */
    List<Language> findByUpdatedAtAfter(LocalDateTime date);

    /**
     * Find languages by multiple ISO codes
     * @param isoCodes Set of ISO 639-1 codes
     * @return List of matching languages
     */
    List<Language> findByIso6391In(Set<String> isoCodes);

    /**
     * Custom query for language availability check
     * @param iso6391 Language code
     * @param platform Platform name
     * @return true if language is available for the platform
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Language l " +
            "JOIN l.supportedPlatforms p WHERE l.iso6391 = :iso6391 " +
            "AND l.isActive = true AND l.isGuestEnabled = true " +
            "AND UPPER(p) = UPPER(:platform)")
    boolean isLanguageAvailableForPlatform(@Param("iso6391") String iso6391,
                                           @Param("platform") String platform);

    /**
     * Find duplicate languages by ISO code (for data validation)
     * @return List of ISO codes that have duplicates
     */
    @Query("SELECT l.iso6391 FROM Language l GROUP BY l.iso6391 HAVING COUNT(l) > 1")
    List<String> findDuplicateIsoCodes();

    /**
     * Native query for performance-critical operations
     * Get minimal language data for TV apps
     * @return List of language data for TV display
     */
    @Query(value = "SELECT id, iso_639_1, native_name, is_rtl, display_order, flag_url " +
            "FROM languages WHERE is_active = true AND is_guest_enabled = true " +
            "ORDER BY is_default DESC, display_order ASC",
            nativeQuery = true)
    List<LanguageMinimalProjection> findMinimalGuestLanguages();
}

