package com.tvboot.tivio.language.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
        * Validation report DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReportDTO {
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @Builder.Default
    private List<String> info = new ArrayList<>();

    private LocalDateTime validatedAt = LocalDateTime.now();

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void addInfo(String info) {
        this.info.add(info);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
