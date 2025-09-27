package com.tvboot.tivio.guest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tvboot.tivio.common.enumeration.Gender;
import com.tvboot.tivio.common.enumeration.LoyaltyLevel;
import com.tvboot.tivio.room.Room;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponseDto {

    private Long id;
    private String pmsGuestId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String nationality;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;
    private Boolean vipStatus;
    private LoyaltyLevel loyaltyLevel;

    // Nested DTOs for related entities
    private LanguageDto language;
    private RoomDto room;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Nested DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageDto {
        private Long id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomDto {
        private Long id;
        private String roomNumber;
        private Room.RoomType roomType;
        private int floorNumber;
    }
}
