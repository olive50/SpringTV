package com.tvboot.tivio.room.dto;

import com.tvboot.tivio.common.enumeration.GuestType;
import lombok.Data;

@Data
public class GuestRoomDto {
    private String pmsGuestId;
    private String title;
    private String languageCode;
    private GuestType type;
    private String firstName;
    private String lastName;
}