package com.tvboot.tivio.smartapp.amenity.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

@Data
public class AmenityUpdateDTO {
    private String name;
    private String shortDescription;
    private String htmlContent;
    private String imageUrl;
    private String thumbnailPath;
    private Boolean available;
    private String category;
    private String location;
    private String contactPhone;
    private String contactExtension;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    private Integer displayOrder;
    private Boolean showInMenu;
    private String cssStyle;
}