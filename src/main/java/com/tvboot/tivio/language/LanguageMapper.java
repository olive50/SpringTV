// LanguageMapper.java - Fixed version
package com.tvboot.tivio.language;

import com.tvboot.tivio.common.util.FileUrlBuilder;
import com.tvboot.tivio.language.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import org.mapstruct.*;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Mapper(
        componentModel = "spring",uses = FileUrlBuilder.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LanguageMapper {

    // Remove @Value annotations - they cause issues with MapStruct

   String FLAGS_DIR = "/files/image/image/flags/";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Language toEntity(LanguageCreateDTO dto);

 @Mapping(target = "flagUrl", expression = "java(generateFlagUrlFromEntity(entity))")

//    @Mapping(target = "flagUrl", source = "flagPath", qualifiedByName = "imageUrlBuilder")
    public abstract LanguageResponseDTO toDTO(Language entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "iso6391", ignore = true) // ISO code should not be updated
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(LanguageUpdateDTO dto, @MappingTarget Language entity);

    /**
     * Génère l'URL complète du drapeau à partir de l'entité Language
     * Cette méthode prend en compte le contexte de la requête pour générer une URL dynamique
     *
     * @param entity L'entité Language contenant le flagPath
     * @return L'URL complète du drapeau ou null si pas de flagPath
     */
    default String generateFlagUrlFromEntity(Language entity) {
        if (entity == null || !StringUtils.hasText(entity.getFlagPath())) {
            return null;
        }

        String flagPath = entity.getFlagPath();

        try {
            // Méthode 1: Utiliser ServletUriComponentsBuilder (recommandée dans un contexte web)
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(FLAGS_DIR)
                    .path(flagPath)
                    .toUriString();

        } catch (Exception e) {
            // Méthode 2: Fallback avec récupération manuelle de la requête
            try {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String scheme = request.getScheme();              // http ou https
                    String serverName = request.getServerName();      // localhost ou domain.com
                    int serverPort = request.getServerPort();         // 8080, 80, 443, etc.
                    String contextPath = request.getContextPath();    // "" ou "/app"

                    StringBuilder baseUrl = new StringBuilder();
                    baseUrl.append(scheme).append("://").append(serverName);

                    // Ajouter le port seulement s'il n'est pas standard
                    if ((serverPort != 80 && "http".equals(scheme)) ||
                            (serverPort != 443 && "https".equals(scheme))) {
                        baseUrl.append(":").append(serverPort);
                    }

                    // Ajouter le context path s'il existe
                    if (StringUtils.hasText(contextPath)) {
                        baseUrl.append(contextPath);
                    }

                    return baseUrl.append("/files/image/")
                            .append(flagPath)
                            .toString();
                }
            } catch (Exception fallbackException) {
                // Log l'erreur si nécessaire
                System.err.println("Impossible de générer l'URL du drapeau pour l'entité " +
                        entity.getId() + ": " + fallbackException.getMessage());
            }

            // Méthode 3: Dernier fallback - URL relative
            return FLAGS_DIR + flagPath;
        }
    }


    @AfterMapping
    default void setDefaults(@MappingTarget Language entity, LanguageCreateDTO dto) {
        if (entity.getCharset() == null) {
            entity.setCharset(Language.DEFAULT_CHARSET);
        }
        if (entity.getDateFormat() == null) {
            entity.setDateFormat(Language.DEFAULT_DATE_FORMAT);
        }
        if (entity.getTimeFormat() == null) {
            entity.setTimeFormat(Language.DEFAULT_TIME_FORMAT);
        }
        if (entity.getIsRtl() == null) {
            entity.setIsRtl(false);
        }
        if (entity.getIsAdminEnabled() == null) {
            entity.setIsAdminEnabled(true);
        }
        if (entity.getIsGuestEnabled() == null) {
            entity.setIsGuestEnabled(false);
        }
        if (entity.getDisplayOrder() == null) {
            entity.setDisplayOrder(0);
        }
        if (entity.getDecimalSeparator() == null) {
            entity.setDecimalSeparator('.');
        }
        if (entity.getThousandsSeparator() == null) {
            entity.setThousandsSeparator(',');
        }
    }
}