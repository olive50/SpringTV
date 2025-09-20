-- ===================================================================
-- TVBOOT IPTV Platform - Initial Database Schema (Production Ready)
-- Flyway Migration V1__Initial_Schema.sql
-- ===================================================================
-- Notes:
-- - Lookup/reference tables are used instead of ENUMs for flexibility.
-- - All audit timestamps use DATETIME(6) with CURRENT_TIMESTAMP(6).
-- - Primary keys use BIGINT UNSIGNED.
-- - Charset: utf8mb4 / collation: utf8mb4_unicode_ci
-- ===================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ===================================================================
-- 0. CLEAN START - DROP IF EXISTS (optional in initial migration)
-- (Uncomment if you want to ensure a clean DB during development)
-- ===================================================================
/*
DROP VIEW IF EXISTS v_terminal_status;
DROP VIEW IF EXISTS v_room_availability;
DROP VIEW IF EXISTS v_guest_languages;
DROP VIEW IF EXISTS v_active_channels;

DROP TABLE IF EXISTS package_channels;
DROP TABLE IF EXISTS channel_packages;
DROP TABLE IF EXISTS epg_entries;
DROP TABLE IF EXISTS terminal_channel_assignments;
DROP TABLE IF EXISTS terminals;
DROP TABLE IF EXISTS room_amenities;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS guests;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS tv_channels;
DROP TABLE IF EXISTS tv_channel_categories;
DROP TABLE IF EXISTS translations;
DROP TABLE IF EXISTS language_supported_platforms;
DROP TABLE IF EXISTS languages;
DROP TABLE IF EXISTS users;

-- Lookup tables
DROP TABLE IF EXISTS reservation_statuses;
DROP TABLE IF EXISTS guest_loyalty_levels;
DROP TABLE IF EXISTS terminal_statuses;
DROP TABLE IF EXISTS device_types;
DROP TABLE IF EXISTS room_statuses;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS user_roles;
*/

-- ===================================================================
-- 1. LOOKUP / REFERENCE TABLES (seeded)
-- ===================================================================

CREATE TABLE user_roles (
                            id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            description VARCHAR(255),
                            created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO user_roles (name, description) VALUES
                                               ('ADMIN','Platform administrator'),
                                               ('MANAGER','Hotel / property manager'),
                                               ('RECEPTIONIST','Front desk receptionist'),
                                               ('TECHNICIAN','Technical staff'),
                                               ('HOUSEKEEPER','Housekeeping staff'),
                                               ('TERMINAL','Terminal user'),
                                               ('GUEST','Guest user');

CREATE TABLE room_types (
                            id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO room_types (name) VALUES
                                  ('STANDARD'), ('DELUXE'), ('SUITE'), ('JUNIOR_SUITE'),
                                  ('PRESIDENTIAL_SUITE'), ('FAMILY_ROOM'), ('SINGLE'), ('DOUBLE'), ('TWIN');

CREATE TABLE room_statuses (
                               id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               name VARCHAR(50) NOT NULL UNIQUE,
                               created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO room_statuses (name) VALUES
                                     ('AVAILABLE'), ('OCCUPIED'), ('MAINTENANCE'), ('OUT_OF_ORDER'), ('CLEANING');

CREATE TABLE device_types (
                              id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              name VARCHAR(50) NOT NULL UNIQUE,
                              created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO device_types (name) VALUES
                                    ('SMART_TV'), ('ANDROID_BOX'), ('STREAMING_STICK'), ('SET_TOP_BOX'), ('MEDIA_PLAYER'), ('PROJECTOR');

CREATE TABLE terminal_statuses (
                                   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                   name VARCHAR(50) NOT NULL UNIQUE,
                                   created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO terminal_statuses (name) VALUES
                                         ('ACTIVE'), ('INACTIVE'), ('MAINTENANCE'), ('OFFLINE'), ('FAULTY');

CREATE TABLE guest_loyalty_levels (
                                      id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(50) NOT NULL UNIQUE,
                                      created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO guest_loyalty_levels (name) VALUES
                                            ('BRONZE'), ('SILVER'), ('GOLD'), ('PLATINUM'), ('DIAMOND');

CREATE TABLE reservation_statuses (
                                      id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(50) NOT NULL UNIQUE,
                                      created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO reservation_statuses (name) VALUES
                                            ('CONFIRMED'), ('CHECKED_IN'), ('CHECKED_OUT'), ('CANCELLED'), ('NO_SHOW');

-- ===================================================================
-- 2. USERS / AUTHENTICATION
-- ===================================================================

CREATE TABLE users (
                       id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       role_id BIGINT UNSIGNED NOT NULL,
                       is_active TINYINT(1) NOT NULL DEFAULT 1,
                       last_login DATETIME(6),
                       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                       CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES user_roles (id) ON DELETE RESTRICT,

                       INDEX idx_users_username (username),
                       INDEX idx_users_email (email),
                       INDEX idx_users_role (role_id),
                       INDEX idx_users_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 3. LANGUAGES & TRANSLATION SUPPORT
-- ===================================================================

CREATE TABLE languages (
                           id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           native_name VARCHAR(100) NOT NULL,
                           iso_639_1 VARCHAR(2) NOT NULL UNIQUE,
                           iso_639_2 VARCHAR(3),
                           locale_code VARCHAR(10),
                           charset VARCHAR(50) DEFAULT 'UTF-8',
                           flag_url VARCHAR(500),
                           flag_path VARCHAR(255),
                           is_rtl TINYINT(1) NOT NULL DEFAULT 0,
                           is_active TINYINT(1) NOT NULL DEFAULT 1,
                           is_default TINYINT(1) NOT NULL DEFAULT 0,
                           is_admin_enabled TINYINT(1) NOT NULL DEFAULT 1,
                           is_guest_enabled TINYINT(1) NOT NULL DEFAULT 0,
                           display_order INT NOT NULL DEFAULT 0,
                           font_family VARCHAR(100),
                           currency_code VARCHAR(3),
                           currency_symbol VARCHAR(10),
                           date_format VARCHAR(50) DEFAULT 'yyyy-MM-dd',
                           time_format VARCHAR(50) DEFAULT 'HH:mm',
                           number_format VARCHAR(50),
                           decimal_separator CHAR(1) DEFAULT '.',
                           thousands_separator CHAR(1) DEFAULT ',',
                           ui_translation_progress INT UNSIGNED DEFAULT 0,
                           channel_translation_progress INT UNSIGNED DEFAULT 0,
                           epg_translation_enabled TINYINT(1) DEFAULT 0,
                           welcome_message TEXT,
                           created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                           updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                           created_by VARCHAR(100),
                           last_modified_by VARCHAR(100),

                           INDEX idx_language_active (is_active),
                           INDEX idx_language_default (is_default),
                           INDEX idx_language_guest_enabled (is_guest_enabled),
                           INDEX idx_language_locale (locale_code),
                           INDEX idx_language_currency (currency_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE language_supported_platforms (
                                              language_id BIGINT UNSIGNED NOT NULL,
                                              platform VARCHAR(50) NOT NULL,
                                              PRIMARY KEY (language_id, platform),
                                              CONSTRAINT fk_lang_supported_platform_language FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE CASCADE,
                                              INDEX idx_language_platforms_platform (platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE translations (
                              id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              language_id BIGINT UNSIGNED NOT NULL,
                              message_key VARCHAR(255) NOT NULL,
                              message_value TEXT NOT NULL,
                              created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                              updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                              created_by VARCHAR(100),
                              updated_by VARCHAR(100),
                              CONSTRAINT fk_translations_language FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE CASCADE ON UPDATE CASCADE,
                              UNIQUE KEY uq_translation_language_key (language_id, message_key),
                              INDEX idx_translation_message_key (message_key),
                              INDEX idx_translation_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 4. TV CHANNEL CATEGORIES & CHANNELS
-- ===================================================================

CREATE TABLE tv_channel_categories (
                                       id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(100) NOT NULL UNIQUE,
                                       description TEXT,
                                       icon_url VARCHAR(500),
                                       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                       INDEX idx_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tv_channels (
                             id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             channel_number INT NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             description TEXT,
                             ip VARCHAR(45),
                             port INT,
                             stream_url VARCHAR(1000) NOT NULL,
                             category_id BIGINT UNSIGNED,
                             language_id BIGINT UNSIGNED,
                             logo_url VARCHAR(500),
                             logo_path VARCHAR(255),
                             is_active TINYINT(1) NOT NULL DEFAULT 1,
                             is_hd TINYINT(1) DEFAULT 0,
                             is_available TINYINT(1) DEFAULT 1,
                             sort_order INT NOT NULL DEFAULT 0,
                             comment TEXT,
                             created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                             updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                             CONSTRAINT fk_channel_category FOREIGN KEY (category_id) REFERENCES tv_channel_categories (id) ON DELETE SET NULL,
                             CONSTRAINT fk_channel_language FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE SET NULL,

                             UNIQUE KEY uk_channel_number (channel_number),
                             INDEX idx_channel_name (name),
                             INDEX idx_channel_active (is_active),
                             INDEX idx_channel_category (category_id),
                             INDEX idx_channel_language (language_id),
                             INDEX idx_channel_sort_order (sort_order),
                             INDEX idx_channel_ip_port (ip, port),
                             INDEX idx_channel_active_category (is_active, category_id),
                             INDEX idx_channel_active_language (is_active, language_id),
                             INDEX idx_channel_active_sort (is_active, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 5. EPG ENTRIES
-- ===================================================================

CREATE TABLE epg_entries (
                             id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             channel_id BIGINT UNSIGNED NOT NULL,
                             title VARCHAR(255) NOT NULL,
                             description TEXT,
                             start_time DATETIME(6) NOT NULL,
                             end_time DATETIME(6) NOT NULL,
                             genre VARCHAR(100),
                             created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                             CONSTRAINT fk_epg_channel FOREIGN KEY (channel_id) REFERENCES tv_channels (id) ON DELETE CASCADE,

                             INDEX idx_epg_channel (channel_id),
                             INDEX idx_epg_start_time (start_time),
                             INDEX idx_epg_end_time (end_time),
                             INDEX idx_epg_time_range (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 6. CHANNEL PACKAGES & M2M
-- ===================================================================

CREATE TABLE channel_packages (
                                  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                  name VARCHAR(255) NOT NULL UNIQUE,
                                  description TEXT,
                                  price DECIMAL(10,2) DEFAULT 0.00,
                                  is_premium TINYINT(1) DEFAULT 0,
                                  is_active TINYINT(1) DEFAULT 1,
                                  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                                  INDEX idx_package_name (name),
                                  INDEX idx_package_active (is_active),
                                  INDEX idx_package_premium (is_premium)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE package_channels (
                                  package_id BIGINT UNSIGNED NOT NULL,
                                  channel_id BIGINT UNSIGNED NOT NULL,
                                  PRIMARY KEY (package_id, channel_id),

                                  CONSTRAINT fk_package_channels_package FOREIGN KEY (package_id) REFERENCES channel_packages (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_package_channels_channel FOREIGN KEY (channel_id) REFERENCES tv_channels (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 7. ROOMS & AMENITIES
-- ===================================================================

CREATE TABLE rooms (
                       id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       room_number VARCHAR(50) NOT NULL UNIQUE,
                       room_type_id BIGINT UNSIGNED NOT NULL,
                       floor_number INT,
                       building VARCHAR(100),
                       capacity INT DEFAULT 1,
                       price_per_night DECIMAL(10,2) DEFAULT 0.00,
                       status_id BIGINT UNSIGNED NOT NULL,
                       description TEXT,
                       channel_package_id BIGINT UNSIGNED,
                       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                       CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES room_types (id) ON DELETE RESTRICT,
                       CONSTRAINT fk_room_status FOREIGN KEY (status_id) REFERENCES room_statuses (id) ON DELETE RESTRICT,
                       CONSTRAINT fk_room_package FOREIGN KEY (channel_package_id) REFERENCES channel_packages (id) ON DELETE SET NULL,

                       INDEX idx_room_number (room_number),
                       INDEX idx_room_status (status_id),
                       INDEX idx_room_type (room_type_id),
                       INDEX idx_room_floor (floor_number),
                       INDEX idx_room_building (building)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE room_amenities (
                                room_id BIGINT UNSIGNED NOT NULL,
                                amenity VARCHAR(100) NOT NULL,
                                PRIMARY KEY (room_id, amenity),
                                CONSTRAINT fk_room_amenities_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 8. TERMINALS & ASSIGNMENTS
-- ===================================================================

CREATE TABLE terminals (
                           id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           terminal_identifier VARCHAR(100) NOT NULL UNIQUE,
                           device_type_id BIGINT UNSIGNED NOT NULL,
                           brand VARCHAR(50),
                           model VARCHAR(50),
                           mac_address VARCHAR(17) UNIQUE,
                           ip_address VARCHAR(45),
                           status_id BIGINT UNSIGNED NOT NULL,
                           location VARCHAR(100),
                           room_id BIGINT UNSIGNED,
                           last_seen DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
                           firmware_version VARCHAR(50),
                           serial_number VARCHAR(100),
                           response_time_ms INT,
                           uptime_hours DECIMAL(10,2),
                           last_ping_time DATETIME(6),
                           is_online TINYINT(1) NOT NULL DEFAULT 0,
                           created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                           updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                           CONSTRAINT fk_terminal_device_type FOREIGN KEY (device_type_id) REFERENCES device_types (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_terminal_status FOREIGN KEY (status_id) REFERENCES terminal_statuses (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_terminal_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE SET NULL,

                           INDEX idx_terminal_identifier (terminal_identifier),
                           INDEX idx_terminal_mac (mac_address),
                           INDEX idx_terminal_ip (ip_address),
                           INDEX idx_terminal_status (status_id),
                           INDEX idx_terminal_device_type (device_type_id),
                           INDEX idx_terminal_room (room_id),
                           INDEX idx_terminal_online (is_online)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE terminal_channel_assignments (
                                              id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                              terminal_id BIGINT UNSIGNED NOT NULL,
                                              channel_id BIGINT UNSIGNED NOT NULL,
                                              position INT DEFAULT 0,
                                              is_enabled TINYINT(1) DEFAULT 1,
                                              assigned_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                                              CONSTRAINT fk_terminal_assignment_terminal FOREIGN KEY (terminal_id) REFERENCES terminals (id) ON DELETE CASCADE,
                                              CONSTRAINT fk_terminal_assignment_channel FOREIGN KEY (channel_id) REFERENCES tv_channels (id) ON DELETE CASCADE,

                                              INDEX idx_terminal_assignment_terminal (terminal_id),
                                              INDEX idx_terminal_assignment_channel (channel_id),
                                              INDEX idx_terminal_assignment_position (position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 9. GUESTS, RESERVATIONS
-- ===================================================================

CREATE TABLE guests (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        guest_id VARCHAR(100) NOT NULL UNIQUE,         -- Hotel-specific guest ID
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        email VARCHAR(150) UNIQUE,
                        phone VARCHAR(50),
                        nationality VARCHAR(100),
                        passport_number VARCHAR(50),
                        id_card_number VARCHAR(50),
                        date_of_birth DATE,
                        gender VARCHAR(20),                            -- Enum in Java
                        vip_status BOOLEAN DEFAULT FALSE,
                        loyalty_level VARCHAR(20),                     -- Enum in Java
                        preferred_language VARCHAR(50),
                        special_requests TEXT,
                        notes TEXT,
                        current_room_id BIGINT,
                        created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                        updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                        CONSTRAINT fk_guest_room FOREIGN KEY (current_room_id)
                            REFERENCES rooms(id) ON DELETE SET NULL,

                        INDEX idx_guest_name (first_name, last_name),
                        INDEX idx_guest_email (email),
                        INDEX idx_guest_loyalty (loyalty_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE reservations (
                              id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              reservation_number VARCHAR(100) NOT NULL UNIQUE,
                              guest_id BIGINT UNSIGNED NOT NULL,
                              room_id BIGINT UNSIGNED NOT NULL,
                              check_in_date DATETIME(6) NOT NULL,
                              check_out_date DATETIME(6) NOT NULL,
                              actual_check_in DATETIME(6),
                              actual_check_out DATETIME(6),
                              number_of_guests INT DEFAULT 1,
                              total_amount DECIMAL(10,2) DEFAULT 0.00,
                              status_id BIGINT UNSIGNED NOT NULL,
                              booking_source VARCHAR(255),
                              special_requests TEXT,
                              notes TEXT,
                              created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                              updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                              CONSTRAINT fk_reservation_guest FOREIGN KEY (guest_id) REFERENCES guests (id) ON DELETE CASCADE,
                              CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE,
                              CONSTRAINT fk_reservation_status FOREIGN KEY (status_id) REFERENCES reservation_statuses (id) ON DELETE RESTRICT,

                              INDEX idx_reservation_number (reservation_number),
                              INDEX idx_reservation_guest (guest_id),
                              INDEX idx_reservation_room (room_id),
                              INDEX idx_reservation_status (status_id),
                              INDEX idx_reservation_checkin (check_in_date),
                              INDEX idx_reservation_checkout (check_out_date),
                              INDEX idx_reservation_status_checkin (status_id, check_in_date),
                              INDEX idx_reservation_status_checkout (status_id, check_out_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 10. VIEWS (convenience / read-only)
-- ===================================================================

CREATE OR REPLACE VIEW v_active_channels AS
SELECT
    c.id,
    c.channel_number,
    c.name,
    c.description,
    c.ip,
    c.port,
    c.stream_url,
    c.logo_url,
    c.is_hd,
    c.sort_order,
    cat.name AS category_name,
    lang.name AS language_name,
    lang.iso_639_1 AS language_code
FROM tv_channels c
         LEFT JOIN tv_channel_categories cat ON c.category_id = cat.id
         LEFT JOIN languages lang ON c.language_id = lang.id
WHERE c.is_active = 1 AND c.is_available = 1
ORDER BY c.sort_order ASC, c.name ASC;

CREATE OR REPLACE VIEW v_guest_languages AS
SELECT
    id,
    name,
    native_name,
    iso_639_1,
    locale_code,
    is_rtl,
    display_order,
    flag_url,
    currency_code,
    currency_symbol,
    welcome_message
FROM languages
WHERE is_active = 1 AND is_guest_enabled = 1
ORDER BY is_default DESC, display_order ASC;

CREATE OR REPLACE VIEW v_room_availability AS
SELECT
    r.id,
    r.room_number,
    rt.name AS room_type,
    r.floor_number,
    r.building,
    r.capacity,
    r.price_per_night,
    rs.name AS status,
    cp.name AS package_name,
    COUNT(t.id) AS terminal_count
FROM rooms r
         LEFT JOIN room_types rt ON r.room_type_id = rt.id
         LEFT JOIN room_statuses rs ON r.status_id = rs.id
         LEFT JOIN channel_packages cp ON r.channel_package_id = cp.id
         LEFT JOIN terminals t ON r.id = t.room_id
GROUP BY r.id, r.room_number, rt.name, r.floor_number, r.building, r.capacity, r.price_per_night, rs.name, cp.name;

CREATE OR REPLACE VIEW v_terminal_status AS
SELECT
    t.id,
    t.terminal_identifier,
    dt.name AS device_type,
    t.brand,
    t.model,
    t.ip_address,
    ts.name AS status,
    t.is_online,
    t.last_seen,
    r.room_number,
    r.building
FROM terminals t
         LEFT JOIN device_types dt ON t.device_type_id = dt.id
         LEFT JOIN terminal_statuses ts ON t.status_id = ts.id
         LEFT JOIN rooms r ON t.room_id = r.id;

-- ===================================================================
-- 11. OPTIONAL SEED: Example admin user (you should replace password_hash)
-- ===================================================================
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, is_active)
SELECT 'admin','admin@example.com','<REPLACE_WITH_BCRYPT_HASH>','Admin','User', id, 1 FROM user_roles WHERE name = 'ADMIN'
    ON DUPLICATE KEY UPDATE email = VALUES(email);

-- ===================================================================
-- END
-- ===================================================================

SET FOREIGN_KEY_CHECKS = 1;
