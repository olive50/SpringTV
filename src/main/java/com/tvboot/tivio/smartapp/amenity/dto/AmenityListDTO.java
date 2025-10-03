package com.tvboot.tivio.smartapp.amenity.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

@Data
public class AmenityListDTO {
    private Long id;
    private String name;
    private String shortDescription;
    private String thumbnailUrl;
    private String category;
    private String categoryDisplayName;
    private String location;
    private Boolean available;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    private Integer displayOrder;
}