
package com.tvboot.tivio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Remove CORS configuration from here as it's handled in SecurityConfig
    // This prevents conflicts between CORS configurations
}