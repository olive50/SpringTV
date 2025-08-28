package com.tvboot.tivio.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "terminals")
public class Terminal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "terminal_id", unique = true, nullable = false)
    private String terminalId; // Unique identifier for the device

    @Column(name = "device_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "mac_address", unique = true)
    private String macAddress;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TerminalStatus status;

    @Column(name = "location")
    private String location;

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "warranty_expiry")
    private LocalDateTime warrantyExpiry;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @OneToMany(mappedBy = "terminal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TerminalChannelAssignment> channelAssignments = new ArrayList<>();

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

    public enum DeviceType {
        SET_TOP_BOX, SMART_TV, DESKTOP_PC, TABLET, MOBILE, DISPLAY_SCREEN, PROJECTOR
    }

    public enum TerminalStatus {
        ACTIVE, INACTIVE, MAINTENANCE, OFFLINE, FAULTY
    }
}