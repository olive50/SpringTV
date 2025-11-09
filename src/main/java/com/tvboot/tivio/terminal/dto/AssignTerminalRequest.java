package com.tvboot.tivio.terminal.dto;

import com.tvboot.tivio.common.enumeration.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTerminalRequest {
    @NotNull
    private LocationType locationType;

    @NotBlank
    private String locationIdentifier;
}