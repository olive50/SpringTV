package com.tvboot.tivio.language.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LanguageResponseDTO {
    private Long id;
    private String name;
    private String nativeName;
    private String iso6391;
    private String iso6392;
    private String localeCode;
    private String charset;
    private String flagUrl;
    private Boolean isRtl;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}