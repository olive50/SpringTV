package com.tvboot.tivio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.stream")
public class StreamProperties {

    private HealthCheck healthCheck = new HealthCheck();

    @Data
    public static class HealthCheck {
        private boolean enabled = false;
        private FastCheck fastCheck = new FastCheck();
        private int intervalMinutes = 5;
        private int timeoutSeconds = 5;
        private int maxConcurrentChecks = 10;

        @Data
        public static class FastCheck {
            private boolean enabled = false;
        }
    }
}