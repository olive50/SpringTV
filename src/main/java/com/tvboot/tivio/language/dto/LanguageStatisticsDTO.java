package com.tvboot.tivio.language.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Language statistics DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageStatisticsDTO {
    private Long totalLanguages;
    private Long activeLanguages;
    private Long guestEnabledLanguages;
    private Long adminEnabledLanguages;
    private Long fullyTranslatedLanguages;
    private Double averageTranslationProgress;

    @Builder.Default
    private List<LanguageProgressDTO> languageProgress = new ArrayList<>();

    @Builder.Default
    private List<PlatformStatisticsDTO> platformStatistics = new ArrayList<>();
}
