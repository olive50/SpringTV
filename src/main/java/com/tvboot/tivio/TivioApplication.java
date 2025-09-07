
package com.tvboot.tivio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
//@ComponentScan(basePackages = "com.tvboot.tivio") // Explicit component scanning

@EnableJpaRepositories(basePackages = "com.tvboot.tivio")  // Scan for repositories
@EntityScan(basePackages = "com.tvboot.tivio")             // Scan for entities
@EnableJpaAuditing                                          // Enable auditing for @CreatedBy, @LastModifiedBy
@EnableTransactionManagement                                // Enable transaction management
public class TivioApplication {

    public static void main(String[] args) {
        log.info("=== Starting TVBOOT IPTV Platform ===");
        SpringApplication.run(TivioApplication.class, args);
        log.info("=== TVBOOT IPTV Platform Started Successfully ===");
    }
}