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


    @Size(max = 255, message = "Flag path must not exceed 255 characters")
    @Column(name = "flag_path", length = 255)
    private String flagPath; // Local path to flag image (e.g., /assets/flags/fr.svg)

    @Column(name = "is_rtl", nullable = false)
    @Builder.Default
    private Boolean isRtl = false; // Right-to-left for Arabic, Hebrew, etc.


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
    private Character decimalSeparator = '.';

    @Column(name = "thousands_separator", length = 1)
    @Builder.Default
    private Character thousandsSeparator = ',';

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}