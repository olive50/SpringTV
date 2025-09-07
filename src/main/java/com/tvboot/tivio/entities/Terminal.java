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

/*--
-- Table structure for table `terminal`
--

DROP TABLE IF EXISTS `terminal`;
CREATE TABLE `terminal` (
  `IP_terminal` varchar(15) NOT NULL default '',
  `type_terminal` enum('DSL4000','AMINET100','other','PHILIPS','SAMSUNG','SAMSUNG_TIZEN','LG') NOT NULL default 'other',
  `f_terminal_sub_type` tinytext NOT NULL,
  `numero_chambre` varchar(30) NOT NULL default '',
  `active` enum('yes','no') NOT NULL default 'no',
  `maintenance` enum('yes','no') NOT NULL default 'no',
  `f_pms_room_number` varchar(30) default NULL,
  `f_guest_room_number` text,
  `f_location` text NOT NULL,
  `f_reboot_flag` enum('true','false') NOT NULL default 'false',
  `parental_code` tinytext NOT NULL,
  `f_wakeup_allowed` enum('yes','no') NOT NULL default 'yes',
  `f_wakeup_mode` enum('internal','external') NOT NULL default 'internal',
  `f_record_type` enum('registration','session') NOT NULL default 'registration',
  PRIMARY KEY  (`IP_terminal`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
*/