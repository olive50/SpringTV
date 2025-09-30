package com.tvboot.tivio.smartapp.amenity.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

@Data
public class AmenityCreateDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String shortDescription;
    private String htmlContent;
    private String imagePath;
    private String thumbnailPath;

    @NotNull(message = "Availability status is required")
    private Boolean available = true;

    @NotBlank(message = "Category is required")
    private String category;

    private String location;
    private String contactPhone;
    private String contactExtension;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    private Integer displayOrder = 0;
    private Boolean showInMenu = true;
    private String cssStyle;
}