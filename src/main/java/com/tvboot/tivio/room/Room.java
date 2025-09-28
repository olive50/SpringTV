package com.tvboot.tivio.room;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tvboot.tivio.guest.Guest;
import com.tvboot.tivio.terminal.Terminal;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = {"terminals", "guests", "channelPackage"})
@EntityListeners(AuditingEntityListener.class)
@Table(name = "rooms", indexes = {
        @Index(name = "idx_room_status", columnList = "status"),
        @Index(name = "idx_room_type", columnList = "room_type"),
        @Index(name = "idx_room_floor", columnList = "floor_number"),
        @Index(name = "idx_room_number", columnList = "room_number")
})
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Room number is required")
    @Column(name = "room_number", unique = true, nullable = false)
    private String roomNumber;

    @NotNull(message = "Room type is required")
    @Column(name = "room_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @PositiveOrZero(message = "Floor number cannot be negative")
    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "building")
    private String building;

    @Positive(message = "Max occupancy must be positive")
    @Column(name = "capacity")
    private Integer capacity;

    @Positive(message = "Price must be positive")
    @Column(name = "price_per_night", precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @NotNull(message = "Room status is required")
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;


    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Terminal> terminals = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Guest> guests = new ArrayList<>();



    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship management methods
    public void addTerminal(Terminal terminal) {
        terminals.add(terminal);
        terminal.setRoom(this);
    }

    public void removeTerminal(Terminal terminal) {
        terminals.remove(terminal);
        terminal.setRoom(null);
    }

    public void addGuest(Guest guest) {
        guests.add(guest);
        guest.setRoom(this);
    }

    public void removeGuest(Guest guest) {
        guests.remove(guest);
        guest.setRoom(null);
    }

    // Business logic methods
    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }

    public boolean canAccommodate(int numberOfGuests) {
        return capacity != null && numberOfGuests <= capacity;
    }

    public boolean isUnderMaintenance() {
        return status == RoomStatus.MAINTENANCE || status == RoomStatus.OUT_OF_ORDER;
    }

    public boolean isOccupied() {
        return status == RoomStatus.OCCUPIED;
    }

    public boolean needsCleaning() {
        return status == RoomStatus.CLEANING;
    }

    public String getFullRoomIdentifier() {
        return (building != null ? building + "-" : "") + roomNumber +
                (floorNumber != null ? " (Floor " + floorNumber + ")" : "");
    }

    public enum RoomType {
        STANDARD, DELUXE, SUITE, JUNIOR_SUITE, PRESIDENTIAL_SUITE,
        FAMILY_ROOM, SINGLE, DOUBLE, TWIN
    }

    public enum RoomStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE, OUT_OF_ORDER, CLEANING
    }
}