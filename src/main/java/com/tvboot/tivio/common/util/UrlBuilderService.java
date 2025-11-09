package com.tvboot.tivio.common.util;

import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlBuilderService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Named("urlBuilder")
    public String buildImageUrl(String imagePath) {
        return imagePath != null
                ? baseUrl + "/files/image/" + imagePath
                : null;
    }
}