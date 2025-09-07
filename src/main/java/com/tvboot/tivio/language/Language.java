package com.tvboot.tivio.language;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "languages",
        indexes = {
                @Index(name = "idx_language_iso_639_1", columnList = "iso_639_1"),
                @Index(name = "idx_language_active", columnList = "is_active"),
                @Index(name = "idx_language_default", columnList = "is_default"),
                @Index(name = "idx_language_admin_enabled", columnList = "is_admin_enabled"),
                @Index(name = "idx_language_guest_enabled", columnList = "is_guest_enabled"),
                @Index(name = "idx_language_display_order", columnList = "display_order"),
                @Index(name = "idx_language_composite", columnList = "is_active,is_guest_enabled,display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_language_default", columnNames = "is_default")
        })
@EntityListeners(AuditingEntityListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"createdBy", "lastModifiedBy", "translations"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(chain = true)
public class Language {

//    private static final long serialVersionUID = 1L;

    // ==========================================
    // CONSTANTS
    // ==========================================

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm";
    public static final int MAX_DISPLAY_ORDER = 9999;

    // Common language codes for quick reference
    public enum CommonLanguage {
        ENGLISH("en", "eng", "en-US"),
        FRENCH("fr", "fra", "fr-FR"),
        SPANISH("es", "spa", "es-ES"),
        ARABIC("ar", "ara", "ar-SA"),
        CHINESE("zh", "zho", "zh-CN"),
        RUSSIAN("ru", "rus", "ru-RU"),
        GERMAN("de", "deu", "de-DE");

        private final String iso6391;
        private final String iso6392;
        private final String localeCode;

        CommonLanguage(String iso6391, String iso6392, String localeCode) {
            this.iso6391 = iso6391;
            this.iso6392 = iso6392;
            this.localeCode = localeCode;
        }

        public String getIso6391() { return iso6391; }
        public String getIso6392() { return iso6392; }
        public String getLocaleCode() { return localeCode; }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Language name is required")
    @Size(max = 100, message = "Language name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name; // English, Français, العربية

    @NotBlank(message = "Native name is required")
    @Size(max = 100, message = "Native name must not exceed 100 characters")
    @Column(name = "native_name", nullable = false, length = 100)
    private String nativeName; // اللغة العربية, 中文, Deutsch, etc.

    @NotBlank(message = "ISO 639-1 code is required")
    @Pattern(regexp = "^[a-z]{2}$", message = "ISO 639-1 code must be 2 lowercase letters")
    @Column(name = "iso_639_1", nullable = false, unique = true, length = 2)
    private String iso6391; // en, fr, es, ar, zh, ru, de

    @Pattern(regexp = "^[a-z]{3}$", message = "ISO 639-2 code must be 3 lowercase letters")
    @Column(name = "iso_639_2", length = 3)
    private String iso6392; // eng, fra, spa, ara, zho, rus, deu

    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$",
            message = "Locale code must be in format xx or xx-XX")
    @Column(name = "locale_code", length = 5)
    private String localeCode; // en-US, fr-FR, es-ES, ar-DZ, etc.

    @Size(max = 50, message = "Charset must not exceed 50 characters")
    @Column(name = "charset", length = 50)
    @Builder.Default
    private String charset = DEFAULT_CHARSET; // UTF-8, iso-8859-1

    @Size(max = 500, message = "Flag URL must not exceed 500 characters")
    @Column(name = "flag_url", length = 500)
    private String flagUrl; // URL to flag image (e.g., CDN or external URL)

    @Size(max = 255, message = "Flag path must not exceed 255 characters")
    @Column(name = "flag_path", length = 255)
    private String flagPath; // Local path to flag image (e.g., /assets/flags/fr.svg)

    @Column(name = "is_rtl", nullable = false)
    @Builder.Default
    private Boolean isRtl = false; // Right-to-left for Arabic, Hebrew, etc.

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    // IPTV Platform-specific availability flags
    @Column(name = "is_admin_enabled", nullable = false)
    @Builder.Default
    private Boolean isAdminEnabled = true; // Available in Angular admin platform

    @Column(name = "is_guest_enabled", nullable = false)
    @Builder.Default
    private Boolean isGuestEnabled = false; // Available for hotel guests in TV apps

    @Min(value = 0, message = "Display order must be non-negative")
    @Max(value = MAX_DISPLAY_ORDER, message = "Display order must not exceed " + MAX_DISPLAY_ORDER)
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    // Additional IPTV-specific fields
    @Size(max = 100, message = "Font family must not exceed 100 characters")
    @Column(name = "font_family", length = 100)
    private String fontFamily; // For rendering text in this language

    @Size(max = 20, message = "Currency code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters (ISO 4217)")
    @Column(name = "currency_code", length = 3)
    private String currencyCode; // ISO 4217: USD, EUR, DZD, etc.

    @Size(max = 10, message = "Currency symbol must not exceed 10 characters")
    @Column(name = "currency_symbol", length = 10)
    private String currencySymbol; // $, €, د.ج, etc.

    @Size(max = 50, message = "Date format must not exceed 50 characters")
    @Column(name = "date_format", length = 50)
    @Builder.Default
    private String dateFormat = DEFAULT_DATE_FORMAT; // Localized date format

    @Size(max = 50, message = "Time format must not exceed 50 characters")
    @Column(name = "time_format", length = 50)
    @Builder.Default
    private String timeFormat = DEFAULT_TIME_FORMAT; // Localized time format

    // TV App specific settings
    @Size(max = 50, message = "Number format must not exceed 50 characters")
    @Column(name = "number_format", length = 50)
    private String numberFormat; // #,##0.00 or # ##0,00 etc.

    @Column(name = "decimal_separator", length = 1)
    @Builder.Default
    private char decimalSeparator = '.';

    @Column(name = "thousands_separator", length = 1)
    @Builder.Default
    private char thousandsSeparator = ',';


    // Translation status tracking
    @Column(name = "ui_translation_progress")
    @Min(0) @Max(100)
    @Builder.Default
    private Integer uiTranslationProgress = 0; // Percentage of UI elements translated

    @Column(name = "channel_translation_progress")
    @Min(0) @Max(100)
    @Builder.Default
    private Integer channelTranslationProgress = 0; // Percentage of channel names/descriptions translated


    @Column(name = "epg_translation_enabled")
    @Builder.Default
    private Boolean epgTranslationEnabled = false; // Whether EPG (Electronic Program Guide) is translated

    // Welcome message for hotel guests
    @Column(name = "welcome_message", columnDefinition = "TEXT")
    private String welcomeMessage; // Localized welcome message for TV app

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;


    // Relationships (if needed in future)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "language_supported_platforms",
            joinColumns = @JoinColumn(name = "language_id")
    )
    @Column(name = "platform")
    @Builder.Default
    private Set<String> supportedPlatforms = new HashSet<>(); // "TIZEN", "WEBOS", "ANDROID", etc.

    // ==========================================
    // HELPER METHODS
    // ==========================================

    /**
     * Check if language is Right-to-Left
     */
    public boolean isRightToLeft() {
        return Boolean.TRUE.equals(isRtl);
    }

    /**
     * Get the effective locale code with fallback
     */
    public String getEffectiveLocaleCode() {
        if (localeCode != null && !localeCode.isEmpty()) {
            return localeCode;
        }
        // Fallback to ISO 639-1 code
        return iso6391;
    }

    /**
     * Get Java Locale object
     */
    public Locale getLocale() {
        String effectiveLocale = getEffectiveLocaleCode();
        if (effectiveLocale.contains("-")) {
            String[] parts = effectiveLocale.split("-");
            return new Locale(parts[0], parts[1]);
        }
        return new Locale(effectiveLocale);
    }

    /**
     * Get flag source with URL priority over path
     */
    public String getEffectiveFlagSource() {
        if (flagUrl != null && !flagUrl.isEmpty()) {
            return flagUrl;
        }
        return flagPath;
    }

    /**
     * Get display name for UI (native name with fallback to name)
     */
    public String getDisplayName() {
        if (nativeName != null && !nativeName.isEmpty()) {
            return nativeName;
        }
        return name;
    }

    /**
     * Check if this language is suitable for basic display
     */
    public boolean isReadyForDisplay() {
        return Boolean.TRUE.equals(isActive) &&
                name != null &&
                !name.isEmpty() &&
                iso6391 != null &&
                !iso6391.isEmpty();
    }

    /**
     * Check if language is available for admin platform (Angular app)
     * Used for managing channels, terminals, and hotel configuration
     */
    public boolean isAvailableForAdmin() {
        return Boolean.TRUE.equals(isActive) &&
                Boolean.TRUE.equals(isAdminEnabled) &&
                isReadyForDisplay();
    }

    /**
     * Check if language is ready for guest TV applications
     * Used in Samsung Tizen, LG WebOS apps for guest language selection
     */
    public boolean isAvailableForGuests() {
        return Boolean.TRUE.equals(isActive) &&
                Boolean.TRUE.equals(isGuestEnabled) &&
                isReadyForDisplay() &&
                uiTranslationProgress != null &&
                uiTranslationProgress >= 80; // At least 80% translated
    }

    /**
     * Check if language needs guest app configuration
     * Active for admin but not yet ready for guests
     */
    public boolean needsGuestConfiguration() {
        return Boolean.TRUE.equals(isActive) &&
                Boolean.TRUE.equals(isAdminEnabled) &&
                Boolean.FALSE.equals(isGuestEnabled) &&
                isReadyForDisplay();
    }

    /**
     * Check if language is fully translated for TV apps
     */
    public boolean isFullyTranslated() {
        return uiTranslationProgress != null && uiTranslationProgress == 100 &&
                channelTranslationProgress != null && channelTranslationProgress == 100;
    }

    /**
     * Get the effective font family with fallback
     */
    public String getEffectiveFontFamily() {
        if (fontFamily != null && !fontFamily.isEmpty()) {
            return fontFamily;
        }

        // Provide defaults based on script/language
        if (isRightToLeft()) {
            return "Arial, 'Noto Sans Arabic', sans-serif";
        } else if ("zh".equals(iso6391)) {
            return "'Noto Sans CJK SC', 'Microsoft YaHei', sans-serif";
        } else if ("ja".equals(iso6391)) {
            return "'Noto Sans CJK JP', 'Hiragino Sans', sans-serif";
        } else if ("ko".equals(iso6391)) {
            return "'Noto Sans CJK KR', 'Malgun Gothic', sans-serif";
        } else if ("hi".equals(iso6391)) {
            return "'Noto Sans Devanagari', 'Mangal', sans-serif";
        } else if ("th".equals(iso6391)) {
            return "'Noto Sans Thai', 'Tahoma', sans-serif";
        }

        return "Arial, sans-serif";
    }

    /**
     * Format number according to language settings
     */
//    public String formatNumber(double number) {
//        String formatted = String.format("%.2f", number);
//        if (thousandsSeparator != ' ' && thousandsSeparator !=',') {
//            // Simple formatting - can be enhanced with NumberFormat
//            formatted = formatted.replace(",", thousandsSeparator);
//        }
//        if (decimalSeparator != null && !decimalSeparator.equals('.')) {
//            formatted = formatted.replace(".", decimalSeparator);
//        }
//        return formatted;
//    }

    /**
     * Get platform-specific language code
     * All platforms use the same standard locale code
     */
    public String getPlatformSpecificCode(String platform) {
        // All platforms (Tizen, WebOS, Android, etc.) use the same standard locale
        return getEffectiveLocaleCode();
    }

    /**
     * Check if platform is supported
     */
    public boolean isPlatformSupported(String platform) {
        return supportedPlatforms != null &&
                supportedPlatforms.contains(platform.toUpperCase());
    }

    /**
     * Calculate overall translation readiness
     */
    public int getOverallTranslationProgress() {
        int uiProgress = uiTranslationProgress != null ? uiTranslationProgress : 0;
        int channelProgress = channelTranslationProgress != null ? channelTranslationProgress : 0;
        return (uiProgress + channelProgress) / 2;
    }

    /**
     * Pre-persist validation
     */
    @PrePersist
    @PreUpdate
    private void validateLanguage() {
        // Ensure charset has a default
        if (charset == null || charset.isEmpty()) {
            charset = DEFAULT_CHARSET;
        }

        // Ensure date/time formats have defaults
        if (dateFormat == null || dateFormat.isEmpty()) {
            dateFormat = DEFAULT_DATE_FORMAT;
        }
        if (timeFormat == null || timeFormat.isEmpty()) {
            timeFormat = DEFAULT_TIME_FORMAT;
        }

        // Ensure only one default language
        // This should be handled at service level with proper transaction

        // Initialize supported platforms if null
        if (supportedPlatforms == null) {
            supportedPlatforms = new HashSet<>();
        }

        // Auto-detect RTL languages
        if (iso6391 != null && (iso6391.equals("ar") || iso6391.equals("he") ||
                iso6391.equals("fa") || iso6391.equals("ur"))) {
            isRtl = true;
        }
    }

    /**
     * Post-load initialization
     */
    @PostLoad
    private void postLoad() {
        // Initialize transient fields if needed
        if (supportedPlatforms == null) {
            supportedPlatforms = new HashSet<>();
        }
    }
}