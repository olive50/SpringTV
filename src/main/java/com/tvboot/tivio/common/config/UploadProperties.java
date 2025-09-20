package com.tvboot.tivio.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {
    @Data
    public static class Logos {
        private String path = "uploads/logos/channels/";
        private long maxSize = 5242880; // 5MB en bytes
        private List<String> allowedTypes = List.of(
                "image/png",
                "image/jpeg",
                "image/jpg",
                "image/gif",
                "image/webp"
        );
    }

    private Logos logos = new Logos();



}