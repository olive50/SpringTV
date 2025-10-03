package com.tvboot.tivio.hotel.checkin;

import com.tvboot.tivio.auth.User;
import com.tvboot.tivio.guest.Guest;
import com.tvboot.tivio.room.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkin_history")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckinHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(nullable = false)
    private String action; // CHECK_IN, CHECK_OUT, ROOM_CHANGE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @CreationTimestamp
    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}