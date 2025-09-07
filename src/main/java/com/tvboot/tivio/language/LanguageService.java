package com.tvboot.tivio.language;

import com.tvboot.tivio.exception.BusinessException;
import com.tvboot.tivio.exception.ResourceNotFoundException;
import com.tvboot.tivio.exception.ValidationException;
import com.tvboot.tivio.language.dto.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing languages in TVBOOT IPTV system
 * Handles business logic for multi-platform language support
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LanguageService {

    private final LanguageRepository languageRepository;

    // Constants
    private static final int MIN_GUEST_TRANSLATION_THRESHOLD = 80;
    private static final int MIN_CHANNEL_TRANSLATION_THRESHOLD = 70;
    private static final String CACHE_NAME_GUEST = "guestLanguages";
    private static final String CACHE_NAME_ADMIN = "adminLanguages";
    private static final String CACHE_NAME_DEFAULT = "defaultLanguage";
    private static final String CACHE_NAME_PLATFORM = "platformLanguages";

    // ==========================================
    // CREATE OPERATIONS
    // ==========================================

    /**
     * Create a new language
     * @param request Language creation request
     * @return Created language
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_ADMIN}, allEntries = true)
    public LanguageDTO createLanguage(LanguageCreateRequest request) {
        log.info("Creating new language: {}", request.getName());

        // Validate unique ISO codes
        if (languageRepository.existsByIso6391(request.getIso6391())) {
            throw new ValidationException("Language with ISO 639-1 code '" + request.getIso6391() + "' already exists");
        }

        // Build language entity
        Language language = Language.builder()
                .name(request.getName())
                .nativeName(request.getNativeName())
                .iso6391(request.getIso6391().toLowerCase())
                .iso6392(request.getIso6392() != null ? request.getIso6392().toLowerCase() : null)
                .localeCode(request.getLocaleCode())
                .charset(request.getCharset() != null ? request.getCharset() : Language.DEFAULT_CHARSET)
                .flagUrl(request.getFlagUrl())
                .flagPath(request.getFlagPath())
                .isRtl(request.getIsRtl() != null ? request.getIsRtl() : false)
                .isActive(true)
                .isDefault(false) // Never set as default on creation
                .isAdminEnabled(request.getIsAdminEnabled() != null ? request.getIsAdminEnabled() : true)
                .isGuestEnabled(false) // Always start as disabled for guests
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 999)
                .fontFamily(request.getFontFamily())
                .currencyCode(request.getCurrencyCode())
                .currencySymbol(request.getCurrencySymbol())
                .dateFormat(request.getDateFormat() != null ? request.getDateFormat() : Language.DEFAULT_DATE_FORMAT)
                .timeFormat(request.getTimeFormat() != null ? request.getTimeFormat() : Language.DEFAULT_TIME_FORMAT)
                .numberFormat(request.getNumberFormat())
                .decimalSeparator(request.getDecimalSeparator() != null ? request.getDecimalSeparator() : '.')
                .thousandsSeparator(request.getThousandsSeparator() != null ? request.getThousandsSeparator() : ',')
                .uiTranslationProgress(0)
                .channelTranslationProgress(0)
                .epgTranslationEnabled(false)
                .welcomeMessage(request.getWelcomeMessage())
                .supportedPlatforms(request.getSupportedPlatforms() != null ?
                        new HashSet<>(request.getSupportedPlatforms()) : new HashSet<>())
                .build();

        Language saved = languageRepository.save(language);
        log.info("Language created successfully with ID: {}", saved.getId());

        return toDTO(saved);
    }

    // ==========================================
    // READ OPERATIONS
    // ==========================================

    /**
     * Get language by ID
     * @param id Language ID
     * @return Language details
     */
    public LanguageDTO getLanguageById(Long id) {
        log.debug("Fetching language by ID: {}", id);
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));
        return toDTO(language);
    }

    /**
     * Get language by ISO code
     * @param isoCode ISO 639-1 code
     * @return Language details
     */
    @Cacheable(value = "languageByCode", key = "#isoCode")
    public LanguageDTO getLanguageByIsoCode(String isoCode) {
        log.debug("Fetching language by ISO code: {}", isoCode);
        Language language = languageRepository.findByIso6391(isoCode.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ISO code: " + isoCode));
        return toDTO(language);
    }

    /**
     * Get all languages with pagination and filtering
     * @param pageable Pagination parameters
     * @param filter Filter criteria
     * @return Page of languages
     */
    public Page<LanguageDTO> getAllLanguages(Pageable pageable, LanguageFilterRequest filter) {
        log.debug("Fetching languages with filter: {}", filter);

        Specification<Language> spec = buildSpecification(filter);
        Page<Language> languages = languageRepository.findAll(spec, pageable);

        return languages.map(this::toDTO);
    }

    /**
     * Get all active languages for admin platform
     * @return List of admin-enabled languages
     */
    @Cacheable(value = CACHE_NAME_ADMIN)
    public List<LanguageDTO> getAdminLanguages() {
        log.debug("Fetching admin languages");

        List<Language> languages = languageRepository.findByIsActiveAndIsAdminEnabled(true, true);

        return languages.stream()
                .sorted(Comparator.comparing(Language::getDisplayOrder))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active languages for guest TV applications
     * @param platform Platform name (TIZEN, WEBOS, ANDROID)
     * @return List of guest-enabled languages
     */
    @Cacheable(value = CACHE_NAME_GUEST, key = "#platform")
    public List<LanguageDTO> getGuestLanguages(String platform) {
        log.debug("Fetching guest languages for platform: {}", platform);

        List<Language> languages;

        if (StringUtils.hasText(platform)) {
            languages = languageRepository.findGuestLanguagesByPlatform(platform);
        } else {
            languages = languageRepository.findActiveGuestLanguagesOrdered();
        }

        // Additional filtering for translation threshold
        return languages.stream()
                .filter(lang -> lang.getUiTranslationProgress() >= MIN_GUEST_TRANSLATION_THRESHOLD)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get default language
     * @return Default language
     */
    @Cacheable(value = CACHE_NAME_DEFAULT)
    public LanguageDTO getDefaultLanguage() {
        log.debug("Fetching default language");

        return languageRepository.findByIsDefault(true)
                .map(this::toDTO)
                .orElseThrow(() -> new BusinessException("No default language configured"));
    }

    /**
     * Get languages for specific TV platform
     * @param platform Platform name
     * @return List of supported languages
     */
    @Cacheable(value = CACHE_NAME_PLATFORM, key = "#platform")
    public List<LanguageMinimalDTO> getLanguagesForPlatform(String platform) {
        log.debug("Fetching languages for platform: {}", platform);

        return languageRepository.findMinimalGuestLanguages().stream()
                .map(this::toMinimalDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // UPDATE OPERATIONS
    // ==========================================

    /**
     * Update language details
     * @param id Language ID
     * @param request Update request
     * @return Updated language
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME_ADMIN, allEntries = true),
            @CacheEvict(value = CACHE_NAME_GUEST, allEntries = true),
            @CacheEvict(value = "languageByCode", key = "#result.iso6391")
    })
    public LanguageDTO updateLanguage(Long id, LanguageUpdateRequest request) {
        log.info("Updating language ID: {}", id);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));

        // Update basic information
        if (request.getName() != null) {
            language.setName(request.getName());
        }
        if (request.getNativeName() != null) {
            language.setNativeName(request.getNativeName());
        }
        if (request.getLocaleCode() != null) {
            language.setLocaleCode(request.getLocaleCode());
        }
        if (request.getFlagUrl() != null) {
            language.setFlagUrl(request.getFlagUrl());
        }
        if (request.getFlagPath() != null) {
            language.setFlagPath(request.getFlagPath());
        }
        if (request.getIsRtl() != null) {
            language.setIsRtl(request.getIsRtl());
        }
        if (request.getDisplayOrder() != null) {
            language.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getFontFamily() != null) {
            language.setFontFamily(request.getFontFamily());
        }
        if (request.getCurrencyCode() != null) {
            language.setCurrencyCode(request.getCurrencyCode());
        }
        if (request.getCurrencySymbol() != null) {
            language.setCurrencySymbol(request.getCurrencySymbol());
        }
        if (request.getDateFormat() != null) {
            language.setDateFormat(request.getDateFormat());
        }
        if (request.getTimeFormat() != null) {
            language.setTimeFormat(request.getTimeFormat());
        }
        if (request.getWelcomeMessage() != null) {
            language.setWelcomeMessage(request.getWelcomeMessage());
        }

        Language saved = languageRepository.save(language);
        log.info("Language updated successfully: {}", saved.getId());

        return toDTO(saved);
    }

    /**
     * Set language as default
     * @param id Language ID
     * @return Updated language
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_DEFAULT, CACHE_NAME_GUEST, CACHE_NAME_ADMIN}, allEntries = true)
    public LanguageDTO setDefaultLanguage(Long id) {
        log.info("Setting language {} as default", id);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));

        // Validate language is active
        if (!language.getIsActive()) {
            throw new ValidationException("Cannot set inactive language as default");
        }

        // Unset current default
        languageRepository.unsetAllDefaults();

        // Set new default
        language.setIsDefault(true);
        Language saved = languageRepository.save(language);

        log.info("Language {} set as default", saved.getIso6391());

        return toDTO(saved);
    }

    /**
     * Update language status (active/inactive)
     * @param id Language ID
     * @param active Active status
     * @return Updated language
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_ADMIN, CACHE_NAME_GUEST}, allEntries = true)
    public LanguageDTO updateLanguageStatus(Long id, boolean active) {
        log.info("Updating language {} status to: {}", id, active);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));

        // Cannot deactivate default language
        if (!active && language.getIsDefault()) {
            throw new ValidationException("Cannot deactivate default language");
        }

        language.setIsActive(active);

        // If deactivating, also disable for guests
        if (!active) {
            language.setIsGuestEnabled(false);
        }

        Language saved = languageRepository.save(language);
        log.info("Language status updated successfully");

        return toDTO(saved);
    }

    /**
     * Enable/disable language for guest TV apps
     * @param id Language ID
     * @param enabled Guest availability
     * @return Updated language
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME_GUEST, allEntries = true)
    public LanguageDTO updateGuestAvailability(Long id, boolean enabled) {
        log.info("Updating guest availability for language {}: {}", id, enabled);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));

        if (enabled) {
            // Validate before enabling for guests
            if (!language.getIsActive()) {
                throw new ValidationException("Cannot enable inactive language for guests");
            }

            if (language.getUiTranslationProgress() < MIN_GUEST_TRANSLATION_THRESHOLD) {
                throw new ValidationException(
                        String.format("Language must be at least %d%% translated for guest use. Current: %d%%",
                                MIN_GUEST_TRANSLATION_THRESHOLD, language.getUiTranslationProgress())
                );
            }
        }

        language.setIsGuestEnabled(enabled);
        Language saved = languageRepository.save(language);

        log.info("Guest availability updated for language: {}", saved.getIso6391());

        return toDTO(saved);
    }

    /**
     * Update translation progress
     * @param id Language ID
     * @param uiProgress UI translation percentage
     * @param channelProgress Channel translation percentage
     * @return Updated language
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_GUEST}, allEntries = true)
    public LanguageDTO updateTranslationProgress(Long id, Integer uiProgress, Integer channelProgress) {
        log.info("Updating translation progress for language {}: UI={}%, Channels={}%",
                id, uiProgress, channelProgress);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));

        boolean wasGuestReady = language.getUiTranslationProgress() >= MIN_GUEST_TRANSLATION_THRESHOLD;

        if (uiProgress != null) {
            language.setUiTranslationProgress(Math.min(100, Math.max(0, uiProgress)));
        }

        if (channelProgress != null) {
            language.setChannelTranslationProgress(Math.min(100, Math.max(0, channelProgress)));
        }

        // Auto-enable for guests if threshold reached
        boolean isNowGuestReady = language.getUiTranslationProgress() >= MIN_GUEST_TRANSLATION_THRESHOLD;
        if (!wasGuestReady && isNowGuestReady && language.getIsActive()) {
            log.info("Auto-enabling language {} for guests due to translation progress", language.getIso6391());
            language.setIsGuestEnabled(true);
        }

        Language saved = languageRepository.save(language);

        return toDTO(saved);
    }

    /**
     * Update display order for multiple languages
     * @param orderUpdates Map of language ID to new display order
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_ADMIN, CACHE_NAME_GUEST}, allEntries = true)
    public void updateDisplayOrder(Map<Long, Integer> orderUpdates) {
        log.info("Updating display order for {} languages", orderUpdates.size());

        orderUpdates.forEach((id, order) -> {
            languageRepository.updateDisplayOrder(id, order);
        });

        log.info("Display order updated successfully");
    }

    /**
     * Add platform support to language
     * @param id Language ID
     * @param platforms Set of platform names
     * @return Updated language
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_PLATFORM, CACHE_NAME_GUEST}, allEntries = true)
    public LanguageDTO addPlatformSupport(Long id, Set<String> platforms) {
        log.info("Adding platform support for language {}: {}", id, platforms);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));

        platforms.stream()
                .map(String::toUpperCase)
                .forEach(platform -> language.getSupportedPlatforms().add(platform));

        Language saved = languageRepository.save(language);

        return toDTO(saved);
    }

    /**
     * Remove platform support from language
     * @param id Language ID
     * @param platforms Set of platform names to remove
     * @return Updated language
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_PLATFORM, CACHE_NAME_GUEST}, allEntries = true)
    public LanguageDTO removePlatformSupport(Long id, Set<String> platforms) {
        log.info("Removing platform support for language {}: {}", id, platforms);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));

        platforms.stream()
                .map(String::toUpperCase)
                .forEach(platform -> language.getSupportedPlatforms().remove(platform));

        Language saved = languageRepository.save(language);

        return toDTO(saved);
    }

    // ==========================================
    // DELETE OPERATIONS
    // ==========================================

    /**
     * Delete a language
     * @param id Language ID
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_ADMIN, CACHE_NAME_GUEST, CACHE_NAME_DEFAULT, CACHE_NAME_PLATFORM}, allEntries = true)
    public void deleteLanguage(Long id) {
        log.info("Deleting language with ID: {}", id);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with ID: " + id));

        // Cannot delete default language
        if (language.getIsDefault()) {
            throw new ValidationException("Cannot delete default language. Set another language as default first.");
        }

        languageRepository.delete(language);
        log.info("Language deleted successfully: {}", language.getIso6391());
    }

    // ==========================================
    // STATISTICS & REPORTING
    // ==========================================

    /**
     * Get language statistics for dashboard
     * @return Statistics object
     */
    @Cacheable(value = "languageStatistics")
    public LanguageStatisticsDTO getStatistics() {
        log.debug("Generating language statistics");

        LanguageStatisticsProjection stats = languageRepository.getLanguageStatistics();
        Double avgUiProgress = languageRepository.getAverageUiTranslationProgress();

        return LanguageStatisticsDTO.builder()
                .totalLanguages(stats.getTotal())
                .activeLanguages(stats.getActive())
                .guestEnabledLanguages(stats.getGuestEnabled())
                .adminEnabledLanguages(stats.getAdminEnabled())
                .fullyTranslatedLanguages(stats.getFullyTranslated())
                .averageTranslationProgress(avgUiProgress != null ? avgUiProgress : 0.0)
                .build();
    }

    /**
     * Get languages needing translation work
     * @return List of languages below translation threshold
     */
    public List<LanguageDTO> getLanguagesNeedingTranslation() {
        log.debug("Fetching languages needing translation");

        List<Language> languages = languageRepository.findLanguagesNeedingTranslation(100);

        return languages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get languages ready to be enabled for guests
     * @return List of languages meeting guest criteria
     */
    public List<LanguageDTO> getLanguagesReadyForGuests() {
        log.debug("Fetching languages ready for guest enablement");

        List<Language> languages = languageRepository.findLanguagesReadyForGuests();

        return languages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get RTL languages for special handling
     * @return List of RTL languages
     */
    @Cacheable(value = "rtlLanguages")
    public List<LanguageDTO> getRtlLanguages() {
        log.debug("Fetching RTL languages");

        List<Language> languages = languageRepository.findActiveRtlGuestLanguages();

        return languages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // INITIALIZATION & MAINTENANCE
    // ==========================================

    /**
     * Initialize default languages for new installation
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_ADMIN, CACHE_NAME_GUEST, CACHE_NAME_DEFAULT}, allEntries = true)
    public void initializeDefaultLanguages() {
        log.info("Initializing default languages");

        // Check if already initialized
        if (languageRepository.count() > 0) {
            log.warn("Languages already exist, skipping initialization");
            return;
        }

        List<Language> defaultLanguages = createDefaultLanguages();
        languageRepository.saveAll(defaultLanguages);

        log.info("Initialized {} default languages", defaultLanguages.size());
    }

    /**
     * Validate language data integrity
     * @return Validation report
     */
    public ValidationReportDTO validateLanguageData() {
        log.info("Running language data validation");

        ValidationReportDTO report = new ValidationReportDTO();

        // Check for default language
        if (!languageRepository.existsByIsDefaultTrue()) {
            report.addError("No default language configured");
        }

        // Check for duplicate ISO codes
        List<String> duplicates = languageRepository.findDuplicateIsoCodes();
        if (!duplicates.isEmpty()) {
            report.addError("Duplicate ISO codes found: " + String.join(", ", duplicates));
        }

        // Check guest-enabled languages have sufficient translation
        List<Language> guestLanguages = languageRepository.findByIsGuestEnabledTrue();

        for (Language lang : guestLanguages) {
            if (lang.getUiTranslationProgress() < MIN_GUEST_TRANSLATION_THRESHOLD) {
                report.addWarning(String.format(
                        "Language %s is guest-enabled but only %d%% translated",
                        lang.getIso6391(), lang.getUiTranslationProgress()
                ));
            }
        }

        log.info("Validation completed with {} errors and {} warnings",
                report.getErrors().size(), report.getWarnings().size());

        return report;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    /**
     * Build JPA Specification from filter request
     */
    private Specification<Language> buildSpecification(LanguageFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.getIsActive() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
                }
                if (filter.getIsGuestEnabled() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("isGuestEnabled"), filter.getIsGuestEnabled()));
                }
                if (filter.getIsAdminEnabled() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("isAdminEnabled"), filter.getIsAdminEnabled()));
                }
                if (filter.getIsRtl() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("isRtl"), filter.getIsRtl()));
                }
                if (StringUtils.hasText(filter.getSearchTerm())) {
                    String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("nativeName")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("iso6391")), searchPattern)
                    ));
                }
                if (filter.getMinTranslationProgress() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("uiTranslationProgress"), filter.getMinTranslationProgress()
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create default languages for new installation
     */
    private List<Language> createDefaultLanguages() {
        List<Language> languages = new ArrayList<>();

        // English (Default)
        languages.add(Language.builder()
                .name("English")
                .nativeName("English")
                .iso6391("en")
                .iso6392("eng")
                .localeCode("en-US")
                .isDefault(true)
                .isActive(true)
                .isAdminEnabled(true)
                .isGuestEnabled(true)
                .displayOrder(0)
                .uiTranslationProgress(100)
                .channelTranslationProgress(100)
                .currencyCode("USD")
                .currencySymbol("$")
                .supportedPlatforms(new HashSet<>(Arrays.asList("TIZEN", "WEBOS", "ANDROID")))
                .build());

        // French
        languages.add(Language.builder()
                .name("French")
                .nativeName("Français")
                .iso6391("fr")
                .iso6392("fra")
                .localeCode("fr-FR")
                .isActive(true)
                .isAdminEnabled(true)
                .isGuestEnabled(true)
                .displayOrder(1)
                .uiTranslationProgress(100)
                .channelTranslationProgress(100)
                .currencyCode("EUR")
                .currencySymbol("€")
                .supportedPlatforms(new HashSet<>(Arrays.asList("TIZEN", "WEBOS", "ANDROID")))
                .build());

        // Arabic
        languages.add(Language.builder()
                .name("Arabic")
                .nativeName("العربية")
                .iso6391("ar")
                .iso6392("ara")
                .localeCode("ar-DZ")
                .isRtl(true)
                .isActive(true)
                .isAdminEnabled(true)
                .isGuestEnabled(false)
                .displayOrder(2)
                .uiTranslationProgress(70)
                .channelTranslationProgress(60)
                .currencyCode("DZD")
                .currencySymbol("د.ج")
                .fontFamily("'Noto Sans Arabic', Arial, sans-serif")
                .supportedPlatforms(new HashSet<>(Arrays.asList("TIZEN", "WEBOS")))
                .build());

        // Spanish
        languages.add(Language.builder()
                .name("Spanish")
                .nativeName("Español")
                .iso6391("es")
                .iso6392("spa")
                .localeCode("es-ES")
                .isActive(true)
                .isAdminEnabled(true)
                .isGuestEnabled(false)
                .displayOrder(3)
                .uiTranslationProgress(50)
                .channelTranslationProgress(40)
                .currencyCode("EUR")
                .currencySymbol("€")
                .supportedPlatforms(new HashSet<>(Arrays.asList("TIZEN", "WEBOS", "ANDROID")))
                .build());

        return languages;
    }

    /**
     * Convert entity to DTO
     */
    private LanguageDTO toDTO(Language language) {
        return LanguageDTO.builder()
                .id(language.getId())
                .name(language.getName())
                .nativeName(language.getNativeName())
                .iso6391(language.getIso6391())
                .iso6392(language.getIso6392())
                .localeCode(language.getEffectiveLocaleCode())
                .charset(language.getCharset())
                .flagUrl(language.getFlagUrl())
                .flagPath(language.getFlagPath())
                .flagSource(language.getEffectiveFlagSource())
                .isRtl(language.getIsRtl())
                .isActive(language.getIsActive())
                .isDefault(language.getIsDefault())
                .isAdminEnabled(language.getIsAdminEnabled())
                .isGuestEnabled(language.getIsGuestEnabled())
                .displayOrder(language.getDisplayOrder())
                .fontFamily(language.getEffectiveFontFamily())
                .currencyCode(language.getCurrencyCode())
                .currencySymbol(language.getCurrencySymbol())
                .dateFormat(language.getDateFormat())
                .timeFormat(language.getTimeFormat())
                .numberFormat(language.getNumberFormat())
                .decimalSeparator(language.getDecimalSeparator())
                .thousandsSeparator(language.getThousandsSeparator())
                .uiTranslationProgress(language.getUiTranslationProgress())
                .channelTranslationProgress(language.getChannelTranslationProgress())
                .epgTranslationEnabled(language.getEpgTranslationEnabled())
                .welcomeMessage(language.getWelcomeMessage())
                .supportedPlatforms(language.getSupportedPlatforms())
                .overallTranslationProgress(language.getOverallTranslationProgress())
                .isFullyTranslated(language.isFullyTranslated())
                .isReadyForDisplay(language.isReadyForDisplay())
                .isAvailableForAdmin(language.isAvailableForAdmin())
                .isAvailableForGuests(language.isAvailableForGuests())
                .createdAt(language.getCreatedAt())
                .updatedAt(language.getUpdatedAt())
                .createdBy(language.getCreatedBy())
                .lastModifiedBy(language.getLastModifiedBy())
                .build();
    }

    /**
     * Convert to minimal DTO for TV apps
     */
    private LanguageMinimalDTO toMinimalDTO(LanguageMinimalProjection projection) {
        return LanguageMinimalDTO.builder()
                .id(projection.getId())
                .code(projection.getIso6391())
                .name(projection.getNativeName())
                .isRtl(projection.getIsRtl())
                .order(projection.getDisplayOrder())
                .flag(projection.getFlagUrl())
                .build();
    }

    /**
     * Clear all language caches
     */
    @CacheEvict(value = {
            CACHE_NAME_ADMIN,
            CACHE_NAME_GUEST,
            CACHE_NAME_DEFAULT,
            CACHE_NAME_PLATFORM,
            "languageByCode",
            "languageStatistics",
            "rtlLanguages"
    }, allEntries = true)
    public void clearAllCaches() {
        log.info("Clearing all language caches");
    }

    /**
     * Export languages to JSON for backup
     * @return JSON string of all languages
     */
    public String exportLanguagesToJson() {
        log.info("Exporting languages to JSON");
        List<Language> languages = languageRepository.findAll();
        // Use Jackson ObjectMapper or similar to convert to JSON
        // This is a placeholder - implement with your JSON library
        return "{}"; // Implement actual JSON conversion
    }

    /**
     * Import languages from JSON backup
     * @param jsonData JSON string containing language data
     * @return Number of languages imported
     */
    @Transactional
    @CacheEvict(value = {CACHE_NAME_ADMIN, CACHE_NAME_GUEST, CACHE_NAME_DEFAULT}, allEntries = true)
    public int importLanguagesFromJson(String jsonData) {
        log.info("Importing languages from JSON");
        // Parse JSON and create Language entities
        // This is a placeholder - implement with your JSON library
        return 0; // Return actual count
    }
}