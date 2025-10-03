package com.tvboot.tivio.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomStatsDTO {
    private int total;
    private int available;
    private int maintenance;
    private int occupied;
    private Double occupancy;


//    @Builder.Default
//    private Map<String, Long> byCategory = new HashMap<>();
//
//    @Builder.Default
//    private Map<String, Long> byLanguage = new HashMap<>();
}