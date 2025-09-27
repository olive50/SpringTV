package com.tvboot.tivio.terminal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalStatsDto {
    private long total;
    private long active;
    private long inactive;
    private long offline;
    private long maintenance;
    private long faulty;
    private Map<String, Long> byDeviceType;
    private Map<String, Long> byLocation;
    private Double averageUptime;

}