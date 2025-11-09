package com.tvboot.tivio.wifi;

import com.tvboot.tivio.common.enumeration.AccessPointType;
import com.tvboot.tivio.common.enumeration.WifiSecurityProtocol;
import com.tvboot.tivio.terminal.Terminal;
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
@Table(name = "access_points")
public class AccessPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ssid", length = 32)
    private String ssid;

    @Column(name = "password", length = 63)
    private String password;

    @Column(name = "is_available", nullable = false)
    private Boolean available = false; // Hardware capability

    @Column(name = "is_enabled", nullable = false)
    private Boolean enabled = false; // Admin can disable even if available

    @Enumerated(EnumType.STRING)
    @Column(name = "security_protocol", length = 20)
    private WifiSecurityProtocol securityProtocol; // WPA2_PSK, WPA3_SAE, OPEN

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AccessPointType type; // INTEGRATED ou EXTERNAL

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false, unique = true)
    private Terminal terminal;

    // Dans AccessPoint.java
    public String toWifiQrString() {
        if (!enabled || !available) {
            throw new IllegalStateException("Access point must be enabled and available");
        }

        String authType = switch (securityProtocol) {
            case WPA2_PSK, WPA3_SAE -> "WPA";
            case OPEN -> "nopass";
            default -> "WPA";
        };

        StringBuilder wifi = new StringBuilder("WIFI:");
        wifi.append("T:").append(authType).append(";");
        wifi.append("S:").append(escapeSpecialChars(ssid)).append(";");

        if (securityProtocol != WifiSecurityProtocol.OPEN && password != null) {
            wifi.append("P:").append(escapeSpecialChars(password)).append(";");
        }

        wifi.append(";");
        return wifi.toString();
    }

    private String escapeSpecialChars(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace(":", "\\:")
                .replace("\"", "\\\"");
    }
}