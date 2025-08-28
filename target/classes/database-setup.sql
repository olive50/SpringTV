-- Create Database
CREATE DATABASE IF NOT EXISTS iptv_platform
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE iptv_platform;

-- Create Categories Table
CREATE TABLE IF NOT EXISTS tv_channel_categories (
                                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                     name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Create Languages Table
CREATE TABLE IF NOT EXISTS languages (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(3) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Create TV Channels Table
CREATE TABLE IF NOT EXISTS tv_channels (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           channel_number INT NOT NULL UNIQUE,
                                           name VARCHAR(100) NOT NULL,
    description TEXT,
    ip VARCHAR(15) NOT NULL,
    port INT NOT NULL,
    logo VARCHAR(500),
    category_id BIGINT,
    language_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES tv_channel_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE SET NULL,
    UNIQUE KEY unique_ip_port (ip, port)
    );

-- Create EPG Entries Table
CREATE TABLE IF NOT EXISTS epg_entries (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           channel_id BIGINT NOT NULL,
                                           title VARCHAR(200) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    genre VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE,
    INDEX idx_channel_time (channel_id, start_time, end_time),
    INDEX idx_time_range (start_time, end_time)
    );

-- Insert Sample Data
INSERT INTO tv_channel_categories (name, description, icon_url) VALUES
                                                                    ('News', 'News and current affairs channels', 'https://example.com/icons/news.png'),
                                                                    ('Sports', 'Sports and athletics channels', 'https://example.com/icons/sports.png'),
                                                                    ('Entertainment', 'Movies and entertainment channels', 'https://example.com/icons/entertainment.png'),
                                                                    ('Kids', 'Children and family channels', 'https://example.com/icons/kids.png'),
                                                                    ('Documentary', 'Documentary and educational channels', 'https://example.com/icons/documentary.png');

INSERT INTO languages (name, code) VALUES
                                       ('English', 'ENG'),
                                       ('French', 'FRA'),
                                       ('Arabic', 'ARA'),
                                       ('Spanish', 'SPA'),
                                       ('German', 'GER');

INSERT INTO tv_channels (channel_number, name, description, ip, port, logo, category_id, language_id) VALUES
                                                                                                          (101, 'CNN International', 'International news channel', '192.168.1.100', 8001, 'https://example.com/logos/cnn.png', 1, 1),
                                                                                                          (102, 'BBC World News', 'British news channel', '192.168.1.100', 8002, 'https://example.com/logos/bbc.png', 1, 1),
                                                                                                          (201, 'ESPN', 'Sports entertainment channel', '192.168.1.101', 8001, 'https://example.com/logos/espn.png', 2, 1),
                                                                                                          (301, 'HBO', 'Premium entertainment channel', '192.168.1.102', 8001, 'https://example.com/logos/hbo.png', 3, 1),
                                                                                                          (401, 'Cartoon Network', 'Kids cartoon channel', '192.168.1.103', 8001, 'https://example.com/logos/cartoon.png', 4, 1);


-- Extended Database Schema for Hotel IPTV Platform

-- Rooms Table
CREATE TABLE IF NOT EXISTS rooms (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     room_number VARCHAR(20) NOT NULL UNIQUE,
    room_type ENUM('STANDARD', 'DELUXE', 'SUITE', 'PRESIDENTIAL_SUITE', 'FAMILY_ROOM', 'SINGLE', 'DOUBLE', 'TWIN') NOT NULL,
    floor_number INT,
    building VARCHAR(50),
    max_occupancy INT DEFAULT 2,
    price_per_night DECIMAL(10,2),
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_ORDER', 'CLEANING') NOT NULL DEFAULT 'AVAILABLE',
    description TEXT,
    amenities TEXT,
    channel_package_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_package_id) REFERENCES channel_packages(id) ON DELETE SET NULL
    );

-- Terminals Table
CREATE TABLE IF NOT EXISTS terminals (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         terminal_id VARCHAR(50) NOT NULL UNIQUE,
    device_type ENUM('SET_TOP_BOX', 'SMART_TV', 'DESKTOP_PC', 'TABLET', 'MOBILE', 'DISPLAY_SCREEN', 'PROJECTOR') NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(100),
    mac_address VARCHAR(17) UNIQUE,
    ip_address VARCHAR(15),
    firmware_version VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'OFFLINE', 'FAULTY') NOT NULL DEFAULT 'ACTIVE',
    location VARCHAR(100),
    serial_number VARCHAR(100) UNIQUE,
    purchase_date DATETIME,
    warranty_expiry DATETIME,
    last_seen DATETIME,
    notes TEXT,
    room_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
    );

-- Guests Table
CREATE TABLE IF NOT EXISTS guests (
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (current_room_id) REFERENCES rooms(id) ON DELETE SET NULL
    );

-- Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            reservation_number VARCHAR(50) NOT NULL UNIQUE,
    guest_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATETIME NOT NULL,
    check_out_date DATETIME NOT NULL,
    actual_check_in DATETIME,
    actual_check_out DATETIME,
    number_of_guests INT DEFAULT 1,
    total_amount DECIMAL(10,2),
    status ENUM('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'CONFIRMED',
    booking_source VARCHAR(50),
    special_requests TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
    );

-- Channel Packages Table
CREATE TABLE IF NOT EXISTS channel_packages (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(10,2),
    is_premium BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Package Channels Junction Table
CREATE TABLE IF NOT EXISTS package_channels (
                                                package_id BIGINT NOT NULL,
                                                channel_id BIGINT NOT NULL,
                                                PRIMARY KEY (package_id, channel_id),
    FOREIGN KEY (package_id) REFERENCES channel_packages(id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE
    );

-- Terminal Channel Assignments Table
CREATE TABLE IF NOT EXISTS terminal_channel_assignments (
                                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                            terminal_id BIGINT NOT NULL,
                                                            channel_id BIGINT NOT NULL,
                                                            position INT,
                                                            is_enabled BOOLEAN DEFAULT TRUE,
                                                            assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                            FOREIGN KEY (terminal_id) REFERENCES terminals(id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES tv_channels(id) ON DELETE CASCADE,
    UNIQUE KEY unique_terminal_position (terminal_id, position)
    );

-- Insert Sample Data
INSERT INTO channel_packages (name, description, price, is_premium) VALUES
                                                                        ('Basic Package', 'Standard channels for all rooms', 0.00, FALSE),
                                                                        ('Premium Package', 'Extended channels with international content', 15.00, TRUE),
                                                                        ('VIP Package', 'All channels including premium movies and sports', 25.00, TRUE);

INSERT INTO rooms (room_number, room_type, floor_number, building, max_occupancy, price_per_night, status, description) VALUES
                                                                                                                            ('101', 'STANDARD', 1, 'Main Building', 2, 89.99, 'AVAILABLE', 'Standard room with city view'),
                                                                                                                            ('102', 'STANDARD', 1, 'Main Building', 2, 89.99, 'AVAILABLE', 'Standard room with garden view'),
                                                                                                                            ('201', 'DELUXE', 2, 'Main Building', 3, 129.99, 'AVAILABLE', 'Deluxe room with balcony'),
                                                                                                                            ('301', 'SUITE', 3, 'Main Building', 4, 199.99, 'AVAILABLE', 'Executive suite with living room'),
                                                                                                                            ('401', 'PRESIDENTIAL_SUITE', 4, 'Main Building', 6, 399.99, 'AVAILABLE', 'Presidential suite with panoramic view');

INSERT INTO guests (guest_id, first_name, last_name, email, phone, nationality, vip_status, loyalty_level) VALUES
                                                                                                               ('G12345678', 'Ahmed', 'Ben Ali', 'ahmed.benali@email.com', '+213555123456', 'Algeria', FALSE, 'BRONZE'),
                                                                                                               ('G87654321', 'Fatima', 'Zohra', 'fatima.zohra@email.com', '+213666789012', 'Algeria', TRUE, 'GOLD'),
                                                                                                               ('G11223344', 'Mohamed', 'Cherif', 'mohamed.cherif@email.com', '+213777456789', 'Algeria', FALSE, 'SILVER');

INSERT INTO terminals (terminal_id, device_type, brand, model, mac_address, ip_address, status, location, room_id) VALUES
                                                                                                                       ('STB001', 'SET_TOP_BOX', 'Samsung', 'SMT-C7140', '00:1A:2B:3C:4D:5E', '192.168.1.101', 'ACTIVE', 'Room 101', 1),
                                                                                                                       ('STB002', 'SET_TOP_BOX', 'LG', 'ST600S', '00:1A:2B:3C:4D:5F', '192.168.1.102', 'ACTIVE', 'Room 102', 2),
                                                                                                                       ('TV001', 'SMART_TV', 'Sony', 'KD-55X80K', '00:1A:2B:3C:4D:60', '192.168.1.201', 'ACTIVE', 'Room 201', 3);