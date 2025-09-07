package com.tvboot.tivio.language.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * Platform statistics DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatisticsDTO {
    private String platform;
    private Long supportedLanguages;
    private List<String> languageCodes;
}