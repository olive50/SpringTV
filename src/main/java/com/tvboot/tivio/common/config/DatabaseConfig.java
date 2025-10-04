package com.tvboot.tivio.common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${DB_USERNAME:postgres}")
    private String username;

    @Value("${DB_PASSWORD:root}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Primary DataSource with optimized HikariCP configuration
     * FIX: Properly configured for PostgreSQL with transaction handling
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring HikariCP DataSource for PostgreSQL...");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // ✅ FIX: Use the driver from properties instead of hardcoding
        config.setDriverClassName(driverClassName);

        // PostgreSQL specific optimizations
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // ✅ FIX: Disable auto-commit for proper transaction handling
        config.setAutoCommit(false);

        // PostgreSQL specific properties
        config.addDataSourceProperty("ApplicationName", "tvboot-iptv");
        config.addDataSourceProperty("logServerErrorDetail", "false");
        config.addDataSourceProperty("prepareThreshold", "5");
        config.addDataSourceProperty("binaryTransfer", "true");

        log.info("HikariCP DataSource configured successfully");
        return new HikariDataSource(config);
    }

    /**
     * EntityManagerFactory with optimized Hibernate properties for PostgreSQL
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        log.info("Configuring EntityManagerFactory for PostgreSQL...");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.tvboot.tivio");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // Hibernate properties optimized for PostgreSQL
        Properties properties = new Properties();


        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        // Schema management - Use validate with Flyway
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");

        properties.setProperty("hibernate.connection.autocommit", "false");
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        properties.setProperty("hibernate.transaction.coordinator_class", "jdbc");
        properties.setProperty("hibernate.resource.transaction.backend", "jdbc");

        // Performance optimizations
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.jdbc.fetch_size", "50");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.batch_versioned_data", "true");


        properties.setProperty("hibernate.connection.characterEncoding", "utf8");
        properties.setProperty("hibernate.connection.useUnicode", "true");

        // Cache configuration (disabled for now, can be enabled with Redis)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");

        // SQL logging - controlled by profile
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");

        // Statistics for monitoring
        properties.setProperty("hibernate.generate_statistics", "false");

        em.setJpaProperties(properties);

        log.info("EntityManagerFactory configured successfully for PostgreSQL");
        return em;
    }

    /**
     * Transaction Manager with proper configuration for PostgreSQL
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        log.info("Configuring JpaTransactionManager...");

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        // Additional transaction configuration
        transactionManager.setJpaDialect(new org.springframework.orm.jpa.vendor.HibernateJpaDialect());
        transactionManager.setNestedTransactionAllowed(true);

        log.info("JpaTransactionManager configured successfully");
        return transactionManager;
    }

    /**
     * Exception translation for better error handling
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Development profile specific configuration
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentConfig {

        @Bean
        public Properties hibernatePropertiesDev() {
            log.info("Loading development-specific Hibernate properties...");
            Properties properties = new Properties();
            properties.setProperty("hibernate.show_sql", "true");
            properties.setProperty("hibernate.format_sql", "true");
            properties.setProperty("hibernate.use_sql_comments", "true");
            properties.setProperty("hibernate.generate_statistics", "true");
            return properties;
        }
    }

    /**
     * Production profile specific configuration
     */
    @Configuration
    @Profile("prod")
    static class ProductionConfig {

        @Bean
        public Properties hibernatePropertiesProd() {
            log.info("Loading production-specific Hibernate properties...");
            Properties properties = new Properties();

            // Disable SQL logging in production
            properties.setProperty("hibernate.show_sql", "false");
            properties.setProperty("hibernate.format_sql", "false");
            properties.setProperty("hibernate.generate_statistics", "false");

            // Enable second level cache in production (if using Redis/Hazelcast)
            properties.setProperty("hibernate.cache.use_second_level_cache", "false"); // Enable when cache is configured
            properties.setProperty("hibernate.cache.region.factory_class",
                    "org.hibernate.cache.jcache.JCacheRegionFactory");

            return properties;
        }
    }
}