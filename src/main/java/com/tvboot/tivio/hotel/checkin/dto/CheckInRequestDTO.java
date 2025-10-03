package com.tvboot.tivio.hotel.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequestDTO {

    @NotNull(message = "Guest ID is required")
    private Long guestId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    // Optional: Staff qui fait le check-in
    private Long performedBy;

    // Optional: Notes
    private String notes;
}