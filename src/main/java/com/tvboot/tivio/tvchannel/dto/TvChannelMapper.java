package com.tvboot.tivio.tvchannel.dto;


import com.tvboot.tivio.tvchannel.TvChannel;
import org.mapstruct.*;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TvChannelMapper {
    String LOGOS_DIR = "/files/image/image/logos/";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    TvChannel toEntity(TvChannelCreateDTO dto);

    @Mapping(target = "logoUrl", expression = "java(generateLogoUrl(entity))")
    TvChannelResponseDTO toDTO(TvChannel entity);




    // https://www.ibcscorp.com/learning/resources/spring-boot/mapstruct/mapping-multiple-to-one/
    //@Mapping(target="fullName",expression="java(person.getFirstName()+  \" \" + person.getLastName())")

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//    @Mapping(target = "id", ignore = true)
    @Mapping(target="udpUrl", expression="java(tvChannel.getUdpUrl())")
    @Mapping(target="rtpUrl", expression="java(tvChannel.getRtpUrl())")
    @Mapping(target="httpUrl", source = "webUrl")
    @Mapping(target = "logoUrl", expression = "java(generateLogoUrl(tvChannel))")
    TvChannelStreamDTO toStreamDto(TvChannel tvChannel);

    void update(TvChannelUpdateDTO request, @MappingTarget TvChannel tvChannel);

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
                    .path(LOGOS_DIR)
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

                    return baseUrl.append(LOGOS_DIR)
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