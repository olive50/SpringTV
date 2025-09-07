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
    private String streamUrl;
    private String logoPath;
    private String logoUrl;

    private boolean active;
    private CategoryDTO category;
    private TvLanguageDTO language;

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
    public static class TvLanguageDTO {
        private Long id;
        private String name;
        private String code;
    }
}