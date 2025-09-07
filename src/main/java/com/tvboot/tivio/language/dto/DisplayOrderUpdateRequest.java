package com.tvboot.tivio.language.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Display order update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisplayOrderUpdateRequest {

    @NotNull(message = "Language ID is required")
    private Long languageId;

    @NotNull(message = "Display order is required")
    @Min(0)
    @Max(9999)
    private Integer displayOrder;
}
