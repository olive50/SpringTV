package com.tvboot.tivio.smartapp.amenity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

@Entity
@Table(name = "amenities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean available = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmenityCategory category;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "contact_extension")
    private String contactExtension;

    @Column(name = "contact_phone")
    private String contactPhone;


    @Column(name = "css_style", columnDefinition = "TEXT")
    private String cssStyle;

    @Column(name = "display_order")
    private Integer displayOrder = 0;


    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "image_url")
    private String imagePath;

    private String location;

    @Column(nullable = false)
    private String name;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "short_description", length = 200)
    private String shortDescription;

    @Column(name = "show_in_menu")
    private Boolean showInMenu = true;

    @Column(name = "thumbnail_url")
    private String thumbnailPath;

    public enum AmenityCategory {
        ROOM_SERVICE("Room Service"),
        RESTAURANT("Restaurant & Bar"),
        SPA("Spa & Wellness"),
        FITNESS("Fitness Center"),
        POOL("Swimming Pool"),
        BUSINESS("Business Center"),
        ENTERTAINMENT("Entertainment"),
        CONCIERGE("Concierge"),
        LAUNDRY("Laundry Service"),
        PARKING("Parking"),
        OTHER("Other Services");

        private final String displayName;

        AmenityCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}