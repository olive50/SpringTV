-- ===================================================================
-- TVBOOT IPTV Platform - TV Channels and Terminals Schema
-- Migration: V002__Create_tv_and_terminal_tables.sql
-- Description: Create TV channels, terminals, and IPTV-related tables
-- Author: TVBOOT Team
-- Date: 2025-01-20
-- ===================================================================

-- ==========================================
-- TV CHANNELS MANAGEMENT
-- ==========================================

-- TV Channels table
CREATE TABLE tv_channels (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             channel_number INT NOT NULL UNIQUE,
                             name VARCHAR(200) NOT NULL,
                             description TEXT,
                             ip VARCHAR(15),
                             port INT,
                             stream_url VARCHAR(500) NOT NULL,
                             logo_url VARCHAR(500),
                             logo_path VARCHAR(255),
                             category_id BIGINT,
                             language_id BIGINT,
                             is_active BOOLEAN NOT NULL DEFAULT TRUE,
                             is_hd BOOLEAN DEFAULT FALSE,
                             is_avialable BOOLEAN DEFAULT TRUE,
                             sort_order INT NOT NULL DEFAULT 0,
                             comment TEXT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             FOREIGN KEY (category_id) REFERENCES tv_channel_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
                             FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE SET NULL ON UPDATE CASCADE,
                             INDEX idx_channel_number (channel_number),
                             INDEX idx_channel_name (name),
                             INDEX idx_channel_active (is_active),
                             INDEX idx_channel_category (category_id),
                             INDEX idx_channel_language (language_id),
                             INDEX idx_channel_sort (sort_order),
                             INDEX idx_channel_ip_port (ip, port),
                             CONSTRAINT chk_channel_number CHECK (channel_number > 0),
                             CONSTRAINT chk_channel_port CHECK (port IS NULL OR (port > 0 AND port <= 65535))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TV channels available in the IPTV system';

-- ==========================================
-- ELECTRONIC PROGRAM GUIDE (EPG)
-- ==========================================

-- EPG Entries table
CREATE TABLE epg_entries (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             channel_id BIGINT NOT NULL,
                             title VARCHAR(300) NOT NULL,
                             description TEXT,
                             start_time DATETIME NOT NULL,
                             end_time DATETIME NOT NULL,
                             genre VARCHAR(100),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE ON UPDATE CASCADE,
                             INDEX idx_epg_channel (channel_id),
                             INDEX idx_epg_start_time (start_time),
                             INDEX idx_epg_end_time (end_time),
                             INDEX idx_epg_time_range (channel_id, start_time, end_time),
                             INDEX idx_epg_genre (genre),
                             CONSTRAINT chk_epg_times CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Electronic Program Guide entries for TV channels';

-- ==========================================
-- CHANNEL PACKAGES
-- ==========================================

-- Channel Packages table
CREATE TABLE channel_packages (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  name VARCHAR(100) NOT NULL UNIQUE,
                                  description TEXT,
                                  price DECIMAL(10,2),
                                  is_premium BOOLEAN DEFAULT FALSE,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (id),
                                  INDEX idx_package_name (name),
                                  INDEX idx_package_premium (is_premium),
                                  INDEX idx_package_active (is_active),
                                  CONSTRAINT chk_package_price CHECK (price IS NULL OR price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Channel packages for grouping TV channels';

-- Package Channels (Many-to-Many relationship)
CREATE TABLE package_channels (
                                  package_id BIGINT NOT NULL,
                                  channel_id BIGINT NOT NULL,
                                  position INT,
                                  is_enabled BOOLEAN DEFAULT TRUE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (package_id, channel_id),
                                  FOREIGN KEY (package_id) REFERENCES channel_packages(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                  FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                  INDEX idx_package_channels_position (package_id, position),
                                  INDEX idx_package_channels_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Channels included in each package';

-- ==========================================
-- TERMINALS/DEVICES MANAGEMENT
-- ==========================================
-- Terminals table
CREATE TABLE terminals (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           terminal_id VARCHAR(50) NOT NULL UNIQUE,
                           device_type ENUM(
                               'SMART_TV',
                               'ANDROID_BOX',
                               'STREAMING_STICK',
                               'SET_TOP_BOX',
                               'MEDIA_PLAYER',
                               'PROJECTOR'
                               ) NOT NULL,
                           brand VARCHAR(50) NOT NULL,
                           model VARCHAR(50) NOT NULL,
                           mac_address VARCHAR(17) NOT NULL UNIQUE,
                           ip_address VARCHAR(45) NOT NULL, -- supports IPv4, IPv6, and hostnames if needed
                           status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'OFFLINE', 'FAULTY')
                               NOT NULL DEFAULT 'INACTIVE',
                           location VARCHAR(100) NOT NULL,
                           room_id BIGINT,
                           last_seen DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           firmware_version VARCHAR(20),
                           serial_number VARCHAR(50),
                           response_time INT,
                           uptime FLOAT(17),
                           last_ping_time DATETIME,
                           is_online BOOLEAN NOT NULL DEFAULT FALSE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           FOREIGN KEY (room_id) REFERENCES rooms(id)
                               ON DELETE SET NULL
                               ON UPDATE CASCADE,

    -- indexes
                           INDEX idx_terminal_id (terminal_id),
                           INDEX idx_terminal_mac (mac_address),
                           INDEX idx_terminal_ip (ip_address),
                           INDEX idx_terminal_status (status),
                           INDEX idx_terminal_device_type (device_type),
                           INDEX idx_terminal_room (room_id),
                           INDEX idx_terminal_online (is_online),
                           INDEX idx_terminal_last_seen (last_seen),

    -- constraints
                           CONSTRAINT chk_terminal_uptime CHECK (
                               uptime IS NULL OR (uptime >= 0 AND uptime <= 100)
                               ),
                           CONSTRAINT chk_terminal_response_time CHECK (
                               response_time IS NULL OR response_time >= 0
                               )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='IPTV terminals and devices';

-- Terminal Channel Assignments table
CREATE TABLE terminal_channel_assignments (
                                              id BIGINT NOT NULL AUTO_INCREMENT,
                                              terminal_id BIGINT NOT NULL,
                                              channel_id BIGINT NOT NULL,
                                              position INT,
                                              is_enabled BOOLEAN DEFAULT TRUE,
                                              assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              PRIMARY KEY (id),
                                              FOREIGN KEY (terminal_id) REFERENCES terminals(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                              FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                              UNIQUE KEY uk_terminal_channel (terminal_id, channel_id),
                                              INDEX idx_terminal_assignment_terminal (terminal_id),
                                              INDEX idx_terminal_assignment_channel (channel_id),
                                              INDEX idx_terminal_assignment_position (terminal_id, position),
                                              INDEX idx_terminal_assignment_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Channel assignments for specific terminals';

-- ==========================================
-- ADD FOREIGN KEY CONSTRAINTS FOR ROOMS
-- ==========================================

-- Add foreign key constraint for channel_package_id in rooms table
ALTER TABLE rooms
    ADD CONSTRAINT fk_rooms_channel_package
        FOREIGN KEY (channel_package_id) REFERENCES channel_packages(id) ON DELETE SET NULL ON UPDATE CASCADE;

-- ==========================================
-- ADDITIONAL INDEXES FOR PERFORMANCE
-- ==========================================

-- Composite indexes for common queries
CREATE INDEX idx_tv_channels_active_category ON tv_channels(is_active, category_id);
CREATE INDEX idx_tv_channels_active_language ON tv_channels(is_active, language_id);
CREATE INDEX idx_tv_channels_active_sort ON tv_channels(is_active, sort_order);
CREATE INDEX idx_terminals_status_type ON terminals(status, device_type);

-- Indexes for EPG queries
CREATE INDEX idx_epg_current_program ON epg_entries(channel_id, start_time, end_time);
CREATE INDEX idx_epg_today_programs ON epg_entries(start_time, end_time);

-- ==========================================
-- ADDITIONAL CONSTRAINTS
-- ==========================================

-- Ensure MAC address format (basic validation)
ALTER TABLE terminals ADD CONSTRAINT chk_mac_address_format
    CHECK (mac_address REGEXP '^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$');

-- Ensure EPG entries don't overlap for the same channel (to be enforced by application logic)
-- Note: MySQL doesn't support exclusion constraints, so this will be handled in application code