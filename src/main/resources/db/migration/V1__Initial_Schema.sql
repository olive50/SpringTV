-- ===================================================================
-- TVBOOT IPTV Platform - Initial Database Schema Migration
-- Version: V1__Initial_Schema.sql
-- Target: MySQL 8.0+
-- Description: Creates all initial tables for the Hotel IPTV Management System
-- Charset: utf8mb4 with utf8mb4_unicode_ci collation for full Unicode support
-- ===================================================================

-- Set MySQL 8 specific settings
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- ===================================================================
-- CORE AUTHENTICATION & USER MANAGEMENT
-- ===================================================================

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       role ENUM('ADMIN', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN', 'HOUSEKEEPER', 'TERMINAL', 'GUEST') NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       last_login DATETIME(6),
                       created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                       INDEX idx_users_username (username),
                       INDEX idx_users_email (email),
                       INDEX idx_users_role (role),
                       INDEX idx_users_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- LANGUAGES MANAGEMENT
-- ===================================================================

CREATE TABLE languages (
                           id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                           name VARCHAR(100) NOT NULL,
                           native_name VARCHAR(100) NOT NULL,
                           iso_639_1 VARCHAR(2) NOT NULL,
                           iso_639_2 VARCHAR(3) NULL,
                           locale_code VARCHAR(5) NULL,
                           charset VARCHAR(50) DEFAULT 'UTF-8',
                           flag_url VARCHAR(500) NULL,
                           flag_path VARCHAR(255) NULL,
                           is_rtl TINYINT(1) NOT NULL DEFAULT 0,
                           is_active TINYINT(1) NOT NULL DEFAULT 1,
                           is_default TINYINT(1) NOT NULL DEFAULT 0,
                           is_admin_enabled TINYINT(1) NOT NULL DEFAULT 1,
                           is_guest_enabled TINYINT(1) NOT NULL DEFAULT 0,
                           display_order INT NOT NULL DEFAULT 0,
                           font_family VARCHAR(100) NULL,
                           currency_code VARCHAR(3) NULL,
                           currency_symbol VARCHAR(10) NULL,
                           date_format VARCHAR(50) DEFAULT 'yyyy-MM-dd',
                           time_format VARCHAR(50) DEFAULT 'HH:mm',
                           number_format VARCHAR(50) NULL,
                           decimal_separator CHAR(1) DEFAULT '.',
                           thousands_separator CHAR(1) DEFAULT ',',
                           ui_translation_progress INT UNSIGNED DEFAULT 0,
                           channel_translation_progress INT UNSIGNED DEFAULT 0,
                           epg_translation_enabled TINYINT(1) DEFAULT 0,
                           welcome_message TEXT NULL,
                           created_at DATETIME(6) NOT NULL,
                           updated_at DATETIME(6) NULL,
                           created_by VARCHAR(100) NULL,
                           last_modified_by VARCHAR(100) NULL,
                           

                           PRIMARY KEY (id),
                           UNIQUE KEY uk_language_iso_639_1 (iso_639_1),

                           KEY idx_language_iso_639_1 (iso_639_1),
                           KEY idx_language_active (is_active),
                           KEY idx_language_default (is_default),
                           KEY idx_language_admin_enabled (is_admin_enabled),
                           KEY idx_language_guest_enabled (is_guest_enabled),
                           KEY idx_language_display_order (display_order),
                           KEY idx_language_composite (is_active, is_guest_enabled, display_order),
                           KEY idx_language_locale (locale_code),
                           KEY idx_language_currency (currency_code),
                           KEY idx_language_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE language_supported_platforms (
                                              language_id BIGINT UNSIGNED NOT NULL,
                                              platform VARCHAR(50) NOT NULL,

                                              PRIMARY KEY (language_id, platform),
                                              CONSTRAINT fk_language_platforms_language
                                                  FOREIGN KEY (language_id)
                                                      REFERENCES languages(id)
                                                      ON DELETE CASCADE,

                                              KEY idx_language_platforms_platform (platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- IPTV CONTENT MANAGEMENT
-- ===================================================================

CREATE TABLE tv_channel_categories (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       name VARCHAR(100) NOT NULL UNIQUE,
                                       description TEXT,
                                       icon_url VARCHAR(500),

                                       INDEX idx_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tv_channels (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             channel_number INT NOT NULL UNIQUE,
                             name VARCHAR(100) NOT NULL,
                             description TEXT,
                             ip VARCHAR(45) NOT NULL,
                             port INT NOT NULL,
                             logo_url VARCHAR(200),
                             logo_path VARCHAR(200),
                             category_id BIGINT,
                             language_id BIGINT UNSIGNED,  -- Matches languages.id type
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                             updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                             CONSTRAINT fk_channels_category FOREIGN KEY (category_id) REFERENCES tv_channel_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
                             CONSTRAINT fk_channels_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE SET NULL ON UPDATE CASCADE,

                             UNIQUE KEY unique_ip_port (ip, port),
                             INDEX idx_channels_number (channel_number),
                             INDEX idx_channels_category (category_id),
                             INDEX idx_channels_language (language_id),
                             INDEX idx_channels_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE epg_entries (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             channel_id BIGINT NOT NULL,
                             title VARCHAR(200) NOT NULL,
                             description TEXT,
                             start_time DATETIME(6) NOT NULL,
                             end_time DATETIME(6) NOT NULL,
                             genre VARCHAR(50),

                             CONSTRAINT fk_epg_channel FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE ON UPDATE CASCADE,

                             INDEX idx_epg_channel_time (channel_id, start_time, end_time),
                             INDEX idx_epg_time_range (start_time, end_time),
                             INDEX idx_epg_channel (channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- CHANNEL PACKAGES & SUBSCRIPTIONS
-- ===================================================================

CREATE TABLE channel_packages (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  name VARCHAR(100) NOT NULL UNIQUE,
                                  description TEXT,
                                  price DECIMAL(10,2) DEFAULT 0.00,
                                  is_premium BOOLEAN DEFAULT FALSE,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                                  updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                                  INDEX idx_packages_active (is_active),
                                  INDEX idx_packages_premium (is_premium),
                                  INDEX idx_packages_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE package_channels (
                                  package_id BIGINT NOT NULL,
                                  channel_id BIGINT NOT NULL,

                                  PRIMARY KEY (package_id, channel_id),
                                  CONSTRAINT fk_package_channels_package FOREIGN KEY (package_id) REFERENCES channel_packages(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                  CONSTRAINT fk_package_channels_channel FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE ON UPDATE CASCADE,

                                  INDEX idx_package_channels_package (package_id),
                                  INDEX idx_package_channels_channel (channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- HOTEL MANAGEMENT
-- ===================================================================

CREATE TABLE rooms (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       room_number VARCHAR(20) NOT NULL UNIQUE,
                       room_type ENUM('STANDARD', 'DELUXE', 'SUITE', 'JUNIOR_SUITE', 'PRESIDENTIAL_SUITE', 'FAMILY_ROOM', 'SINGLE', 'DOUBLE', 'TWIN') NOT NULL,
                       floor_number INT,
                       building VARCHAR(50),
                       max_occupancy INT DEFAULT 2,
                       price_per_night DECIMAL(10,2) DEFAULT 0.00,
                       status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_ORDER', 'CLEANING') NOT NULL DEFAULT 'AVAILABLE',
                       description TEXT,
                       amenities JSON,
                       channel_package_id BIGINT,
                       created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                       CONSTRAINT fk_rooms_package FOREIGN KEY (channel_package_id) REFERENCES channel_packages(id) ON DELETE SET NULL ON UPDATE CASCADE,

                       INDEX idx_rooms_number (room_number),
                       INDEX idx_rooms_type (room_type),
                       INDEX idx_rooms_status (status),
                       INDEX idx_rooms_floor (floor_number),
                       INDEX idx_rooms_building (building),
                       INDEX idx_rooms_package (channel_package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE guests (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        guest_id VARCHAR(20) NOT NULL UNIQUE,
                        first_name VARCHAR(50) NOT NULL,
                        last_name VARCHAR(50) NOT NULL,
                        email VARCHAR(100),
                        phone VARCHAR(20),
                        nationality VARCHAR(50),
                        passport_number VARCHAR(20),
                        id_card_number VARCHAR(20),
                        date_of_birth DATE,
                        gender ENUM('MALE', 'FEMALE', 'OTHER'),
                        vip_status BOOLEAN DEFAULT FALSE,
                        loyalty_level ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND'),
                        preferred_language VARCHAR(10),
                        special_requests TEXT,
                        notes TEXT,
                        current_room_id BIGINT,
                        created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                        updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                        CONSTRAINT fk_guests_room FOREIGN KEY (current_room_id) REFERENCES rooms(id) ON DELETE SET NULL ON UPDATE CASCADE,

                        INDEX idx_guests_guest_id (guest_id),
                        INDEX idx_guests_email (email),
                        INDEX idx_guests_phone (phone),
                        INDEX idx_guests_passport (passport_number),
                        INDEX idx_guests_name (first_name, last_name),
                        INDEX idx_guests_vip (vip_status),
                        INDEX idx_guests_loyalty (loyalty_level),
                        INDEX idx_guests_current_room (current_room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reservations (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              reservation_number VARCHAR(50) NOT NULL UNIQUE,
                              guest_id BIGINT NOT NULL,
                              room_id BIGINT NOT NULL,
                              check_in_date DATETIME(6) NOT NULL,
                              check_out_date DATETIME(6) NOT NULL,
                              actual_check_in DATETIME(6),
                              actual_check_out DATETIME(6),
                              number_of_guests INT DEFAULT 1,
                              total_amount DECIMAL(10,2) DEFAULT 0.00,
                              status ENUM('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'CONFIRMED',
                              booking_source VARCHAR(50),
                              special_requests TEXT,
                              notes TEXT,
                              created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                              updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                              CONSTRAINT fk_reservations_guest FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE ON UPDATE CASCADE,
                              CONSTRAINT fk_reservations_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT ON UPDATE CASCADE,

                              INDEX idx_reservations_number (reservation_number),
                              INDEX idx_reservations_guest (guest_id),
                              INDEX idx_reservations_room (room_id),
                              INDEX idx_reservations_status (status),
                              INDEX idx_reservations_check_in (check_in_date),
                              INDEX idx_reservations_check_out (check_out_date),
                              INDEX idx_reservations_dates (check_in_date, check_out_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- TERMINAL & DEVICE MANAGEMENT
-- ===================================================================

CREATE TABLE terminals (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           terminal_id VARCHAR(50) NOT NULL UNIQUE,
                           device_type ENUM('SET_TOP_BOX', 'SMART_TV', 'DESKTOP_PC', 'TABLET', 'MOBILE', 'DISPLAY_SCREEN', 'PROJECTOR') NOT NULL,
                           brand VARCHAR(50),
                           model VARCHAR(100),
                           mac_address VARCHAR(17) UNIQUE,
                           ip_address VARCHAR(45),
                           firmware_version VARCHAR(50),
                           status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'OFFLINE', 'FAULTY') NOT NULL DEFAULT 'ACTIVE',
                           location VARCHAR(100),
                           serial_number VARCHAR(100) UNIQUE,
                           purchase_date DATETIME(6),
                           warranty_expiry DATETIME(6),
                           last_seen DATETIME(6),
                           notes TEXT,
                           room_id BIGINT,
                           created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                           updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                           CONSTRAINT fk_terminals_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL ON UPDATE CASCADE,

                           INDEX idx_terminals_terminal_id (terminal_id),
                           INDEX idx_terminals_device_type (device_type),
                           INDEX idx_terminals_status (status),
                           INDEX idx_terminals_mac_address (mac_address),
                           INDEX idx_terminals_serial (serial_number),
                           INDEX idx_terminals_room (room_id),
                           INDEX idx_terminals_last_seen (last_seen),
                           INDEX idx_terminals_warranty (warranty_expiry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE terminal_channel_assignments (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              terminal_id BIGINT NOT NULL,
                                              channel_id BIGINT NOT NULL,
                                              position INT,
                                              is_enabled BOOLEAN DEFAULT TRUE,
                                              assigned_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),

                                              CONSTRAINT fk_terminal_assignments_terminal FOREIGN KEY (terminal_id) REFERENCES terminals(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                              CONSTRAINT fk_terminal_assignments_channel FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE ON UPDATE CASCADE,

                                              UNIQUE KEY unique_terminal_position (terminal_id, position),
                                              INDEX idx_terminal_assignments_terminal (terminal_id),
                                              INDEX idx_terminal_assignments_channel (channel_id),
                                              INDEX idx_terminal_assignments_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- TRIGGERS FOR DATA INTEGRITY
-- ===================================================================

-- DELIMITER $$
--
-- CREATE TRIGGER before_language_insert
--     BEFORE INSERT ON languages
--     FOR EACH ROW
-- BEGIN
--     IF NEW.is_default = 1 THEN
--     UPDATE languages SET is_default = 0 WHERE is_default = 1;
-- END IF;
-- END$$
--
-- CREATE TRIGGER before_language_update
--     BEFORE UPDATE ON languages
--     FOR EACH ROW
-- BEGIN
--     IF NEW.is_default = 1 AND OLD.is_default = 0 THEN
--     UPDATE languages SET is_default = 0 WHERE is_default = 1 AND id != NEW.id;
-- END IF;
-- END$$
--
-- DELIMITER ;

-- ===================================================================
-- INITIAL DATA INSERTION
-- ===================================================================

-- Users (Password: admin123 - BCrypt)
INSERT INTO users (username, email, password, first_name, last_name, role, is_active) VALUES
                                                                                          ('admin', 'admin@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'System', 'Administrator', 'ADMIN', TRUE),
                                                                                          ('manager', 'manager@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'Hotel', 'Manager', 'MANAGER', TRUE),
                                                                                          ('receptionist', 'receptionist@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'Front', 'Desk', 'RECEPTIONIST', TRUE),
                                                                                          ('technician', 'technician@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'IT', 'Technician', 'TECHNICIAN', TRUE);

-- Insert 10 sample languages with comprehensive data
INSERT INTO languages (
    name, native_name, iso_639_1, iso_639_2, locale_code, charset,
    flag_url, flag_path, is_rtl, is_active, is_default, is_admin_enabled,
    is_guest_enabled, display_order, font_family, currency_code, currency_symbol,
    date_format, time_format, number_format, decimal_separator, thousands_separator,
    ui_translation_progress, channel_translation_progress, epg_translation_enabled,
    welcome_message, created_at, updated_at, created_by, last_modified_by
) VALUES
      (
          'English', 'English', 'en', 'eng', 'en-US', 'UTF-8',
          'https://flags.example.com/us.svg', '/assets/flags/us.svg', 0, 1, 1, 1,
          1, 1, 'Arial, sans-serif', 'USD', '$',
          'MM/dd/yyyy', 'hh:mm a', '#,##0.00', '.', ',',
          100, 95, 1,
          'Welcome to our hotel entertainment system!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'Arabic', 'العربية', 'ar', 'ara', 'ar-SA', 'UTF-8',
          'https://flags.example.com/sa.svg', '/assets/flags/sa.svg', 1, 1, 0, 1,
          1, 2, 'Arial, Noto Sans Arabic', 'SAR', 'ر.س',
          'yyyy/MM/dd', 'HH:mm', '#,##0.00', '.', ',',
          98, 90, 1,
          'مرحباً بكم في نظام الترفيه بالفندق!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'French', 'Français', 'fr', 'fra', 'fr-FR', 'UTF-8',
          'https://flags.example.com/fr.svg', '/assets/flags/fr.svg', 0, 1, 0, 1,
          1, 3, 'Arial, sans-serif', 'EUR', '€',
          'dd/MM/yyyy', 'HH:mm', '# ##0,00', ',', ' ',
          100, 88, 1,
          'Bienvenue dans notre système de divertissement hôtelier!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'Spanish', 'Español', 'es', 'spa', 'es-ES', 'UTF-8',
          'https://flags.example.com/es.svg', '/assets/flags/es.svg', 0, 1, 0, 1,
          1, 4, 'Arial, sans-serif', 'EUR', '€',
          'dd/MM/yyyy', 'HH:mm', '#,##0.00', ',', '.',
          95, 85, 1,
          '¡Bienvenido a nuestro sistema de entretenimiento hotelero!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'German', 'Deutsch', 'de', 'deu', 'de-DE', 'UTF-8',
          'https://flags.example.com/de.svg', '/assets/flags/de.svg', 0, 1, 0, 1,
          1, 5, 'Arial, sans-serif', 'EUR', '€',
          'dd.MM.yyyy', 'HH:mm', '#.##0,00', ',', '.',
          92, 80, 1,
          'Willkommen in unserem Hotel-Unterhaltungssystem!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'Chinese', '中文', 'zh', 'zho', 'zh-CN', 'UTF-8',
          'https://flags.example.com/cn.svg', '/assets/flags/cn.svg', 0, 1, 0, 1,
          1, 6, 'Microsoft YaHei, Noto Sans SC', 'CNY', '¥',
          'yyyy-MM-dd', 'HH:mm', '#,##0.00', '.', ',',
          85, 75, 0,
          '欢迎使用我们的酒店娱乐系统!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'Russian', 'Русский', 'ru', 'rus', 'ru-RU', 'UTF-8',
          'https://flags.example.com/ru.svg', '/assets/flags/ru.svg', 0, 1, 0, 1,
          1, 7, 'Arial, sans-serif', 'RUB', '₽',
          'dd.MM.yyyy', 'HH:mm', '# ##0,00', ',', ' ',
          90, 78, 1,
          'Добро пожаловать в нашу гостиничную развлекательную систему!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'Italian', 'Italiano', 'it', 'ita', 'it-IT', 'UTF-8',
          'https://flags.example.com/it.svg', '/assets/flags/it.svg', 0, 1, 0, 1,
          1, 8, 'Arial, sans-serif', 'EUR', '€',
          'dd/MM/yyyy', 'HH:mm', '#.##0,00', ',', '.',
          88, 72, 0,
          'Benvenuto nel nostro sistema di intrattenimento alberghiero!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'Portuguese', 'Português', 'pt', 'por', 'pt-PT', 'UTF-8',
          'https://flags.example.com/pt.svg', '/assets/flags/pt.svg', 0, 1, 0, 1,
          1, 9, 'Arial, sans-serif', 'EUR', '€',
          'dd-MM-yyyy', 'HH:mm', '# ##0,00', ',', ' ',
          82, 68, 0,
          'Bem-vindo ao nosso sistema de entretenimento hoteleiro!', NOW(), NOW(), 'system', 'system'
      ),
      (
          'Japanese', '日本語', 'ja', 'jpn', 'ja-JP', 'UTF-8',
          'https://flags.example.com/jp.svg', '/assets/flags/jp.svg', 0, 1, 0, 1,
          0, 10, 'Hiragino Sans, Meiryo', 'JPY', '¥',
          'yyyy/MM/dd', 'HH:mm', '#,##0', '.', ',',
          75, 60, 0,
          'ホテルエンターテインメントシステムへようこそ！', NOW(), NOW(), 'system', 'system'
      );

-- Insert supported platforms for the languages
INSERT INTO language_supported_platforms (language_id, platform) VALUES
-- English (supports all platforms)
(1, 'TIZEN'), (1, 'WEBOS'), (1, 'ANDROID'), (1, 'WEB'), (1, 'IOS'),
-- Arabic (supports all platforms)
(2, 'TIZEN'), (2, 'WEBOS'), (2, 'ANDROID'), (2, 'WEB'), (2, 'IOS'),
-- French (supports all platforms)
(3, 'TIZEN'), (3, 'WEBOS'), (3, 'ANDROID'), (3, 'WEB'), (3, 'IOS'),
-- Spanish (supports all platforms)
(4, 'TIZEN'), (4, 'WEBOS'), (4, 'ANDROID'), (4, 'WEB'), (4, 'IOS'),
-- German (supports all platforms)
(5, 'TIZEN'), (5, 'WEBOS'), (5, 'ANDROID'), (5, 'WEB'), (5, 'IOS'),
-- Chinese (supports web and mobile)
(6, 'ANDROID'), (6, 'WEB'), (6, 'IOS'),
-- Russian (supports all platforms)
(7, 'TIZEN'), (7, 'WEBOS'), (7, 'ANDROID'), (7, 'WEB'), (7, 'IOS'),
-- Italian (supports web and mobile)
(8, 'ANDROID'), (8, 'WEB'), (8, 'IOS'),
-- Portuguese (supports web only)
(9, 'WEB'),
-- Japanese (supports web only - still in development)
(10, 'WEB');

-- TV Channel Categories
INSERT INTO tv_channel_categories (name, description, icon_url) VALUES
                                                                    ('News', 'News and current affairs channels', 'fas fa-newspaper'),
                                                                    ('Sports', 'Sports and athletics channels', 'fas fa-football-ball'),
                                                                    ('Entertainment', 'Movies and entertainment channels', 'fas fa-film'),
                                                                    ('Kids', 'Children and family channels', 'fas fa-child'),
                                                                    ('Documentary', 'Documentary and educational channels', 'fas fa-graduation-cap'),
                                                                    ('Music', 'Music and concert channels', 'fas fa-music'),
                                                                    ('Lifestyle', 'Cooking, travel and lifestyle channels', 'fas fa-utensils');

-- TV Channels
INSERT INTO tv_channels (channel_number, name, description, ip, port, category_id, language_id) VALUES
                                                                                                    (101, 'CNN International', 'International news channel', '192.168.1.100', 8001, 1, 1),
                                                                                                    (102, 'BBC World News', 'British international news channel', '192.168.1.101', 8002, 1, 1),
                                                                                                    (103, 'Al Jazeera English', 'Qatari international news channel', '192.168.1.102', 8003, 1, 1),
                                                                                                    (201, 'ESPN', 'Sports channel', '192.168.1.103', 8004, 2, 1),
                                                                                                    (202, 'beIN Sports', 'International sports channel', '192.168.1.104', 8005, 2, 1),
                                                                                                    (301, 'HBO', 'Entertainment channel', '192.168.1.105', 8006, 3, 1),
                                                                                                    (302, 'Netflix', 'Streaming entertainment', '192.168.1.106', 8007, 3, 1);

-- ===================================================================
-- MIGRATION COMPLETE
-- ===================================================================