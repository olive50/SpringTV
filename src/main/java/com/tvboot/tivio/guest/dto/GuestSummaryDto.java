package com.tvboot.tivio.guest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tvboot.tivio.common.enumeration.LoyaltyLevel;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestSummaryDto {

    private Long id;
    private String pmsGuestId;
    private String firstName;
    private String lastName;
    private String email;
    private String nationality;
    private Boolean vipStatus;
    private LoyaltyLevel loyaltyLevel;
    private String currentRoom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}