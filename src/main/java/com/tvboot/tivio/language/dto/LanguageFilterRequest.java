package com.tvboot.tivio.language.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
        * Language filter request for search/pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageFilterRequest {
    private Boolean isActive;
    private Boolean isGuestEnabled;
    private Boolean isAdminEnabled;
    private Boolean isRtl;
    private String searchTerm; // Searches in name, nativeName, iso codes
    private Integer minTranslationProgress;
    private String platform; // Filter by supported platform
}
