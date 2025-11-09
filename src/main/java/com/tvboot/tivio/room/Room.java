package com.tvboot.tivio.room;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@ToString(exclude = {"terminals"})
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

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;


    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Terminal> terminals = new ArrayList<>();


    private Boolean occupied;

    // Embedded guest information (not a separate entity)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "pmsGuestId", column = @Column(name = "guest_pms_id")),
            @AttributeOverride(name = "title", column = @Column(name = "guest_title")),
            @AttributeOverride(name = "languageCode", column = @Column(name = "guest_language")),
            @AttributeOverride(name = "type", column = @Column(name = "guest_type")),
            @AttributeOverride(name = "firstName", column = @Column(name = "guest_first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "guest_last_name"))
    })
    private RoomGuest currentGuest;



    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public enum RoomType {
        TWIN,  DOUBLE_DELUXE,JUNIOR_SUITE,DELUXE_SUITE, SENIOR_SUITE,STANDARD,PRESIDENTIAL_SUITE, JUNIOR_DELUXE,SENIOR_DELUXE
    }

    public void addTerminal(Terminal terminal) {
        terminals.add(terminal);
        terminal.setRoom(this);
    }

    public void removeTerminal(Terminal terminal) {
        terminals.remove(terminal);
        terminal.setRoom(null);
    }

}