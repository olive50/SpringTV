package com.tvboot.tivio.language.dto;

/**
 * Projection interfaces for repository queries
 */
public interface LanguageStatisticsProjection {
    Long getTotal();
    Long getActive();
    Long getGuestEnabled();
    Long getAdminEnabled();
    Long getFullyTranslated();
}
