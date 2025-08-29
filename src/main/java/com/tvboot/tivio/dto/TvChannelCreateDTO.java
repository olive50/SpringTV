package com.tvboot.tivio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Données pour créer une nouvelle chaîne TV")
public class TvChannelCreateDTO {

    @Schema(description = "Numéro de chaîne unique", example = "105", minimum = "1")
    @Positive(message = "Channel number must be positive")
    private int channelNumber;

    @Schema(description = "Nom de la chaîne", example = "France 24", maxLength = 100)
    @NotBlank(message = "Channel name is required")
    @Size(min = 2, max = 100, message = "Channel name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Description de la chaîne", example = "Chaîne d'information française", maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(description = "Adresse IP du flux", example = "192.168.1.105", pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "Invalid IP address format")
    private String ip;

    @Schema(description = "Port du flux", example = "8005", minimum = "1", maximum = "65535")
    @Min(value = 1, message = "Port must be greater than 0")
    @Max(value = 65535, message = "Port must be less than 65536")
    private int port;

    @Schema(description = "URL du logo de la chaîne", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "ID de la catégorie", example = "1")
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Schema(description = "ID de la langue", example = "2")
    @NotNull(message = "Language ID is required")
    private Long languageId;
}