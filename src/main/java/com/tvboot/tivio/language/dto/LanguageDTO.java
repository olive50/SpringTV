package com.tvboot.tivio.language.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Main Language DTO for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LanguageDTO {
    private Long id;
    private String name;
    private String nativeName;
    private String iso6391;
    private String iso6392;
    private String localeCode;
    private String charset;
    private String flagUrl;
    private String flagPath;
    private String flagSource; // Computed field
    private Boolean isRtl;
    private Boolean isActive;
    private Boolean isDefault;
    private Boolean isAdminEnabled;
    private Boolean isGuestEnabled;
    private Integer displayOrder;
    private String fontFamily;
    private String currencyCode;
    private String currencySymbol;
    private String dateFormat;
    private String timeFormat;
    private String numberFormat;
    private Character decimalSeparator;
    private Character thousandsSeparator;
    private Integer uiTranslationProgress;
    private Integer channelTranslationProgress;
    private Boolean epgTranslationEnabled;
    private String welcomeMessage;
    private Set<String> supportedPlatforms;

    // Computed fields
    private Integer overallTranslationProgress;
    private Boolean isFullyTranslated;
    private Boolean isReadyForDisplay;
    private Boolean isAvailableForAdmin;
    private Boolean isAvailableForGuests;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastModifiedBy;
}