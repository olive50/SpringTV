package com.tvboot.tivio.common.util;

import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileUrlBuilder {

    @Value("${app.protocol:http}")
    private String protocol;

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.port:8080}")
    private String serverPort;

    @Named("imageUrlBuilder")
    public String build(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        return protocol + "://" + serverAddress + ":" + serverPort + "/api/v1/files/image/" + imagePath;
    }
}