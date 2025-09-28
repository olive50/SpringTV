package com.tvboot.tivio.room.dto;

import com.tvboot.tivio.guest.Guest;
import com.tvboot.tivio.guest.dto.GuestResponseDto;
import com.tvboot.tivio.terminal.dto.TerminalDto;
import com.tvboot.tivio.room.Room;
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


    // ✅ FIXED: Use GuestResponseDto instead of Guest entity
    private List<GuestResponseDto> guests;

    // ✅ RECOMMENDED: Create TerminalDto to avoid similar issues
    private List<TerminalDto> terminals; // or keep as Terminal if no lazy loading issues

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}