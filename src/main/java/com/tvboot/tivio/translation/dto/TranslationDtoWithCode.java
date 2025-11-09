package com.tvboot.tivio.translation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Schema(description = "Translation DTO with language ISO code")
public class TranslationDtoWithCode {

    @Schema(description = "Translation ID", example = "1")
    private Long id;

    @Schema(description = "ISO 639-1 language code", example = "en")
    @JsonProperty("languageCode")
    private String iso6391;

    @Schema(description = "Translation message key (dot notation)", example = "welcome.message")
    private String messageKey;

    @Schema(description = "Translated message value", example = "Welcome to Hotel New Day")
    private String messageValue;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}