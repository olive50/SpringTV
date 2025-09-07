package com.tvboot.tivio.language.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal Language DTO for TV applications
 * Optimized for performance and bandwidth
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LanguageMinimalDTO {
    private Long id;
    private String code; // iso6391
    private String name; // nativeName
    private Boolean isRtl;
    private Integer order; // displayOrder
    private String flag; // flagUrl
}