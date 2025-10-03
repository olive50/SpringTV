package com.tvboot.tivio.room.dto;


import com.tvboot.tivio.room.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private Room.RoomType roomType;

    @PositiveOrZero(message = "Floor number cannot be negative")
    private Integer floorNumber;

    private String building;

    @Positive(message = "Max occupancy must be positive")
    private Integer capacity;

    @Positive(message = "Price must be positive")
    private BigDecimal pricePerNight;

    @NotNull(message = "Room status is required")
    private Room.RoomStatus status;

    private String description;

}