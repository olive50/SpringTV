package com.tvboot.tivio.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TvChannelUpdateDTO {
    @Positive(message = "Channel number must be positive")
    private Integer channelNumber;

    @Size(min = 2, max = 100, message = "Channel name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "Invalid IP address format")
    private String ip;

    @Min(value = 1, message = "Port must be greater than 0")
    @Max(value = 65535, message = "Port must be less than 65536")
    private Integer port;

    private String logoUrl;
    private Long categoryId;
    private Long languageId;
    private String streamUrl;
    private boolean active = true;
}