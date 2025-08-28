package com.tvboot.tivio.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TvChannelDTO {
    private Long id;
    private int channelNumber;
    private String name;
    private String description;
    private String ip;
    private int port;
    private String logoUrl;
    private CategoryDTO category;
    private LanguageDTO language;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String description;
        private String iconUrl;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LanguageDTO {
        private Long id;
        private String name;
        private String code;
    }
}