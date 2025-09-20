-- ===================================================================
-- TVBOOT IPTV Platform - Initial Database Schema
-- Migration: V001__Create_core_tables.sql
-- Description: Create core tables for users, authentication, and basic entities
-- Author: TVBOOT Team
-- Date: 2025-01-20
-- ===================================================================

-- ==========================================
-- USERS AND AUTHENTICATION TABLES
-- ==========================================

-- Users table
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       role ENUM('ADMIN', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN', 'HOUSEKEEPER', 'TERMINAL', 'GUEST') NOT NULL DEFAULT 'GUEST',
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       last_login DATETIME,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       INDEX idx_users_username (username),
                       INDEX idx_users_email (email),
                       INDEX idx_users_role (role),
                       INDEX idx_users_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts and authentication';

-- ==========================================
-- LANGUAGE MANAGEMENT TABLES
-- ==========================================

-- Languages table
CREATE TABLE languages (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           name VARCHAR(100) NOT NULL,
                           native_name VARCHAR(100) NOT NULL,
                           iso_639_1 CHAR(2) NOT NULL UNIQUE,
                           iso_639_2 CHAR(3),
                           locale_code VARCHAR(10), -- allow longer than 5
                           charset VARCHAR(50) DEFAULT 'UTF-8',
                           flag_url VARCHAR(500),
                           flag_path VARCHAR(255),
                           is_rtl BOOLEAN NOT NULL DEFAULT FALSE,
                           is_active BOOLEAN NOT NULL DEFAULT TRUE,
                           is_default BOOLEAN NOT NULL DEFAULT FALSE,
                           is_admin_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                           is_guest_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                           display_order INT NOT NULL DEFAULT 0,
                           font_family VARCHAR(100),
                           currency_code CHAR(3),
                           currency_symbol VARCHAR(10),
                           date_format VARCHAR(50) DEFAULT 'yyyy-MM-dd',
                           time_format VARCHAR(50) DEFAULT 'HH:mm',
                           number_format VARCHAR(50),
                           decimal_separator CHAR(1) DEFAULT '.',
                           thousands_separator CHAR(1) DEFAULT ',',
                           ui_translation_progress INT DEFAULT 0 CHECK (ui_translation_progress BETWEEN 0 AND 100),
                           channel_translation_progress INT DEFAULT 0 CHECK (channel_translation_progress BETWEEN 0 AND 100),
                           epg_translation_enabled BOOLEAN DEFAULT FALSE,
                           welcome_message TEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           created_by VARCHAR(100),
                           last_modified_by VARCHAR(100),
                           PRIMARY KEY (id),
                           INDEX idx_language_iso_639_1 (iso_639_1),
                           INDEX idx_language_iso_639_2 (iso_639_2),
                           INDEX idx_language_active (is_active),
                           INDEX idx_language_default (is_default),
                           INDEX idx_language_admin_enabled (is_admin_enabled),
                           INDEX idx_language_guest_enabled (is_guest_enabled),
                           INDEX idx_language_display_order (display_order),
                           INDEX idx_language_composite (is_active, is_guest_enabled, display_order)
                        -- ❌ Removed CONSTRAINT uk_language_default UNIQUE (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Supported languages for the IPTV platform';

-- Language supported platforms (Many-to-Many)
CREATE TABLE language_supported_platforms (
                                              language_id BIGINT NOT NULL,
                                              platform VARCHAR(50) NOT NULL,
                                              PRIMARY KEY (language_id, platform),
                                              FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                              INDEX idx_platform (platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TV platforms supported by each language';

-- Translations table
CREATE TABLE translations (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              language_id BIGINT NOT NULL,
                              message_key VARCHAR(255) NOT NULL,
                              message_value TEXT NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              created_by VARCHAR(100),
                              updated_by VARCHAR(100),
                              PRIMARY KEY (id),
                              UNIQUE KEY uq_translation_language_key (language_id, message_key),
                              FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE ON UPDATE CASCADE,
                              INDEX idx_translation_language_id (language_id),
                              INDEX idx_translation_message_key (message_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='UI translations for different languages';

-- ==========================================
-- TV CHANNEL CATEGORIES
-- ==========================================

-- TV Channel Categories table
CREATE TABLE tv_channel_categories (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       name VARCHAR(100) NOT NULL UNIQUE,
                                       description TEXT,
                                       icon_url VARCHAR(500),
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (id),
                                       INDEX idx_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TV channel categories (News, Sports, Entertainment, etc.)';

-- ==========================================
-- ROOMS MANAGEMENT
-- ==========================================

-- Rooms table
CREATE TABLE rooms (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       room_number VARCHAR(50) NOT NULL UNIQUE,
                       room_type ENUM('STANDARD', 'DELUXE', 'SUITE', 'JUNIOR_SUITE', 'PRESIDENTIAL_SUITE', 'FAMILY_ROOM', 'SINGLE', 'DOUBLE', 'TWIN') NOT NULL,
                       floor_number INT,
                       building VARCHAR(100),
                       capacity INT,
                       price_per_night DECIMAL(10,2),
                       status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_ORDER', 'CLEANING') NOT NULL DEFAULT 'AVAILABLE',
                       description TEXT,
                       channel_package_id BIGINT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       INDEX idx_room_status (status),
                       INDEX idx_room_type (room_type),
                       INDEX idx_room_floor (floor_number),
                       INDEX idx_room_number (room_number),
                       INDEX idx_room_package (channel_package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hotel rooms information';

-- Room amenities (One-to-Many)
CREATE TABLE room_amenities (
                                room_id BIGINT NOT NULL,
                                amenity VARCHAR(100) NOT NULL,
                                PRIMARY KEY (room_id, amenity),
                                FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Amenities available in each room';

-- ==========================================
-- GUESTS MANAGEMENT
-- ==========================================

-- Guests table
CREATE TABLE guests (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        guest_id VARCHAR(50) NOT NULL UNIQUE,
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        email VARCHAR(100),
                        phone VARCHAR(30),
                        nationality VARCHAR(100),
                        passport_number VARCHAR(50),
                        id_card_number VARCHAR(50),
                        date_of_birth DATE,
                        gender ENUM('MALE', 'FEMALE', 'OTHER'),
                        vip_status BOOLEAN DEFAULT FALSE,
                        loyalty_level ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND'),
                        preferred_language VARCHAR(10),
                        special_requests TEXT,
                        notes TEXT,
                        current_room_id BIGINT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        FOREIGN KEY (current_room_id) REFERENCES rooms(id) ON DELETE SET NULL ON UPDATE CASCADE,
                        INDEX idx_guest_id (guest_id),
                        INDEX idx_guest_email (email),
                        INDEX idx_guest_passport (passport_number),
                        INDEX idx_guest_vip (vip_status),
                        INDEX idx_guest_room (current_room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hotel guests information';

-- Reservations table
CREATE TABLE reservations (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              reservation_number VARCHAR(50) NOT NULL UNIQUE,
                              guest_id BIGINT NOT NULL,
                              room_id BIGINT NOT NULL,
                              check_in_date DATETIME NOT NULL,
                              check_out_date DATETIME NOT NULL,
                              actual_check_in DATETIME,
                              actual_check_out DATETIME,
                              number_of_guests INT,
                              total_amount DECIMAL(10,2),
                              status ENUM('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'CONFIRMED',
                              booking_source VARCHAR(100),
                              special_requests TEXT,
                              notes TEXT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (id),
                              FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE ON UPDATE CASCADE,
                              FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE ON UPDATE CASCADE,
                              INDEX idx_reservation_number (reservation_number),
                              INDEX idx_reservation_guest (guest_id),
                              INDEX idx_reservation_room (room_id),
                              INDEX idx_reservation_status (status),
                              INDEX idx_reservation_dates (check_in_date, check_out_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hotel reservations';

-- ==========================================
-- INITIAL DATA CONSTRAINTS
-- ==========================================

-- Add constraint to ensure only one default language
-- This will be enforced by application logic due to MySQL limitations with conditional unique constraints

-- Add check constraints for valid values
ALTER TABLE languages ADD CONSTRAINT chk_translation_progress_ui
    CHECK (ui_translation_progress >= 0 AND ui_translation_progress <= 100);

ALTER TABLE languages ADD CONSTRAINT chk_translation_progress_channel
    CHECK (channel_translation_progress >= 0 AND channel_translation_progress <= 100);

ALTER TABLE languages ADD CONSTRAINT chk_display_order
    CHECK (display_order >= 0 AND display_order <= 9999);

ALTER TABLE rooms ADD CONSTRAINT chk_room_capacity
    CHECK (capacity > 0);

ALTER TABLE rooms ADD CONSTRAINT chk_room_floor
    CHECK (floor_number >= 0);

ALTER TABLE reservations ADD CONSTRAINT chk_reservation_dates
    CHECK (check_out_date > check_in_date);

ALTER TABLE reservations ADD CONSTRAINT chk_actual_dates
    CHECK (actual_check_out IS NULL OR actual_check_in IS NULL OR actual_check_out >= actual_check_in);