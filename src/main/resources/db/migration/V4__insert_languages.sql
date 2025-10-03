-- SQL INSERT Script for 10 Common Languages in IPTV Platform
-- Use CURRENT_TIMESTAMP instead of NOW() for Flyway compatibility

-- 1. English
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('English', 'English', 'en', 'eng', 'en-US', 'UTF-8', '/assets/flags/en.svg', false, true, true, 1, 'Roboto, Arial', 'USD', '$', 'MM/DD/YYYY', 'hh:mm A', '#,##0.00', '.', ',', CURRENT_TIMESTAMP);

-- 2. French
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('French', 'Français', 'fr', 'fra', 'fr-FR', 'UTF-8', '/assets/flags/fr.svg', false, true, true, 2, 'Roboto, Arial', 'EUR', '€', 'DD/MM/YYYY', 'HH:mm', '#,##0.00', ',', ' ', CURRENT_TIMESTAMP);

-- 3. Arabic
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('Arabic', 'العربية', 'ar', 'ara', 'ar-SA', 'UTF-8', '/assets/flags/ar.svg', true, true, true, 3, 'Cairo, Arial', 'SAR', 'ر.س', 'DD/MM/YYYY', 'hh:mm A', '#,##0.00', '.', ',', CURRENT_TIMESTAMP);

-- 4. Spanish
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('Spanish', 'Español', 'es', 'spa', 'es-ES', 'UTF-8', '/assets/flags/es.svg', false, true, true, 4, 'Roboto, Arial', 'EUR', '€', 'DD/MM/YYYY', 'HH:mm', '#,##0.00', ',', '.', CURRENT_TIMESTAMP);

-- 5. German
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('German', 'Deutsch', 'de', 'deu', 'de-DE', 'UTF-8', '/assets/flags/de.svg', false, true, true, 5, 'Roboto, Arial', 'EUR', '€', 'DD.MM.YYYY', 'HH:mm', '#,##0.00', ',', '.', CURRENT_TIMESTAMP);

-- 6. Italian
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('Italian', 'Italiano', 'it', 'ita', 'it-IT', 'UTF-8', '/assets/flags/it.svg', false, true, true, 6, 'Roboto, Arial', 'EUR', '€', 'DD/MM/YYYY', 'HH:mm', '#,##0.00', ',', '.', CURRENT_TIMESTAMP);

-- 7. Chinese (Simplified)
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('Chinese', '简体中文', 'zh', 'zho', 'zh-CN', 'UTF-8', '/assets/flags/cn.svg', false, true, true, 7, 'Microsoft YaHei, SimSun', 'CNY', '¥', 'YYYY/MM/DD', 'HH:mm', '#,##0.00', '.', ',', CURRENT_TIMESTAMP);

-- 8. Russian
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('Russian', 'Русский', 'ru', 'rus', 'ru-RU', 'UTF-8', '/assets/flags/ru.svg', false, true, true, 8, 'Roboto, Arial', 'RUB', '₽', 'DD.MM.YYYY', 'HH:mm', '#,##0.00', ',', ' ', CURRENT_TIMESTAMP);

-- 9. Portuguese
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('Portuguese', 'Português', 'pt', 'por', 'pt-PT', 'UTF-8', '/assets/flags/pt.svg', false, true, true, 9, 'Roboto, Arial', 'EUR', '€', 'DD/MM/YYYY', 'HH:mm', '#,##0.00', ',', '.', CURRENT_TIMESTAMP);

-- 10. Japanese
INSERT INTO languages (name, native_name, iso_639_1, iso_639_2, locale_code, charset, flag_path, is_rtl, is_admin_enabled, is_guest_enabled, display_order, font_family, currency_code, currency_symbol, date_format, time_format, number_format, decimal_separator, thousands_separator, created_at)
VALUES ('Japanese', '日本語', 'ja', 'jpn', 'ja-JP', 'UTF-8', '/assets/flags/jp.svg', false, true, true, 10, 'Yu Gothic, Meiryo', 'JPY', '¥', 'YYYY/MM/DD', 'HH:mm', '#,##0', '.', ',', CURRENT_TIMESTAMP);