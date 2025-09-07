package com.tvboot.tivio.language.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
        * Translation progress update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationProgressRequest {

    @Min(0)
    @Max(100)
    private Integer uiTranslationProgress;

    @Min(0)
    @Max(100)
    private Integer channelTranslationProgress;

    private Boolean epgTranslationEnabled;
}