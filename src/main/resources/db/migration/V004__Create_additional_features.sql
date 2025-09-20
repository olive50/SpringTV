-- ===================================================================
-- TVBOOT IPTV Platform - Additional Features (CORRECTED)
-- Migration: V004__Create_additional_features.sql
-- Description: Add additional features and improvements (NO DATA DUPLICATION)
-- Author: TVBOOT Team
-- Date: 2025-01-20
-- ===================================================================

-- ==========================================
-- ADDITIONAL INDEXES FOR PERFORMANCE
-- ==========================================



-- ==========================================
-- ADDITIONAL TABLES FOR FUTURE FEATURES
-- ==========================================

-- System Settings table
CREATE TABLE IF NOT EXISTS system_settings (
                                               id BIGINT NOT NULL AUTO_INCREMENT,
                                               setting_key VARCHAR(100) NOT NULL UNIQUE,
                                               setting_value TEXT,
                                               setting_type ENUM('STRING', 'INTEGER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
                                               description TEXT,
                                               is_public BOOLEAN DEFAULT FALSE,
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                               PRIMARY KEY (id),
                                               INDEX idx_settings_key (setting_key),
                                               INDEX idx_settings_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System configuration settings';

-- Audit Log table for detailed tracking
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          user_id BIGINT,
                                          action VARCHAR(100) NOT NULL,
                                          resource_type VARCHAR(50) NOT NULL,
                                          resource_id BIGINT,
                                          old_values JSON,
                                          new_values JSON,
                                          ip_address VARCHAR(45),
                                          user_agent TEXT,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (id),
                                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
                                          INDEX idx_audit_user (user_id),
                                          INDEX idx_audit_action (action),
                                          INDEX idx_audit_resource (resource_type, resource_id),
                                          INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Detailed audit trail for system actions';

-- Hotel Information table
CREATE TABLE IF NOT EXISTS hotel_info (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          name VARCHAR(200) NOT NULL,
                                          address TEXT,
                                          phone VARCHAR(30),
                                          email VARCHAR(100),
                                          website VARCHAR(200),
                                          logo_url VARCHAR(500),
                                          timezone VARCHAR(50) DEFAULT 'UTC',
                                          default_language_id BIGINT,
                                          check_in_time TIME DEFAULT '15:00:00',
                                          check_out_time TIME DEFAULT '11:00:00',
                                          currency_code CHAR(3) DEFAULT 'USD',
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (id),
                                          FOREIGN KEY (default_language_id) REFERENCES languages(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hotel basic information and settings';

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
                                             id BIGINT NOT NULL AUTO_INCREMENT,
                                             user_id BIGINT,
                                             title VARCHAR(200) NOT NULL,
                                             message TEXT NOT NULL,
                                             type ENUM('INFO', 'WARNING', 'ERROR', 'SUCCESS') DEFAULT 'INFO',
                                             is_read BOOLEAN DEFAULT FALSE,
                                             is_system BOOLEAN DEFAULT FALSE,
                                             expires_at DATETIME,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             PRIMARY KEY (id),
                                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                             INDEX idx_notifications_user (user_id),
                                             INDEX idx_notifications_unread (user_id, is_read),
                                             INDEX idx_notifications_type (type),
                                             INDEX idx_notifications_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System and user notifications';

-- ==========================================
-- INSERT INITIAL SYSTEM SETTINGS
-- ==========================================

INSERT IGNORE INTO system_settings (setting_key, setting_value, setting_type, description, is_public) VALUES
                                                                                                          ('app.name', 'TVBOOT IPTV Platform', 'STRING', 'Application name', TRUE),
                                                                                                          ('app.version', '1.2.0', 'STRING', 'Application version', TRUE),
                                                                                                          ('app.environment', 'development', 'STRING', 'Current environment', FALSE),
                                                                                                          ('security.session_timeout', '3600', 'INTEGER', 'Session timeout in seconds', FALSE),
                                                                                                          ('tv.max_channels_per_package', '500', 'INTEGER', 'Maximum channels per package', FALSE),
                                                                                                          ('terminal.heartbeat_interval', '60', 'INTEGER', 'Terminal heartbeat interval in seconds', FALSE),
                                                                                                          ('epg.retention_days', '7', 'INTEGER', 'EPG data retention in days', FALSE),
                                                                                                          ('ui.default_page_size', '20', 'INTEGER', 'Default pagination size', FALSE),
                                                                                                          ('maintenance.auto_cleanup', 'true', 'BOOLEAN', 'Enable automatic data cleanup', FALSE),
                                                                                                          ('features.epg_enabled', 'true', 'BOOLEAN', 'Enable EPG functionality', TRUE);

-- ==========================================
-- INSERT DEFAULT HOTEL INFO
-- ==========================================

INSERT IGNORE INTO hotel_info (
    name, address, phone, email, timezone,
    default_language_id, check_in_time, check_out_time, currency_code
) VALUES (
             'Demo Hotel TVBOOT',
             '123 Hotel Street, City, Country',
             '+1-555-HOTEL',
             'info@demo-hotel.com',
             'UTC',
             (SELECT id FROM languages WHERE iso_639_1 = 'en' LIMIT 1),
             '15:00:00',
             '11:00:00',
             'USD'
         );

-- ==========================================
-- ADD USEFUL VIEWS
-- ==========================================

-- Vue pour les statistiques des chaînes
CREATE OR REPLACE VIEW tv_channel_stats AS
SELECT
    c.name as category_name,
    l.name as language_name,
    COUNT(*) as channel_count,
    SUM(CASE WHEN tc.is_active = TRUE THEN 1 ELSE 0 END) as active_channels,
    SUM(CASE WHEN tc.is_hd = TRUE THEN 1 ELSE 0 END) as hd_channels
FROM tv_channels tc
         LEFT JOIN tv_channel_categories c ON tc.category_id = c.id
         LEFT JOIN languages l ON tc.language_id = l.id
GROUP BY c.id, l.id;

-- Vue pour les statistiques des chambres
CREATE OR REPLACE VIEW room_occupancy_stats AS
SELECT
    room_type,
    COUNT(*) as total_rooms,
    SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) as available_rooms,
    SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied_rooms,
    SUM(CASE WHEN status = 'MAINTENANCE' THEN 1 ELSE 0 END) as maintenance_rooms,
    SUM(CASE WHEN status = 'CLEANING' THEN 1 ELSE 0 END) as cleaning_rooms,
    ROUND(AVG(price_per_night), 2) as avg_price
FROM rooms
GROUP BY room_type;

-- Vue pour le statut des terminaux
CREATE OR REPLACE VIEW terminal_status_summary AS
SELECT
    device_type,
    status,
    COUNT(*) as terminal_count,
    SUM(CASE WHEN is_online = TRUE THEN 1 ELSE 0 END) as online_count
FROM terminals
GROUP BY device_type, status;

-- ==========================================
-- ADD TRIGGERS FOR AUDIT
-- ==========================================

-- Trigger pour auditer les modifications d'utilisateurs
DELIMITER $$

CREATE TRIGGER IF NOT EXISTS users_audit_update
    AFTER UPDATE ON users
    FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, resource_type, resource_id, old_values, new_values, created_at)
    VALUES (
               NEW.id,
               'UPDATE',
               'USER',
               NEW.id,
               JSON_OBJECT(
                       'username', OLD.username,
                       'email', OLD.email,
                       'role', OLD.role,
                       'is_active', OLD.is_active
               ),
               JSON_OBJECT(
                       'username', NEW.username,
                       'email', NEW.email,
                       'role', NEW.role,
                       'is_active', NEW.is_active
               ),
               NOW()
           );
END$$

-- Trigger pour auditer les modifications de chaînes TV
CREATE TRIGGER IF NOT EXISTS tv_channels_audit_update
    AFTER UPDATE ON tv_channels
    FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (action, resource_type, resource_id, old_values, new_values, created_at)
    VALUES (
               'UPDATE',
               'TV_CHANNEL',
               NEW.id,
               JSON_OBJECT(
                       'name', OLD.name,
                       'channel_number', OLD.channel_number,
                       'is_active', OLD.is_active
               ),
               JSON_OBJECT(
                       'name', NEW.name,
                       'channel_number', NEW.channel_number,
                       'is_active', NEW.is_active
               ),
               NOW()
           );
END$$

DELIMITER ;

-- ==========================================
-- FINAL VERIFICATION
-- ==========================================

SELECT 'V004 Migration completed successfully - Additional features added' as status;

-- Show summary of new tables
SELECT
    TABLE_NAME,
    TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('system_settings', 'audit_logs', 'hotel_info', 'notifications')
ORDER BY TABLE_NAME;