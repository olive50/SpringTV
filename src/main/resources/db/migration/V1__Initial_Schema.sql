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
                             channel_number INT UNIQUE NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             description TEXT,
                             ip VARCHAR(255),
                             port INT,
                             stream_url VARCHAR(255) NOT NULL,
                             category_id BIGINT,
                             language_id BIGINT UNSIGNED,  -- Changed to match languages.id type
                             logo_url VARCHAR(255),
                             logo_path VARCHAR(255),
                             is_active BOOLEAN NOT NULL DEFAULT TRUE,
                             is_hd BOOLEAN DEFAULT FALSE,
                             is_avialable BOOLEAN DEFAULT TRUE,
                             sort_order INT NOT NULL DEFAULT 0,
                             coment TEXT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraints
                             CONSTRAINT fk_tv_channel_category FOREIGN KEY (category_id)
                                 REFERENCES tv_channel_categories(id) ON DELETE SET NULL,
                             CONSTRAINT fk_tv_channel_language FOREIGN KEY (language_id)
                                 REFERENCES languages(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better performance
CREATE INDEX idx_tv_channel_number ON tv_channels(channel_number);
CREATE INDEX idx_tv_channel_category ON tv_channels(category_id);
CREATE INDEX idx_tv_channel_language ON tv_channels(language_id);
CREATE INDEX idx_tv_channel_active ON tv_channels(is_active);
CREATE INDEX idx_tv_channel_sort_order ON tv_channels(sort_order);


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

-- Create rooms table
CREATE TABLE rooms (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       room_number VARCHAR(50) NOT NULL UNIQUE,
                       room_type VARCHAR(20) NOT NULL,
                       floor_number INT,
                       building VARCHAR(100),
                       capacity INT,
                       price_per_night DECIMAL(10,2),
                       status VARCHAR(20) NOT NULL,
                       description TEXT,
                       channel_package_id BIGINT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       CONSTRAINT fk_room_channel_package FOREIGN KEY (channel_package_id) REFERENCES channel_packages(id) ON DELETE SET NULL
);

-- Create room_amenities table for the amenities collection
CREATE TABLE room_amenities (
                                room_id BIGINT NOT NULL,
                                amenity VARCHAR(255) NOT NULL,
                                PRIMARY KEY (room_id, amenity),
                                CONSTRAINT fk_room_amenities_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_room_status ON rooms(status);
CREATE INDEX idx_room_type ON rooms(room_type);
CREATE INDEX idx_room_floor ON rooms(floor_number);
CREATE INDEX idx_room_number ON rooms(room_number);
CREATE INDEX idx_room_building ON rooms(building);
CREATE INDEX idx_room_capacity ON rooms(capacity);
CREATE INDEX idx_room_price ON rooms(price_per_night);
CREATE INDEX idx_room_created_at ON rooms(created_at);

-- Insert sample data (optional - for development)
INSERT INTO rooms (room_number, room_type, floor_number, building, capacity, price_per_night, status, description) VALUES
                                                                                                                       ('101', 'STANDARD', 1, 'Main Building', 2, 89.99, 'AVAILABLE', 'Standard room with city view'),
                                                                                                                       ('102', 'STANDARD', 1, 'Main Building', 2, 89.99, 'AVAILABLE', 'Standard room with garden view'),
                                                                                                                       ('201', 'DELUXE', 2, 'Main Building', 3, 129.99, 'AVAILABLE', 'Deluxe room with balcony'),
                                                                                                                       ('202', 'DELUXE', 2, 'Main Building', 3, 139.99, 'OCCUPIED', 'Deluxe room with ocean view'),
                                                                                                                       ('301', 'SUITE', 3, 'Main Building', 4, 199.99, 'AVAILABLE', 'Executive suite with living room'),
                                                                                                                       ('302', 'JUNIOR_SUITE', 3, 'Main Building', 3, 169.99, 'AVAILABLE', 'Junior suite with separate seating area'),
                                                                                                                       ('401', 'PRESIDENTIAL_SUITE', 4, 'Main Building', 6, 399.99, 'AVAILABLE', 'Presidential suite with panoramic views'),
                                                                                                                       ('501', 'FAMILY_ROOM', 5, 'Main Building', 5, 159.99, 'MAINTENANCE', 'Family room with extra beds'),
                                                                                                                       ('103', 'SINGLE', 1, 'Annex Building', 1, 69.99, 'AVAILABLE', 'Single room for solo travelers'),
                                                                                                                       ('104', 'DOUBLE', 1, 'Annex Building', 2, 79.99, 'CLEANING', 'Double room with queen bed'),
                                                                                                                       ('105', 'TWIN', 1, 'Annex Building', 2, 79.99, 'AVAILABLE', 'Twin room with two single beds'),
                                                                                                                       ('203', 'DELUXE', 2, 'Annex Building', 3, 119.99, 'OUT_OF_ORDER', 'Deluxe room currently under renovation');

-- Insert sample amenities
INSERT INTO room_amenities (room_id, amenity) VALUES
                                                  (1, 'WiFi'), (1, 'TV'), (1, 'Air Conditioning'), (1, 'Mini Bar'),
                                                  (2, 'WiFi'), (2, 'TV'), (2, 'Air Conditioning'),
                                                  (3, 'WiFi'), (3, 'Smart TV'), (3, 'Air Conditioning'), (3, 'Mini Bar'), (3, 'Balcony'), (3, 'Coffee Maker'),
                                                  (4, 'WiFi'), (4, 'Smart TV'), (4, 'Air Conditioning'), (4, 'Mini Bar'), (4, 'Ocean View'), (4, 'Coffee Maker'),
                                                  (5, 'WiFi'), (5, 'Smart TV'), (5, 'Air Conditioning'), (5, 'Mini Bar'), (5, 'Living Room'), (5, 'Kitchenette'), (5, 'Jacuzzi'),
                                                  (6, 'WiFi'), (6, 'Smart TV'), (6, 'Air Conditioning'), (6, 'Mini Bar'), (6, 'Seating Area'),
                                                  (7, 'WiFi'), (7, 'Multiple Smart TVs'), (7, 'Air Conditioning'), (7, 'Full Bar'), (7, 'Dining Room'), (7, 'Kitchen'), (7, 'Jacuzzi'), (7, 'Private Balcony'),
                                                  (8, 'WiFi'), (8, 'TV'), (8, 'Air Conditioning'), (8, 'Extra Beds'), (8, 'Refrigerator'),
                                                  (9, 'WiFi'), (9, 'TV'), (9, 'Air Conditioning'),
                                                  (10, 'WiFi'), (10, 'TV'), (10, 'Air Conditioning'),
                                                  (11, 'WiFi'), (11, 'TV'), (11, 'Air Conditioning'),
                                                  (12, 'WiFi'), (12, 'TV'), (12, 'Air Conditioning');

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
-- Create terminals table
CREATE TABLE terminals (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           terminal_id VARCHAR(100) NOT NULL UNIQUE,
                           device_type VARCHAR(50) NOT NULL,
                           brand VARCHAR(100) NOT NULL,
                           model VARCHAR(100) NOT NULL,
                           mac_address VARCHAR(17) NOT NULL UNIQUE,
                           ip_address VARCHAR(45) NOT NULL,
                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                           location VARCHAR(200) NOT NULL,
                           room_id BIGINT,
                           last_seen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           firmware_version VARCHAR(50),
                           serial_number VARCHAR(100),
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           response_time INT,
                           uptime DOUBLE,
                           last_ping_time TIMESTAMP,
                           is_online BOOLEAN DEFAULT FALSE,
                           CONSTRAINT fk_terminal_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_terminal_id ON terminals(terminal_id);
CREATE INDEX idx_terminal_mac ON terminals(mac_address);
CREATE INDEX idx_terminal_ip ON terminals(ip_address);
CREATE INDEX idx_terminal_status ON terminals(status);
CREATE INDEX idx_terminal_device_type ON terminals(device_type);
CREATE INDEX idx_terminal_room ON terminals(room_id);
CREATE INDEX idx_terminal_online ON terminals(is_online);
CREATE INDEX idx_terminal_last_seen ON terminals(last_seen);
CREATE INDEX idx_terminal_created_at ON terminals(created_at);

-- Insert sample data (optional - for development)
INSERT INTO terminals (terminal_id, device_type, brand, model, mac_address, ip_address, status, location, room_id, firmware_version, serial_number, is_online, response_time, uptime) VALUES
                                                                                                                                                                                          ('TIVIO-001', 'SMART_TV', 'Samsung', 'QN65Q80AAFXZA', '00:1A:2B:3C:4D:5E', '192.168.1.101', 'ACTIVE', 'Main Building - Lobby', NULL, 'v2.1.5', 'SN1234567890', TRUE, 45, 99.80),
                                                                                                                                                                                          ('TIVIO-002', 'SMART_TV', 'LG', 'OLED65C1PUB', '00:1B:2C:3D:4E:5F', '192.168.1.102', 'ACTIVE', 'Main Building - Conference Room', NULL, 'v3.0.2', 'SN0987654321', TRUE, 38, 99.90),
                                                                                                                                                                                          ('TIVIO-003', 'ANDROID_BOX', 'NVIDIA', 'Shield TV Pro', '00:1C:2D:3E:4F:5A', '192.168.1.103', 'ACTIVE', 'Room 101 - Main Building', 1, 'v8.2.3', 'SN1122334455', TRUE, 25, 99.50),
                                                                                                                                                                                          ('TIVIO-004', 'ANDROID_BOX', 'Xiaomi', 'Mi Box S', '00:1D:2E:3F:4A:5B', '192.168.1.104', 'ACTIVE', 'Room 102 - Main Building', 2, 'v9.0.1', 'SN5566778899', TRUE, 32, 98.70),
                                                                                                                                                                                          ('TIVIO-005', 'SMART_TV', 'Sony', 'XBR-65X90J', '00:1E:2F:3A:4B:5C', '192.168.1.105', 'ACTIVE', 'Room 201 - Main Building', 3, 'v2.5.1', 'SN6677889900', TRUE, 41, 99.20),
                                                                                                                                                                                          ('TIVIO-006', 'SMART_TV', 'Samsung', 'UN65AU8000FXZA', '00:1F:2A:3B:4C:5D', '192.168.1.106', 'MAINTENANCE', 'Room 202 - Main Building', 4, 'v1.8.7', 'SN2233445566', FALSE, NULL, 85.3),
                                                                                                                                                                                          ('TIVIO-007', 'ANDROID_BOX', 'Amazon', 'Fire TV Cube', '00:1A:2B:3C:4D:6E', '192.168.1.107', 'ACTIVE', 'Room 301 - Main Building', 5, 'v7.6.4', 'SN3344556677', TRUE, 29, 97.80),
                                                                                                                                                                                          ('TIVIO-008', 'SMART_TV', 'LG', 'NANO75UQA', '00:1B:2C:3D:4E:6F', '192.168.1.108', 'ACTIVE', 'Room 302 - Main Building', 6, 'v4.2.0', 'SN4455667788', TRUE, 36, 99.1),
                                                                                                                                                                                          ('TIVIO-009', 'SMART_TV', 'TCL', '65R635', '00:1C:2D:3E:4F:6A', '192.168.1.109', 'INACTIVE', 'Storage Room', NULL, 'v3.5.2', 'SN5566778890', FALSE, NULL, NULL),
                                                                                                                                                                                          ('TIVIO-010', 'ANDROID_BOX', 'Formuler', 'Z10 Pro Max', '00:1D:2E:3F:4A:6B', '192.168.1.110', 'ACTIVE', 'Room 401 - Main Building', 7, 'v10.1.3', 'SN6677889901', TRUE, 22, 99.70),
                                                                                                                                                                                          ('TIVIO-011', 'SMART_TV', 'Hisense', '65U8G', '00:1E:2F:3A:4B:6C', '192.168.1.111', 'ACTIVE', 'Room 501 - Main Building', 8, 'v2.9.4', 'SN7788990012', TRUE, 47, 96.50),
                                                                                                                                                                                          ('TIVIO-012', 'ANDROID_BOX', 'BuzzTV', 'XRS4500', '00:1F:2A:3B:4C:6D', '192.168.1.112', 'ACTIVE', 'Room 103 - Annex Building', 9, 'v5.3.1', 'SN8899001123', TRUE, 31, 98.90),
                                                                                                                                                                                          ('TIVIO-013', 'SMART_TV', 'Vizio', 'M65Q7-J03', '00:1A:2B:3C:4D:7E', '192.168.1.113', 'ACTIVE', 'Room 104 - Annex Building', 10, 'v3.7.8', 'SN9900112234', TRUE, 39, 97.20),
                                                                                                                                                                                          ('TIVIO-014', 'ANDROID_BOX', 'Mag', '424', '00:1B:2C:3D:4E:7F', '192.168.1.114', 'MAINTENANCE', 'IT Department', NULL, 'v1.2.3', 'SN0011223345', FALSE, NULL, NULL),
                                                                                                                                                                                          ('TIVIO-015', 'SMART_TV', 'Panasonic', 'TC-65LX600U', '00:1C:2D:3E:4F:7A', '192.168.1.115', 'ACTIVE', 'Room 105 - Annex Building', 11, 'v2.4.6', 'SN1122334456', TRUE, 43, 98.40);
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

CREATE TABLE translations (
                              id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- Foreign key to the languages table
                              language_id BIGINT UNSIGNED NOT NULL,

                              message_key VARCHAR(255) NOT NULL COMMENT 'The translation key (e.g., welcome_header, button_play)',
                              message_value TEXT NOT NULL COMMENT 'The translated text',

    -- Auditing fields (matching your Language table structure)
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              created_by VARCHAR(100) NULL,
                              updated_by VARCHAR(100) NULL,

    -- Foreign key constraint
                              CONSTRAINT fk_translation_language
                                  FOREIGN KEY (language_id)
                                      REFERENCES languages (id)
                                      ON DELETE CASCADE
                                      ON UPDATE CASCADE,

    -- Ensure we don't have duplicate keys for the same language
                              UNIQUE KEY uq_translation_language_key (language_id, message_key),

    -- Indexes for performance
                              KEY idx_translation_language_id (language_id),
                              KEY idx_translation_message_key (message_key),
                              KEY idx_translation_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Stores translations linked to languages';

-- Insert translations for English (assuming language ID 1)
INSERT INTO translations (language_id, message_key, message_value) VALUES
                                                                       (1, 'welcome', 'Welcome to My IPTV'),
                                                                       (1, 'live_tv', 'Live TV'),
                                                                       (1, 'movies', 'Movies'),
                                                                       (1, 'settings', 'Settings'),
                                                                       (1, 'description', 'Your favorite channels in one place.');

-- Insert translations for French (assuming language ID 2)
INSERT INTO translations (language_id, message_key, message_value) VALUES
                                                                       (2, 'welcome', 'Bienvenue sur Mon IPTV'),
                                                                       (2, 'live_tv', 'TV en Direct'),
                                                                       (2, 'movies', 'Films'),
                                                                       (2, 'settings', 'Paramètres'),
                                                                       (2, 'description', 'Vos chaînes préférées en un seul endroit.');

-- Insert translations for Spanish (assuming language ID 3)
INSERT INTO translations (language_id, message_key, message_value) VALUES
                                                                       (3, 'welcome', 'Bienvenido a Mi IPTV'),
                                                                       (3, 'live_tv', 'TV en Vivo'),
                                                                       (3, 'movies', 'Películas'),
                                                                       (3, 'settings', 'Configuración'),
                                                                       (3, 'description', 'Tus canales favoritos en un solo lugar.');



-- TV Channel Categories
INSERT INTO tv_channel_categories (name, description, icon_url) VALUES
                                                                    ('News', 'News and current affairs channels', 'fas fa-newspaper'),
                                                                    ('Sports', 'Sports and athletics channels', 'fas fa-football-ball'),
                                                                    ('Entertainment', 'Movies and entertainment channels', 'fas fa-film'),
                                                                    ('Kids', 'Children and family channels', 'fas fa-child'),
                                                                    ('Documentary', 'Documentary and educational channels', 'fas fa-graduation-cap'),
                                                                    ('Music', 'Music and concert channels', 'fas fa-music'),
                                                                    ('Lifestyle', 'Cooking, travel and lifestyle channels', 'fas fa-utensils');


-- ===================================================================
-- MIGRATION COMPLETE
-- ===================================================================