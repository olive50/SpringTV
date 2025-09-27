package com.tvboot.tivio.tv.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TvChannelResponseDTO {
    private Long id;
    private int channelNumber;
    private int sortOrder;
    private String name;
    private String description;
    private String ip;
    private int port;
    private String streamUrl;
//    private String logoPath;
    private String logoUrl;

    /**
     * URL complète vers le logo, générée à l'exécution
     * Ex: "http://192.168.1.100:8080/api/files/image/logos/b39e29ad-107f-4acb-9fb7-7b61ec4444ac.png"
     */


    private boolean active;
    private boolean available;
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

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TvLanguageDTO {
        private Long id;
        private String name;
        private String nativeName;
    }
}