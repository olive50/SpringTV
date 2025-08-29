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
     *
     * WHY: HikariCP is the fastest, most reliable connection pool
     * HOW: Configure pool sizes based on expected load
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring HikariCP DataSource...");

        HikariConfig config = new HikariConfig();

        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // Pool configuration - CRITICAL for stability
        config.setMaximumPoolSize(20);  // Max connections in pool
        config.setMinimumIdle(5);       // Minimum idle connections
        config.setConnectionTimeout(30000);    // 30 seconds max wait for connection
        config.setIdleTimeout(600000);         // 10 minutes idle timeout
        config.setMaxLifetime(1800000);        // 30 minutes max connection lifetime
        config.setLeakDetectionThreshold(60000); // 1 minute leak detection

        // Pool name for monitoring
        config.setPoolName("TvBootHikariPool");

        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        // MySQL specific optimizations
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

        HikariDataSource dataSource = new HikariDataSource(config);

        log.info("HikariCP DataSource configured successfully");
        log.info("Maximum pool size: {}", config.getMaximumPoolSize());
        log.info("Minimum idle connections: {}", config.getMinimumIdle());

        return dataSource;
    }

    /**
     * EntityManagerFactory with optimized Hibernate properties
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        log.info("Configuring EntityManagerFactory...");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.tvboot.tivio.entities");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // Hibernate properties for performance
        Properties properties = new Properties();

        // Database dialect
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        // Schema management - IMPORTANT: Use validate in production
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");

        // Performance optimizations
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.jdbc.fetch_size", "50");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.batch_versioned_data", "true");

        // Connection handling
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");

        // Cache configuration (add Redis later for production)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");

        // Logging (disable in production)
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");

        em.setJpaProperties(properties);

        log.info("EntityManagerFactory configured successfully");
        return em;
    }

    /**
     * Transaction Manager
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
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
        public Properties hibernateProperties() {
            Properties properties = new Properties();
            properties.setProperty("hibernate.show_sql", "true");
            properties.setProperty("hibernate.format_sql", "true");
            properties.setProperty("hibernate.use_sql_comments", "true");
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
        public Properties hibernateProperties() {
            Properties properties = new Properties();
            // Disable SQL logging in production
            properties.setProperty("hibernate.show_sql", "false");
            properties.setProperty("hibernate.format_sql", "false");

            // Enable second level cache in production
            properties.setProperty("hibernate.cache.use_second_level_cache", "true");
            properties.setProperty("hibernate.cache.region.factory_class",
                    "org.hibernate.cache.jcache.JCacheRegionFactory");

            return properties;
        }
    }
}