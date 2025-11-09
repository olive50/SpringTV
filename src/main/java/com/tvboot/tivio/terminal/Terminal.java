package com.tvboot.tivio.terminal;

import com.tvboot.tivio.common.enumeration.DeviceType;
import com.tvboot.tivio.common.enumeration.LocationType;
import com.tvboot.tivio.room.Room;
import com.tvboot.tivio.wifi.AccessPoint;
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
    private String terminalCode; //duid for smasung tizen

    @Column(name = "ip_address", nullable = false, length = 15)
    private String ipAddress;

    @Column(name = "mac_address", unique = true, nullable = false, length = 17)
    private String macAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "model",  length = 50)
    private String model;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "platform", length = 20)
    private String platform;

    @Column(name = "firmware_version", length = 20)
    private String firmwareVersion;

    @Column(name = "is_active", nullable = false)
    private Boolean active = false;


    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    private LocationType locationType;

    @Column(name = "location_identifier")
    private String locationIdentifier; // Room number or area name

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    private Room room; // Only filled if locationType == ROOM

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen = LocalDateTime.now();

    @Column(name = "uptime", precision = 5)
    private Double uptime; // percentage

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "soft_ap", nullable = false)
    private Boolean softAp = true;

    @Column(name = "comment")
    private String comment;

    @OneToOne(mappedBy = "terminal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AccessPoint accessPoint;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}