package com.tvboot.tivio.language.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Guest availability update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestAvailabilityRequest {

    @NotNull(message = "Guest enabled status is required")
    private Boolean isGuestEnabled;

    private String reason; // Optional reason for enabling/disabling
}