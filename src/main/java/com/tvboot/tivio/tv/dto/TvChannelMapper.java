package com.tvboot.tivio.tv.dto;


import com.tvboot.tivio.tv.TvChannel;
import org.mapstruct.*;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TvChannelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    TvChannel toEntity(TvChannelCreateDTO dto);

    @Mapping(target = "logoUrl", expression = "java(generateLogoUrl(entity))")
    TvChannelResponseDTO toDTO(TvChannel entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    void updateEntityFromDTO(TvChannelUpdateDTO dto, @MappingTarget TvChannel entity);

    /**
     * Génère l'URL complète du logo basée sur l'IP et le port du serveur actuel
     * @param entity L'entité TvChannel
     * @return URL complète vers le logo ou null si logoPath est vide
     */
    default String generateLogoUrl(TvChannel entity) {
        if (entity == null || !StringUtils.hasText(entity.getLogoPath())) {
            return null;
        }

        try {
            // Méthode 1: Utiliser ServletUriComponentsBuilder (recommandée dans un contexte web)
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/image/")
                    .path(entity.getLogoPath())
                    .toUriString();

        } catch (Exception e) {
            // Méthode 2: Fallback avec récupération manuelle de la requête
            try {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String scheme = request.getScheme();
                    String serverName = request.getServerName();
                    int serverPort = request.getServerPort();
                    String contextPath = request.getContextPath();

                    StringBuilder baseUrl = new StringBuilder();
                    baseUrl.append(scheme).append("://").append(serverName);

                    // Ajouter le port seulement s'il n'est pas standard
                    if ((serverPort != 80 && "http".equals(scheme)) ||
                            (serverPort != 443 && "https".equals(scheme))) {
                        baseUrl.append(":").append(serverPort);
                    }

                    if (StringUtils.hasText(contextPath)) {
                        baseUrl.append(contextPath);
                    }

                    return baseUrl.append("/api/files/image/")
                            .append(entity.getLogoPath())
                            .toString();
                }
            } catch (Exception fallbackException) {
                // Log l'erreur si nécessaire
                System.err.println("Impossible de générer l'URL du logo: " + fallbackException.getMessage());
            }

            return null;
        }
    }


}