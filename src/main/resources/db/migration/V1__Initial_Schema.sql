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
-- IPTV CONTENT MANAGEMENT
-- ===================================================================

CREATE TABLE languages (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(100) NOT NULL UNIQUE,
                           code VARCHAR(3) NOT NULL UNIQUE,

                           INDEX idx_languages_code (code),
                           INDEX idx_languages_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
                             logo VARCHAR(500),
                             category_id BIGINT,
                             language_id BIGINT,

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
-- INITIAL DATA INSERTION
-- ===================================================================

-- Users (Password: admin123 - BCrypt)
INSERT INTO users (username, email, password, first_name, last_name, role, is_active) VALUES
                                                                                          ('admin', 'admin@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'System', 'Administrator', 'ADMIN', TRUE),
                                                                                          ('manager', 'manager@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'Hotel', 'Manager', 'MANAGER', TRUE),
                                                                                          ('receptionist', 'receptionist@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'Front', 'Desk', 'RECEPTIONIST', TRUE),
                                                                                          ('technician', 'technician@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'IT', 'Technician', 'TECHNICIAN', TRUE);

-- Languages
INSERT INTO languages (name, code) VALUES
                                       ('English', 'EN'),
                                       ('French', 'FR'),
                                       ('Arabic', 'AR'),
                                       ('Spanish', 'ES'),
                                       ('German', 'DE');

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
                                                                                                    (103, 'Al Jazeera English', 'Qatari international news channel', '192.168.1.102', 8003, 1, 1);
