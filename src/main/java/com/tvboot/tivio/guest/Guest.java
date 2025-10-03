package com.tvboot.tivio.guest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tvboot.tivio.common.enumeration.CheckinStatus;
import com.tvboot.tivio.common.enumeration.Gender;
import com.tvboot.tivio.common.enumeration.LoyaltyLevel;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "guests")
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "pms_guest_id", unique = true)
    private String pmsGuestId; // Hotel-specific guest ID

    @Column(name = "title")
    private String title ;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "nationality")
    private String nationality;

    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY fetching for better performance
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "vip_status")
    private Boolean vipStatus = false;

    @Column(name = "loyalty_level")
    @Enumerated(EnumType.STRING)
    private LoyaltyLevel loyaltyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkin_status")
    private CheckinStatus checkinStatus = CheckinStatus.CHECKED_OUT;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    // Business methods
    public boolean isCheckedIn() {
        return checkinStatus == CheckinStatus.CHECKED_IN;
    }

    public boolean canCheckIn() {
        return checkinStatus != CheckinStatus.CHECKED_IN;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_room_id")
    private Room room;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }



}