package com.tvboot.tivio.room.dto;

import com.tvboot.tivio.room.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSummary {
    private Long id;
    private String roomNumber;
    private Room.RoomType roomType;
    private Integer floorNumber;
    private String building;
    private BigDecimal pricePerNight;
    private Room.RoomStatus status;
    private boolean available;
    private String fullRoomIdentifier;
}