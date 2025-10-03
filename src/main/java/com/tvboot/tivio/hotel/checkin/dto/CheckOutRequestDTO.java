package com.tvboot.tivio.hotel.checkin.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutRequestDTO {

    @NotNull(message = "Guest ID is required")
    private Long guestId;

    // Optional: Staff qui fait le checkout
    private Long performedBy;

    // Optional: Notes
    private String notes;
}