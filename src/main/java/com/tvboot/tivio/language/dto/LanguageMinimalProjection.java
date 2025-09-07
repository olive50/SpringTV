package com.tvboot.tivio.language.dto;
public interface LanguageMinimalProjection {
    Long getId();
    String getIso6391();
    String getNativeName();
    Boolean getIsRtl();
    Integer getDisplayOrder();
    String getFlagUrl();
}