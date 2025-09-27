package com.tvboot.tivio.terminal;

import com.tvboot.tivio.common.enumeration.DeviceType;
import com.tvboot.tivio.common.enumeration.TerminalStatus;
import com.tvboot.tivio.room.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @Column(name = "terminal_code", unique = true, nullable = false, length = 50)
    private String terminalCode;
    @Column(name = "serial_number", length = 50)
    private String serialNumber;

    @Column(name = "ip_address", nullable = false, length = 15)
    private String ipAddress;

    @Column(name = "mac_address", unique = true, nullable = false, length = 17)
    private String macAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "platform", length = 20)
    private String platform;

    @Column(name = "firmware_version", length = 20)
    private String firmwareVersion;



    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TerminalStatus status = TerminalStatus.INACTIVE;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen = LocalDateTime.now();
    @Column(name = "uptime", precision = 5)
    private Double uptime; // percentage

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;




}