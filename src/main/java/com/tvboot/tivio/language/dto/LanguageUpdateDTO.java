package com.tvboot.tivio.language.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LanguageUpdateDTO {

    @Size(max = 100, message = "Language name must not exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Native name must not exceed 100 characters")
    private String nativeName;

    @Pattern(regexp = "^[a-z]{3}$", message = "ISO 639-2 code must be 3 lowercase letters")
    private String iso6392;

    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Locale code must be in format xx or xx-XX")
    private String localeCode;

    @Size(max = 50, message = "Charset must not exceed 50 characters")
    private String charset;

    private String flagPath;
    private Boolean isRtl;
    private Boolean isAdminEnabled;
    private Boolean isGuestEnabled;

    @Min(value = 0, message = "Display order must be non-negative")
    @Max(value = 9999, message = "Display order must not exceed 9999")
    private Integer displayOrder;

    private String fontFamily;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String currencyCode;

    @Size(max = 10, message = "Currency symbol must not exceed 10 characters")
    private String currencySymbol;

    private String dateFormat;
    private String timeFormat;
    private String numberFormat;
    private Character decimalSeparator;
    private Character thousandsSeparator;
}