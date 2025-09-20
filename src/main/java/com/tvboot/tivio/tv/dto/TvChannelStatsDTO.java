package com.tvboot.tivio.tv.dto;


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
public class TvChannelStatsDTO {
    private long total;
    private long active;
    private long inactive;

    @Builder.Default
    private Map<String, Long> byCategory = new HashMap<>();

    @Builder.Default
    private Map<String, Long> byLanguage = new HashMap<>();
}