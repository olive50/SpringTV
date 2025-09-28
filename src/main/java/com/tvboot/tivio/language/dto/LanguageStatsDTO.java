package com.tvboot.tivio.language.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class LanguageStatsDTO {
    private long total;
    private long adminEnabled;
    private long guestEnabled;
    private long rtlLanguages;
    private Map<String, Long> byCharset;
    private Map<String, Long> byCurrency;
}