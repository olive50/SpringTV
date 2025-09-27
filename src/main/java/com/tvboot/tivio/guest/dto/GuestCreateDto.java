package com.tvboot.tivio.guest.dto;

import com.tvboot.tivio.common.enumeration.Gender;
import com.tvboot.tivio.common.enumeration.LoyaltyLevel;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestCreateDto {

    @Size(max = 50, message = "PMS Guest ID cannot exceed 50 characters")
    private String pmsGuestId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Phone number should be valid")
    private String phone;

    @Size(max = 50, message = "Nationality cannot exceed 50 characters")
    private String nationality;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Builder.Default
    private Boolean vipStatus = false;

    private LoyaltyLevel loyaltyLevel;

    private Long languageId;

    private Long roomId;
}