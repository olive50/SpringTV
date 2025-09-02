package com.tvboot.tivio.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    /**
     * Configuration pour les téléchargements de fichiers
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Taille maximale par fichier (5MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(5));

        // Taille maximale de la requête (10MB)
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));

        // Seuil à partir duquel les fichiers sont écrits sur disque
        factory.setFileSizeThreshold(DataSize.ofKilobytes(512));

        return factory.createMultipartConfig();
    }

    /**
     * Configuration pour servir les fichiers statiques (logos)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les logos des chaînes
        registry.addResourceHandler("/uploads/logos/channels/**")
                .addResourceLocations("file:uploads/logos/channels/")
                .setCachePeriod(3600); // Cache pendant 1 heure

        // Servir autres ressources statiques si nécessaire
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
    }
}