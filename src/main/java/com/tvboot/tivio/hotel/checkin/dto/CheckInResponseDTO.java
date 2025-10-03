package com.tvboot.tivio.hotel.checkin.dto;


import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponseDTO {
    private Long guestId;
    private String guestName;
    private Long roomId;
    private String roomNumber;
    private String status; // SUCCESS, FAILED
    private LocalDateTime checkinTime;
    private String message;
}