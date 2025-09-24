-- ===================================================================
-- TVBOOT IPTV Platform - Initial Data Setup
-- Migration: V003__Insert_initial_data.sql
-- Description: Insert initial required data (users, languages, categories)
-- Author: TVBOOT Team
-- Date: 2025-01-20
-- ===================================================================

-- ==========================================
-- INSERT DEFAULT USERS
-- ==========================================

-- Insert default admin user (password: admin123 - BCrypt encoded)
INSERT INTO users (username, email, password, first_name, last_name, role, is_active) VALUES
                                                                                          ('admin', 'admin@tvboot.com', '$2a$12$t8MkCikXiXhmgMnZKaS27ue3jcoGV9WdprT5K3P3yXWd5DYUWq6Hu', 'System', 'Administrator', 'ADMIN', TRUE),
                                                                                          ('manager', 'manager@tvboot.com', '$2a$12$LQv3c1yqBwEH8QA7K9lI/eOqjzTfT1Cc/Gml7mAyuL.FBIyF8xkfi', 'Hotel', 'Manager', 'MANAGER', TRUE),
                                                                                          ('receptionist', 'receptionist@tvboot.com', '$2a$12$LQv3c1yqBwEH8QA7K9lI/eOqjzTfT1Cc/Gml7mAyuL.FBIyF8xkfi', 'Front', 'Desk', 'RECEPTIONIST', TRUE),
                                                                                          ('technician', 'technician@tvboot.com', '$2a$12$LQv3c1yqBwEH8QA7K9lI/eOqjzTfT1Cc/Gml7mAyuL.FBIyF8xkfi', 'IT', 'Technician', 'TECHNICIAN', TRUE),
                                                                                          ('housekeeper', 'housekeeper@tvboot.com', '$2y$12$JPxGDa3h9g2E8pL07hzQherxcalC8BOEhYz4e1g2UXALwBHSXfU2G', 'HK', 'Housekeeper', 'HOUSEKEEPER', TRUE);
-- ==========================================
-- INSERT DEFAULT LANGUAGES
-- ==========================================

-- Insert default languages with comprehensive settings
INSERT INTO languages
(name, native_name, iso_639_1, iso_639_2, locale_code, is_rtl, is_active, is_default, display_order, currency_code, currency_symbol, date_format, time_format, decimal_separator, thousands_separator, ui_translation_progress, channel_translation_progress, welcome_message, created_by, last_modified_by)
VALUES
    ('English', 'English', 'en', 'eng', 'en-US', FALSE, TRUE, TRUE, 1, 'USD', '$', 'yyyy-MM-dd', 'HH:mm', '.', ',', 100, 100, 'Welcome', 'system', 'system'),

    ('French', 'Français', 'fr', 'fra', 'fr-FR', FALSE, TRUE, FALSE, 2, 'EUR', '€', 'dd/MM/yyyy', 'HH:mm', ',', ' ', 95, 80, 'Bienvenue', 'system', 'system'),

    ('Spanish', 'Español', 'es', 'spa', 'es-ES', FALSE, TRUE, FALSE, 3, 'EUR', '€', 'dd/MM/yyyy', 'HH:mm', ',', '.', 90, 75, 'Bienvenido', 'system', 'system'),

    ('German', 'Deutsch', 'de', 'deu', 'de-DE', FALSE, TRUE, FALSE, 4, 'EUR', '€', 'dd.MM.yyyy', 'HH:mm', ',', '.', 85, 70, 'Willkommen', 'system', 'system'),

    ('Italian', 'Italiano', 'it', 'ita', 'it-IT', FALSE, TRUE, FALSE, 5, 'EUR', '€', 'dd/MM/yyyy', 'HH:mm', ',', '.', 80, 65, 'Benvenuto', 'system', 'system'),

    ('Portuguese', 'Português', 'pt', 'por', 'pt-PT', FALSE, TRUE, FALSE, 6, 'EUR', '€', 'dd/MM/yyyy', 'HH:mm', ',', '.', 85, 60, 'Bem-vindo', 'system', 'system'),

    ('Arabic', 'العربية', 'ar', 'ara', 'ar-SA', TRUE, TRUE, FALSE, 7, 'SAR', '﷼', 'dd/MM/yyyy', 'HH:mm', '.', ',', 70, 50, 'مرحبا', 'system', 'system'),

    ('Russian', 'Русский', 'ru', 'rus', 'ru-RU', FALSE, TRUE, FALSE, 8, 'RUB', '₽', 'dd.MM.yyyy', 'HH:mm', ',', ' ', 75, 55, 'Добро пожаловать', 'system', 'system'),

    ('Chinese (Simplified)', '简体中文', 'zh', 'zho', 'zh-CN', FALSE, TRUE, FALSE, 9, 'CNY', '¥', 'yyyy/MM/dd', 'HH:mm', '.', ',', 60, 40, '欢迎', 'system', 'system'),

    ('Japanese', '日本語', 'ja', 'jpn', 'ja-JP', FALSE, TRUE, FALSE, 10, 'JPY', '¥', 'yyyy/MM/dd', 'HH:mm', '.', ',', 65, 45, 'ようこそ', 'system', 'system');


-- Insert supported platforms for languages
INSERT INTO language_supported_platforms (language_id, platform) VALUES
-- English - all platforms
(1, 'TIZEN'), (1, 'WEBOS'), (1, 'ANDROID'), (1, 'WEB'), (1, 'IOS'),
-- French - all platforms
(2, 'TIZEN'), (2, 'WEBOS'), (2, 'ANDROID'), (2, 'WEB'), (2, 'IOS'),
-- Arabic - Tizen and WebOS (better RTL support)
(3, 'TIZEN'), (3, 'WEBOS'), (3, 'WEB'),
-- Spanish - all platforms
(4, 'TIZEN'), (4, 'WEBOS'), (4, 'ANDROID'), (4, 'WEB'), (4, 'IOS'),
-- German - all platforms
(5, 'TIZEN'), (5, 'WEBOS'), (5, 'ANDROID'), (5, 'WEB'), (5, 'IOS'),
-- Italian - all platforms
(6, 'TIZEN'), (6, 'WEBOS'), (6, 'ANDROID'), (6, 'WEB'), (6, 'IOS');

-- ==========================================
-- INSERT DEFAULT TV CHANNEL CATEGORIES
-- ==========================================

INSERT INTO tv_channel_categories (name, description, icon_url) VALUES
                                                                    ('News', 'News and current affairs channels', 'fas fa-newspaper'),
                                                                    ('Sports', 'Sports and athletics channels', 'fas fa-football-ball'),
                                                                    ('Entertainment', 'Movies and entertainment channels', 'fas fa-film'),
                                                                    ('Kids', 'Children and family channels', 'fas fa-child'),
                                                                    ('Documentary', 'Documentary and educational channels', 'fas fa-graduation-cap'),
                                                                    ('Music', 'Music and concerts channels', 'fas fa-music'),
                                                                    ('Lifestyle', 'Lifestyle and cooking channels', 'fas fa-utensils'),
                                                                    ('Religious', 'Religious and spiritual content', 'fas fa-pray'),
                                                                    ('International', 'International and foreign language channels', 'fas fa-globe'),
                                                                    ('Premium', 'Premium subscription channels', 'fas fa-crown');

-- ==========================================
-- INSERT SAMPLE CHANNEL PACKAGES
-- ==========================================

INSERT INTO channel_packages (name, description, price, is_premium, is_active) VALUES
                                                                                   ('Basic Package', 'Essential channels for all guests', 0.00, FALSE, TRUE),
                                                                                   ('Premium Package', 'Extended channel selection with premium content', 15.99, TRUE, TRUE),
                                                                                   ('Sports Package', 'Comprehensive sports coverage', 12.99, TRUE, TRUE),
                                                                                   ('International Package', 'Channels from around the world', 9.99, TRUE, TRUE),
                                                                                   ('Family Package', 'Family-friendly entertainment and kids channels', 8.99, FALSE, TRUE);

-- ==========================================
-- INSERT SAMPLE TV CHANNELS
-- ==========================================

-- Get language IDs for references
SET @english_id = (SELECT id FROM languages WHERE iso_639_1 = 'en');
SET @french_id = (SELECT id FROM languages WHERE iso_639_1 = 'fr');
SET @arabic_id = (SELECT id FROM languages WHERE iso_639_1 = 'ar');

-- Get category IDs for references
SET @news_cat = (SELECT id FROM tv_channel_categories WHERE name = 'News');
SET @sports_cat = (SELECT id FROM tv_channel_categories WHERE name = 'Sports');
SET @entertainment_cat = (SELECT id FROM tv_channel_categories WHERE name = 'Entertainment');
SET @kids_cat = (SELECT id FROM tv_channel_categories WHERE name = 'Kids');
SET @documentary_cat = (SELECT id FROM tv_channel_categories WHERE name = 'Documentary');
SET @music_cat = (SELECT id FROM tv_channel_categories WHERE name = 'Music');

-- Insert sample TV channels
INSERT INTO tv_channels (
    channel_number, name, description, ip, port, stream_url, logo_url,
    category_id, language_id, is_active, is_hd, is_avialable, sort_order
) VALUES
-- News Channels
(101, 'CNN International', 'International news and current affairs', '192.168.1.100', 8001, 'udp://@239.1.1.101:1234', '/logos/cnn.png', @news_cat, @english_id, TRUE, TRUE, TRUE, 1),
(102, 'BBC World News', 'British Broadcasting Corporation World Service', '192.168.1.101', 8002, 'udp://@239.1.1.102:1234', '/logos/bbc.png', @news_cat, @english_id, TRUE, TRUE, TRUE, 2),
(103, 'France 24', 'French international news channel', '192.168.1.102', 8003, 'udp://@239.1.1.103:1234', '/logos/france24.png', @news_cat, @french_id, TRUE, TRUE, TRUE, 3),
(104, 'Al Jazeera English', 'Middle Eastern perspective on global news', '192.168.1.103', 8004, 'udp://@239.1.1.104:1234', '/logos/aljazeera.png', @news_cat, @english_id, TRUE, TRUE, TRUE, 4),
(105, 'Echorouk News', 'Algerian news channel', '192.168.1.104', 8005, 'udp://@239.1.1.105:1234', '/logos/echorouk.png', @news_cat, @arabic_id, TRUE, FALSE, TRUE, 5),

-- Sports Channels
(201, 'ESPN', 'Sports entertainment and programming network', '192.168.1.110', 8011, 'udp://@239.1.2.101:1234', '/logos/espn.png', @sports_cat, @english_id, TRUE, TRUE, TRUE, 10),
(202, 'Eurosport 1', 'European sports channel', '192.168.1.111', 8012, 'udp://@239.1.2.102:1234', '/logos/eurosport.png', @sports_cat, @english_id, TRUE, TRUE, TRUE, 11),
(203, 'beIN Sports 1', 'Premium sports channel', '192.168.1.112', 8013, 'udp://@239.1.2.103:1234', '/logos/bein1.png', @sports_cat, @english_id, TRUE, TRUE, TRUE, 12),
(204, 'Sky Sports', 'UK sports broadcasting', '192.168.1.113', 8014, 'udp://@239.1.2.104:1234', '/logos/skysports.png', @sports_cat, @english_id, TRUE, TRUE, TRUE, 13),

-- Entertainment Channels
(301, 'HBO', 'Premium entertainment channel', '192.168.1.120', 8021, 'udp://@239.1.3.101:1234', '/logos/hbo.png', @entertainment_cat, @english_id, TRUE, TRUE, TRUE, 20),
(302, 'Discovery Channel', 'Documentary and reality programming', '192.168.1.121', 8022, 'udp://@239.1.3.102:1234', '/logos/discovery.png', @documentary_cat, @english_id, TRUE, TRUE, TRUE, 21),
(303, 'National Geographic', 'Science and nature documentaries', '192.168.1.122', 8023, 'udp://@239.1.3.103:1234', '/logos/natgeo.png', @documentary_cat, @english_id, TRUE, TRUE, TRUE, 22),
(304, 'MTV', 'Music television', '192.168.1.123', 8024, 'udp://@239.1.3.104:1234', '/logos/mtv.png', @music_cat, @english_id, TRUE, FALSE, TRUE, 23),

-- Kids Channels
(401, 'Cartoon Network', 'Animated series and movies for children', '192.168.1.130', 8031, 'udp://@239.1.4.101:1234', '/logos/cartoon.png', @kids_cat, @english_id, TRUE, FALSE, TRUE, 30),
(402, 'Disney Channel', 'Family entertainment from Disney', '192.168.1.131', 8032, 'udp://@239.1.4.102:1234', '/logos/disney.png', @kids_cat, @english_id, TRUE, TRUE, TRUE, 31),
(403, 'Nickelodeon', 'Kids entertainment and educational content', '192.168.1.132', 8033, 'udp://@239.1.4.103:1234', '/logos/nick.png', @kids_cat, @english_id, TRUE, FALSE, TRUE, 32),

-- French Channels
(501, 'TF1', 'French general entertainment', '192.168.1.140', 8041, 'udp://@239.1.5.101:1234', '/logos/tf1.png', @entertainment_cat, @french_id, TRUE, TRUE, TRUE, 40),
(502, 'France 2', 'French public television', '192.168.1.141', 8042, 'udp://@239.1.5.102:1234', '/logos/france2.png', @entertainment_cat, @french_id, TRUE, TRUE, TRUE, 41),
(503, 'Canal+', 'French premium channel', '192.168.1.142', 8043, 'udp://@239.1.5.103:1234', '/logos/canal.png', @entertainment_cat, @french_id, TRUE, TRUE, TRUE, 42);

-- ==========================================
-- INSERT SAMPLE ROOMS
-- ==========================================

INSERT INTO rooms (room_number, room_type, floor_number, building, capacity, price_per_night, status, description) VALUES
-- Standard Rooms (Floor 1)
('101', 'STANDARD', 1, 'Main Building', 2, 89.99, 'AVAILABLE', 'Standard room with city view and basic amenities'),
('102', 'STANDARD', 1, 'Main Building', 2, 89.99, 'OCCUPIED', 'Standard room with garden view'),
('103', 'STANDARD', 1, 'Main Building', 2, 89.99, 'AVAILABLE', 'Standard room with courtyard view'),
('104', 'STANDARD', 1, 'Main Building', 2, 89.99, 'CLEANING', 'Standard room with pool view'),
('105', 'STANDARD', 1, 'Main Building', 2, 89.99, 'AVAILABLE', 'Standard room with city view'),

-- Deluxe Rooms (Floor 2)
('201', 'DELUXE', 2, 'Main Building', 3, 129.99, 'AVAILABLE', 'Deluxe room with balcony and enhanced amenities'),
('202', 'DELUXE', 2, 'Main Building', 3, 129.99, 'OCCUPIED', 'Deluxe room with sea view'),
('203', 'DELUXE', 2, 'Main Building', 3, 129.99, 'AVAILABLE', 'Deluxe room with mountain view'),
('204', 'DELUXE', 2, 'Main Building', 3, 129.99, 'AVAILABLE', 'Deluxe room with garden balcony'),
('205', 'DELUXE', 2, 'Main Building', 3, 129.99, 'MAINTENANCE', 'Deluxe room with panoramic view'),

-- Suites (Floor 3)
('301', 'SUITE', 3, 'Main Building', 4, 199.99, 'AVAILABLE', 'Executive suite with living room and work area'),
('302', 'SUITE', 3, 'Main Building', 4, 199.99, 'OCCUPIED', 'Luxury suite with separate dining area'),
('303', 'JUNIOR_SUITE', 3, 'Main Building', 3, 169.99, 'AVAILABLE', 'Junior suite with sitting area'),
('304', 'PRESIDENTIAL_SUITE', 3, 'Main Building', 6, 399.99, 'AVAILABLE', 'Presidential suite with multiple rooms and premium amenities'),

-- Wing B Rooms
('B101', 'STANDARD', 1, 'Wing B', 2, 79.99, 'AVAILABLE', 'Standard room in quiet wing'),
('B102', 'STANDARD', 1, 'Wing B', 2, 79.99, 'AVAILABLE', 'Standard room with garden access'),
('B201', 'DELUXE', 2, 'Wing B', 3, 119.99, 'AVAILABLE', 'Deluxe room with terrace'),
('B202', 'FAMILY_ROOM', 2, 'Wing B', 5, 149.99, 'AVAILABLE', 'Family room with connecting door option');

-- Insert room amenities
INSERT INTO room_amenities (room_id, amenity) VALUES
-- Standard room amenities
(1, 'WiFi'), (1, 'Air Conditioning'), (1, 'TV'), (1, 'Mini Bar'), (1, 'Safe'),
(2, 'WiFi'), (2, 'Air Conditioning'), (2, 'TV'), (2, 'Mini Bar'), (2, 'Safe'),
(3, 'WiFi'), (3, 'Air Conditioning'), (3, 'TV'), (3, 'Mini Bar'), (3, 'Safe'),
(4, 'WiFi'), (4, 'Air Conditioning'), (4, 'TV'), (4, 'Mini Bar'), (4, 'Safe'),
(5, 'WiFi'), (5, 'Air Conditioning'), (5, 'TV'), (5, 'Mini Bar'), (5, 'Safe'),

-- Deluxe room amenities (enhanced)
(6, 'WiFi'), (6, 'Air Conditioning'), (6, 'Smart TV'), (6, 'Mini Bar'), (6, 'Safe'), (6, 'Balcony'), (6, 'Coffee Machine'),
(7, 'WiFi'), (7, 'Air Conditioning'), (7, 'Smart TV'), (7, 'Mini Bar'), (7, 'Safe'), (7, 'Balcony'), (7, 'Coffee Machine'),
(8, 'WiFi'), (8, 'Air Conditioning'), (8, 'Smart TV'), (8, 'Mini Bar'), (8, 'Safe'), (8, 'Balcony'), (8, 'Coffee Machine'),
(9, 'WiFi'), (9, 'Air Conditioning'), (9, 'Smart TV'), (9, 'Mini Bar'), (9, 'Safe'), (9, 'Balcony'), (9, 'Coffee Machine'),
(10, 'WiFi'), (10, 'Air Conditioning'), (10, 'Smart TV'), (10, 'Mini Bar'), (10, 'Safe'), (10, 'Balcony'), (10, 'Coffee Machine'),

-- Suite amenities (premium)
(11, 'WiFi'), (11, 'Air Conditioning'), (11, 'Smart TV'), (11, 'Mini Bar'), (11, 'Safe'), (11, 'Living Room'), (11, 'Coffee Machine'), (11, 'Kitchenette'),
(12, 'WiFi'), (12, 'Air Conditioning'), (12, 'Smart TV'), (12, 'Mini Bar'), (12, 'Safe'), (12, 'Living Room'), (12, 'Coffee Machine'), (12, 'Dining Area'),
(13, 'WiFi'), (13, 'Air Conditioning'), (13, 'Smart TV'), (13, 'Mini Bar'), (13, 'Safe'), (13, 'Sitting Area'), (13, 'Coffee Machine'),
(14, 'WiFi'), (14, 'Air Conditioning'), (14, 'Smart TV'), (14, 'Mini Bar'), (14, 'Safe'), (14, 'Living Room'), (14, 'Dining Area'), (14, 'Kitchenette'), (14, 'Jacuzzi'), (14, 'Butler Service');

-- ==========================================
-- INSERT SAMPLE GUESTS
-- ==========================================

INSERT INTO guests (guest_id, first_name, last_name, email, phone, nationality, vip_status, loyalty_level, preferred_language, current_room_id) VALUES
                                                                                                                                                    ('G001', 'John', 'Smith', 'john.smith@email.com', '+1-555-0123', 'American', FALSE, 'SILVER', 'en', 2),
                                                                                                                                                    ('G002', 'Marie', 'Dubois', 'marie.dubois@email.fr', '+33-1-23456789', 'French', TRUE, 'GOLD', 'fr', 7),
                                                                                                                                                    ('G003', 'Ahmed', 'Hassan', 'ahmed.hassan@email.com', '+20-123-456789', 'Egyptian', FALSE, 'BRONZE', 'ar', 12),
                                                                                                                                                    ('G004', 'Anna', 'Schmidt', 'anna.schmidt@email.de', '+49-30-12345678', 'German', FALSE, 'SILVER', 'de', NULL),
                                                                                                                                                    ('G005', 'Carlos', 'Rodriguez', 'carlos.rodriguez@email.es', '+34-91-1234567', 'Spanish', TRUE, 'PLATINUM', 'es', NULL);

-- ==========================================
-- INSERT SAMPLE RESERVATIONS
-- ==========================================

INSERT INTO reservations (
    reservation_number, guest_id, room_id, check_in_date, check_out_date,
    actual_check_in, number_of_guests, total_amount, status, booking_source
) VALUES
      ('RES001', 1, 2, '2025-01-20 15:00:00', '2025-01-25 11:00:00', '2025-01-20 15:30:00', 2, 449.95, 'CHECKED_IN', 'Website'),
      ('RES002', 2, 7, '2025-01-19 14:00:00', '2025-01-24 12:00:00', '2025-01-19 14:15:00', 2, 649.95, 'CHECKED_IN', 'Phone'),
      ('RES003', 3, 12, '2025-01-18 16:00:00', '2025-01-22 10:00:00', '2025-01-18 16:20:00', 3, 799.96, 'CHECKED_IN', 'Travel Agent'),
      ('RES004', 4, 8, '2025-01-22 15:00:00', '2025-01-26 11:00:00', NULL, 2, 519.96, 'CONFIRMED', 'Website'),
      ('RES005', 5, 14, '2025-01-25 14:00:00', '2025-01-30 12:00:00', NULL, 4, 1999.95, 'CONFIRMED', 'Concierge');

-- ==========================================
-- INSERT SAMPLE TERMINALS
-- ==========================================

INSERT INTO terminals (
    terminal_id, device_type, brand, model, mac_address, ip_address, status,
    location, room_id, firmware_version, serial_number, is_online
) VALUES
      ('TV101', 'SMART_TV', 'Samsung', 'QN55Q80A', '00:1B:44:11:3A:B7', '192.168.2.101', 'ACTIVE', 'Room 101', 1, 'Tizen 6.5', 'SN001TV101', TRUE),
      ('TV102', 'SMART_TV', 'Samsung', 'QN55Q80A', '00:1B:44:11:3A:B8', '192.168.2.102', 'ACTIVE', 'Room 102', 2, 'Tizen 6.5', 'SN002TV102', TRUE),
      ('TV201', 'SMART_TV', 'LG', 'OLED55C1PUB', '00:1B:44:11:3A:C1', '192.168.2.201', 'ACTIVE', 'Room 201', 6, 'WebOS 6.0', 'SN003TV201', TRUE),
      ('TV202', 'SMART_TV', 'LG', 'OLED55C1PUB', '00:1B:44:11:3A:C2', '192.168.2.202', 'ACTIVE', 'Room 202', 7, 'WebOS 6.0', 'SN004TV202', TRUE),
      ('TV301', 'SMART_TV', 'Samsung', 'QN65Q90A', '00:1B:44:11:3A:D1', '192.168.2.301', 'ACTIVE', 'Suite 301', 11, 'Tizen 6.5', 'SN005TV301', TRUE),
      ('STB001', 'SET_TOP_BOX', 'Roku', 'Ultra 4K', '00:1B:44:11:3A:E1', '192.168.2.151', 'ACTIVE', 'Lobby', NULL, 'Roku OS 11', 'SN006STB001', TRUE),
      ('STB002', 'ANDROID_BOX', 'Nvidia', 'Shield TV Pro', '00:1B:44:11:3A:F1', '192.168.2.152', 'MAINTENANCE', 'Conference Room A', NULL, 'Android TV 11', 'SN007STB002', FALSE);

-- ==========================================
-- ASSIGN CHANNEL PACKAGES TO ROOMS
-- ==========================================

-- Assign basic package to standard rooms
UPDATE rooms SET channel_package_id = 1 WHERE room_type IN ('STANDARD');

-- Assign premium package to deluxe rooms and suites
UPDATE rooms SET channel_package_id = 2 WHERE room_type IN ('DELUXE', 'SUITE', 'JUNIOR_SUITE', 'PRESIDENTIAL_SUITE', 'FAMILY_ROOM');

-- ==========================================
-- INSERT PACKAGE CHANNEL ASSIGNMENTS
-- ==========================================

-- Basic Package (ID: 1) - Essential channels
INSERT INTO package_channels (package_id, channel_id, position, is_enabled) VALUES
                                                                                (1, 1, 1, TRUE),  -- CNN International
                                                                                (1, 2, 2, TRUE),  -- BBC World News
                                                                                (1, 7, 3, TRUE),  -- Discovery Channel
                                                                                (1, 9, 4, TRUE),  -- Cartoon Network
                                                                                (1, 10, 5, TRUE), -- Disney Channel
                                                                                (1, 12, 6, TRUE); -- TF1

-- Premium Package (ID: 2) - All channels
INSERT INTO package_channels (package_id, channel_id, position, is_enabled)
SELECT 2, id, sort_order, TRUE FROM tv_channels WHERE is_active = TRUE;

-- Sports Package (ID: 3) - Sports channels only
INSERT INTO package_channels (package_id, channel_id, position, is_enabled) VALUES
                                                                                (3, 4, 1, TRUE),  -- ESPN
                                                                                (3, 5, 2, TRUE),  -- Eurosport 1
                                                                                (3, 6, 3, TRUE),  -- beIN Sports 1
                                                                                (3, 7, 4, TRUE);  -- Sky Sports

-- ==========================================
-- INSERT SAMPLE EPG ENTRIES
-- ==========================================

-- Sample EPG entries for today and tomorrow
INSERT INTO epg_entries (channel_id, title, description, start_time, end_time, genre) VALUES
-- CNN International (Channel 1)
(1, 'World News Now', 'Breaking news and analysis from around the world', '2025-01-20 06:00:00', '2025-01-20 07:00:00', 'News'),
(1, 'CNN Business', 'Global business news and market analysis', '2025-01-20 07:00:00', '2025-01-20 08:00:00', 'Business'),
(1, 'International Desk', 'In-depth coverage of international affairs', '2025-01-20 08:00:00', '2025-01-20 09:00:00', 'News'),

-- ESPN (Channel 4)
(4, 'SportsCenter', 'Latest sports news and highlights', '2025-01-20 06:00:00', '2025-01-20 07:00:00', 'Sports'),
(4, 'NFL Analysis', 'Expert analysis of recent NFL games', '2025-01-20 07:00:00', '2025-01-20 08:00:00', 'Sports'),
(4, 'College Basketball', 'Live college basketball coverage', '2025-01-20 08:00:00', '2025-01-20 11:00:00', 'Sports'),

-- Disney Channel (Channel 10)
(10, 'Mickey Mouse Clubhouse', 'Educational entertainment for children', '2025-01-20 06:00:00', '2025-01-20 06:30:00', 'Kids'),
(10, 'Frozen II', 'Animated musical fantasy film', '2025-01-20 06:30:00', '2025-01-20 08:15:00', 'Family'),
(10, 'The Lion King', 'Classic animated adventure', '2025-01-20 08:15:00', '2025-01-20 10:00:00', 'Family');

-- ==========================================
-- CREATE INDEXES FOR BETTER PERFORMANCE
-- ==========================================

-- Additional performance indexes based on common query patterns
CREATE INDEX idx_users_role_active ON users(role, is_active);
CREATE INDEX idx_languages_guest_display ON languages(is_guest_enabled, display_order);
CREATE INDEX idx_rooms_status_type ON rooms(status, room_type);
CREATE INDEX idx_channels_active_sort ON tv_channels(is_active, sort_order);
CREATE INDEX idx_terminals_room_status ON terminals(room_id, status);
CREATE INDEX idx_epg_channel_time ON epg_entries(channel_id, start_time, end_time);

-- ==========================================
-- FINAL VERIFICATION QUERIES
-- ==========================================

-- Verify data insertion
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Languages', COUNT(*) FROM languages
UNION ALL
SELECT 'Categories', COUNT(*) FROM tv_channel_categories
UNION ALL
SELECT 'Channels', COUNT(*) FROM tv_channels
UNION ALL
SELECT 'Rooms', COUNT(*) FROM rooms
UNION ALL
SELECT 'Guests', COUNT(*) FROM guests
UNION ALL
SELECT 'Terminals', COUNT(*) FROM terminals
UNION ALL
SELECT 'Packages', COUNT(*) FROM channel_packages
UNION ALL
SELECT 'EPG Entries', COUNT(*) FROM epg_entries;

-- Commit the transaction
COMMIT;