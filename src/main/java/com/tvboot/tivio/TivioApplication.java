
package com.tvboot.tivio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
//@ComponentScan(basePackages = "com.tvboot.tivio") // Explicit component scanning
public class TivioApplication {

    public static void main(String[] args) {
        log.info("=== Starting TVBOOT IPTV Platform ===");
        SpringApplication.run(TivioApplication.class, args);
        log.info("=== TVBOOT IPTV Platform Started Successfully ===");
    }
}