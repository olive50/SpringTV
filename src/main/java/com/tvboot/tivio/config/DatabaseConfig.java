package com.tvboot.tivio.config;

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

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Primary DataSource with optimized HikariCP configuration
     * FIX: Properly configured for MySQL 8 with transaction handling
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring HikariCP DataSource for MySQL 8...");

        HikariConfig config = new HikariConfig();

        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // Pool configuration - CRITICAL for stability
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);    // 30 seconds
        config.setIdleTimeout(600000);         // 10 minutes
        config.setMaxLifetime(1800000);        // 30 minutes
        config.setLeakDetectionThreshold(60000); // 1 minute

        // Pool name for monitoring
        config.setPoolName("TvBootHikariPool");

        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        // CRITICAL: MySQL transaction handling fixes
        config.setAutoCommit(false); // Disable auto-commit for proper transaction management
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");

        // MySQL 8 specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // MySQL 8 connection properties for better compatibility
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
        config.addDataSourceProperty("serverTimezone", "UTC");
        config.addDataSourceProperty("createDatabaseIfNotExist", "true");

        // Transaction-related MySQL properties
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("failOverReadOnly", "false");
        config.addDataSourceProperty("maxReconnects", "3");
        config.addDataSourceProperty("initialTimeout", "2");

        // MySQL 8 performance optimizations
        config.addDataSourceProperty("useConfigs", "maxPerformance");
        config.addDataSourceProperty("useCursorFetch", "true");

        HikariDataSource dataSource = new HikariDataSource(config);

        log.info("HikariCP DataSource configured successfully");
        log.info("Auto-commit disabled: {}", !config.isAutoCommit());
        log.info("Maximum pool size: {}", config.getMaximumPoolSize());
        log.info("Minimum idle connections: {}", config.getMinimumIdle());

        return dataSource;
    }

    /**
     * EntityManagerFactory with optimized Hibernate properties for MySQL 8
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        log.info("Configuring EntityManagerFactory for MySQL 8...");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.tvboot.tivio");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // Hibernate properties optimized for MySQL 8
        Properties properties = new Properties();

        // Database dialect - MySQL 8 specific
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        // Schema management - Use validate with Flyway
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");

        // Transaction and connection management - CRITICAL FIXES
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

        // MySQL 8 specific optimizations
        properties.setProperty("hibernate.connection.CharSet", "utf8mb4");
        properties.setProperty("hibernate.connection.characterEncoding", "utf8mb4");
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

        log.info("EntityManagerFactory configured successfully for MySQL 8");
        return em;
    }

    /**
     * Transaction Manager with proper configuration for MySQL 8
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