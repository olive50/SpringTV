package com.tvboot.tivio.guest.dto;

import com.tvboot.tivio.common.enumeration.Gender;
import com.tvboot.tivio.common.enumeration.LoyaltyLevel;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestSearchDto {

    private String searchTerm;
    private String nationality;
    private Gender gender;
    private Boolean vipStatus;
    private LoyaltyLevel loyaltyLevel;
    private Long roomId;
    private String sortBy = "firstName";
    private String sortDirection = "asc";
    private Integer page = 0;
    private Integer size = 10;
}