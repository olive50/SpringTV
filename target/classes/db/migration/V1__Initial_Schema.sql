CREATE DATABASE IF NOT EXISTS iptv_platform_dev;
USE tivio_db;

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role ENUM('ADMIN', 'USER', 'CONTENT_MANAGER') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    profile_picture VARCHAR(512),
    date_of_birth DATE,
    phone_number VARCHAR(20),
    subscription_type ENUM('FREE', 'BASIC', 'PREMIUM') DEFAULT 'FREE',
    subscription_expiry DATE
    );

CREATE TABLE IF NOT EXISTS channels (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(512),
    stream_url VARCHAR(512) NOT NULL,
    category VARCHAR(255),
    language VARCHAR(100),
    country VARCHAR(100),
    is_hd BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS categories (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS channel_categories (
                                                  channel_id BIGINT NOT NULL,
                                                  category_id BIGINT NOT NULL,
                                                  PRIMARY KEY (channel_id, category_id),
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS user_favorites (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              user_id BIGINT NOT NULL,
                                              channel_id BIGINT NOT NULL,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              UNIQUE KEY unique_user_channel (user_id, channel_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS user_preferences (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                user_id BIGINT NOT NULL UNIQUE,
                                                preferred_language VARCHAR(100),
    preferred_categories JSON,
    auto_play BOOLEAN DEFAULT TRUE,
    quality_preference ENUM('LOW', 'MEDIUM', 'HIGH', 'AUTO') DEFAULT 'AUTO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS epg_programs (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            channel_id BIGINT NOT NULL,
                                            title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    genre VARCHAR(255),
    season_number INT,
    episode_number INT,
    episode_title VARCHAR(255),
    image_url VARCHAR(512),
    rating VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    INDEX idx_channel_time (channel_id, start_time)
    );

CREATE TABLE IF NOT EXISTS user_watch_history (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  user_id BIGINT NOT NULL,
                                                  channel_id BIGINT NOT NULL,
                                                  start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  end_time TIMESTAMP NULL,
                                                  duration INT DEFAULT 0,
                                                  program_id BIGINT NULL,
                                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES epg_programs(id) ON DELETE SET NULL,
    INDEX idx_user_time (user_id, start_time)
    );

CREATE TABLE IF NOT EXISTS advertisements (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              title VARCHAR(255) NOT NULL,
    content_url VARCHAR(512) NOT NULL,
    duration INT NOT NULL,
    target_criteria JSON,
    is_active BOOLEAN DEFAULT TRUE,
    start_date DATE,
    end_date DATE,
    max_impressions INT,
    current_impressions INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS user_subscriptions (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  user_id BIGINT NOT NULL,
                                                  type ENUM('FREE', 'BASIC', 'PREMIUM') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_subscription (user_id, is_active)
    );

CREATE TABLE IF NOT EXISTS settings (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Insert default settings
INSERT INTO settings (setting_key, setting_value, description, is_public) VALUES
                                                                              ('MAX_FAVORITES', '50', 'Maximum number of favorite channels per user', TRUE),
                                                                              ('SESSION_TIMEOUT', '3600', 'User session timeout in seconds', FALSE),
                                                                              ('DEFAULT_QUALITY', 'AUTO', 'Default stream quality', TRUE),
                                                                              ('MAX_CONCURRENT_STREAMS', '3', 'Maximum concurrent streams per user', FALSE),
                                                                              ('ADVERTISEMENT_FREQUENCY', '15', 'Advertisement frequency in minutes', TRUE)
    ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;