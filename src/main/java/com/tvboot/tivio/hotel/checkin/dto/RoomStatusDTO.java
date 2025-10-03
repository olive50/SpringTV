package com.tvboot.tivio.hotel.checkin.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusDTO {
    private String roomNumber;
    private Boolean occupied;
    private GuestInfoDTO guest; // null si room non occupée

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestInfoDTO {
        private String firstName;
        private String lastName;
        private String languageCode; // "en", "fr", "ar"
        private String languageName;
    }
}