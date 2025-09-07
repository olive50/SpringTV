package com.tvboot.tivio.language.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch display order update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDisplayOrderRequest {

    @NotEmpty(message = "Order updates cannot be empty")
    @Size(max = 100, message = "Cannot update more than 100 languages at once")
    private List<DisplayOrderUpdateRequest> updates;
}