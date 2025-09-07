package com.tvboot.tivio.language.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Language progress DTO for statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageProgressDTO {
    private String languageCode;
    private String languageName;
    private Integer uiProgress;
    private Integer channelProgress;
    private Integer overallProgress;
}