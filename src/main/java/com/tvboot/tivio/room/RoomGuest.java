package com.tvboot.tivio.room;

import com.tvboot.tivio.common.enumeration.GuestType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomGuest {
    private String pmsGuestId;
    private String title;
    private String languageCode;

    @Enumerated(EnumType.STRING)
    private GuestType type;
    private String firstName;
    private String lastName;
}