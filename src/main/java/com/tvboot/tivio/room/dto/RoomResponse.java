package com.tvboot.tivio.room.dto;

import com.tvboot.tivio.guest.Guest;
import com.tvboot.tivio.tv.tvpackage.ChannelPackage;
import com.tvboot.tivio.room.Room;
import com.tvboot.tivio.terminal.Terminal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String roomNumber;
    private Room.RoomType roomType;
    private Integer floorNumber;
    private String building;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private Room.RoomStatus status;
    private String description;
    private boolean available;
    private String fullRoomIdentifier;


    // Terminal info
    private List<Guest> guests;
    // Terminal info
    private List<Terminal> terminals;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}