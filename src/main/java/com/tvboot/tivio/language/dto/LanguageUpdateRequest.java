package com.tvboot.tivio.language.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Language update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageUpdateRequest {

    @Size(max = 100, message = "Language name must not exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Native name must not exceed 100 characters")
    private String nativeName;

    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Locale code must be in format xx or xx-XX")
    private String localeCode;

    @Size(max = 500, message = "Flag URL must not exceed 500 characters")
    private String flagUrl;

    @Size(max = 255, message = "Flag path must not exceed 255 characters")
    private String flagPath;

    private Boolean isRtl;

    @Min(value = 0, message = "Display order must be non-negative")
    @Max(value = 9999, message = "Display order must not exceed 9999")
    private Integer displayOrder;

    @Size(max = 100, message = "Font family must not exceed 100 characters")
    private String fontFamily;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters (ISO 4217)")
    private String currencyCode;

    @Size(max = 10, message = "Currency symbol must not exceed 10 characters")
    private String currencySymbol;

    @Size(max = 50, message = "Date format must not exceed 50 characters")
    private String dateFormat;

    @Size(max = 50, message = "Time format must not exceed 50 characters")
    private String timeFormat;

    private String welcomeMessage;
}
