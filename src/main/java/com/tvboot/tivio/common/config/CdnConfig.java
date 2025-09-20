package com.tvboot.tivio.common.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.cdn")
public class CdnConfig {

    private String baseUrl = "http://localhost:8080";
    private String logosPath = "/uploads/logos/channels";
    private boolean enabled = false;

    public String getFullLogoUrl(String logoPath) {
        if (!enabled || logoPath == null) {
            return logoPath;
        }

        // If it's already a full URL, return as is
        if (logoPath.startsWith("http://") || logoPath.startsWith("https://")) {
            return logoPath;
        }

        // If logoPath starts with /, remove it to avoid double slashes
        if (logoPath.startsWith("/")) {
            logoPath = logoPath.substring(1);
        }

        return baseUrl + "/" + logoPath;
    }

    public String getLogoBaseUrl() {
        return enabled ? baseUrl + logosPath : logosPath;
    }
}