-- Translations pour TVBOOT IPTV Platform - Welcome Screen
-- English (language_id = 1), French (language_id = 2), Arabic (language_id = 3)

-- ==========================================
-- ENGLISH TRANSLATIONS (language_id = 1)
-- ==========================================

-- Welcome Section
INSERT INTO translations (language_id, message_key, message_value, created_at, updated_at) VALUES
                                                                                               (1, 'guest.welcome.title', 'Welcome', NOW(), NOW()),
                                                                                               (1, 'guest.welcome.message', 'We''re delighted to have you as our guest. Your comfort is our priority, and we''re here to ensure your stay is exceptional in every way. Enjoy our premium amenities and personalized service throughout your stay.', NOW(), NOW()),
                                                                                               (1, 'guest.welcome.highlight', 'exceptional in every way', NOW(), NOW()),
                                                                                               (1, 'guest.room.label', 'Room Number', NOW(), NOW()),

-- Language Selection
                                                                                               (1, 'guest.language.title', 'Select your language to begin', NOW(), NOW()),
                                                                                               (1, 'guest.language.russian', 'Russian', NOW(), NOW()),
                                                                                               (1, 'guest.language.chinese', 'Chinese', NOW(), NOW()),
                                                                                               (1, 'guest.language.arabic', 'Arabic', NOW(), NOW()),
                                                                                               (1, 'guest.language.german', 'Deutsch', NOW(), NOW()),
                                                                                               (1, 'guest.language.italian', 'Italiano', NOW(), NOW()),
                                                                                               (1, 'guest.language.spanish', 'Spanish', NOW(), NOW()),
                                                                                               (1, 'guest.language.english', 'English', NOW(), NOW()),
                                                                                               (1, 'guest.language.french', 'French', NOW(), NOW()),

-- WiFi Section
                                                                                               (1, 'guest.wifi.title', 'Hotel WiFi Information', NOW(), NOW()),
                                                                                               (1, 'guest.wifi.ssid', 'SSID:', NOW(), NOW()),
                                                                                               (1, 'guest.wifi.password', 'Password:', NOW(), NOW()),
                                                                                               (1, 'guest.wifi.security', 'Security:', NOW(), NOW()),
                                                                                               (1, 'guest.wifi.qr.label', 'Scan to connect automatically', NOW(), NOW()),
                                                                                               (1, 'guest.wifi.copy', 'Copy', NOW(), NOW()),

-- TV & Services
                                                                                               (1, 'guest.tv.live', 'Live TV', NOW(), NOW()),
                                                                                               (1, 'guest.tv.channels', 'TV Channels', NOW(), NOW()),
                                                                                               (1, 'guest.services.hotel', 'Hotel Services', NOW(), NOW()),
                                                                                               (1, 'guest.services.room', 'Room Service', NOW(), NOW()),
                                                                                               (1, 'guest.services.reception', 'Reception', NOW(), NOW()),

-- Common UI
                                                                                               (1, 'guest.common.settings', 'Settings', NOW(), NOW()),
                                                                                               (1, 'guest.common.volume', 'Volume', NOW(), NOW()),
                                                                                               (1, 'guest.common.checkout', 'Check Out', NOW(), NOW()),
                                                                                               (1, 'guest.common.footer', '© 2023 Samsung Hospitality. All rights reserved.', NOW(), NOW());

-- ==========================================
-- FRENCH TRANSLATIONS (language_id = 2)
-- ==========================================

-- Welcome Section
INSERT INTO translations (language_id, message_key, message_value, created_at, updated_at) VALUES
                                                                                               (2, 'guest.welcome.title', 'Bienvenue', NOW(), NOW()),
                                                                                               (2, 'guest.welcome.message', 'Nous sommes ravis de vous accueillir. Votre confort est notre priorité et nous sommes là pour nous assurer que votre séjour soit exceptionnel à tous égards. Profitez de nos équipements haut de gamme et de notre service personnalisé tout au long de votre séjour.', NOW(), NOW()),
                                                                                               (2, 'guest.welcome.highlight', 'exceptionnel à tous égards', NOW(), NOW()),
                                                                                               (2, 'guest.room.label', 'Numéro de Chambre', NOW(), NOW()),

-- Language Selection
                                                                                               (2, 'guest.language.title', 'Sélectionnez votre langue pour commencer', NOW(), NOW()),
                                                                                               (2, 'guest.language.russian', 'Russe', NOW(), NOW()),
                                                                                               (2, 'guest.language.chinese', 'Chinois', NOW(), NOW()),
                                                                                               (2, 'guest.language.arabic', 'Arabe', NOW(), NOW()),
                                                                                               (2, 'guest.language.german', 'Allemand', NOW(), NOW()),
                                                                                               (2, 'guest.language.italian', 'Italien', NOW(), NOW()),
                                                                                               (2, 'guest.language.spanish', 'Espagnol', NOW(), NOW()),
                                                                                               (2, 'guest.language.english', 'Anglais', NOW(), NOW()),
                                                                                               (2, 'guest.language.french', 'Français', NOW(), NOW()),

-- WiFi Section
                                                                                               (2, 'guest.wifi.title', 'Informations WiFi de l''Hôtel', NOW(), NOW()),
                                                                                               (2, 'guest.wifi.ssid', 'SSID:', NOW(), NOW()),
                                                                                               (2, 'guest.wifi.password', 'Mot de passe:', NOW(), NOW()),
                                                                                               (2, 'guest.wifi.security', 'Sécurité:', NOW(), NOW()),
                                                                                               (2, 'guest.wifi.qr.label', 'Scanner pour se connecter automatiquement', NOW(), NOW()),
                                                                                               (2, 'guest.wifi.copy', 'Copier', NOW(), NOW()),

-- TV & Services
                                                                                               (2, 'guest.tv.live', 'TV en Direct', NOW(), NOW()),
                                                                                               (2, 'guest.tv.channels', 'Chaînes TV', NOW(), NOW()),
                                                                                               (2, 'guest.services.hotel', 'Services de l''Hôtel', NOW(), NOW()),
                                                                                               (2, 'guest.services.room', 'Service en Chambre', NOW(), NOW()),
                                                                                               (2, 'guest.services.reception', 'Réception', NOW(), NOW()),

-- Common UI
                                                                                               (2, 'guest.common.settings', 'Paramètres', NOW(), NOW()),
                                                                                               (2, 'guest.common.volume', 'Volume', NOW(), NOW()),
                                                                                               (2, 'guest.common.checkout', 'Départ', NOW(), NOW()),
                                                                                               (2, 'guest.common.footer', '© 2023 Samsung Hospitality. Tous droits réservés.', NOW(), NOW());

-- ==========================================
-- ARABIC TRANSLATIONS (language_id = 3)
-- ==========================================

-- Welcome Section
INSERT INTO translations (language_id, message_key, message_value, created_at, updated_at) VALUES
                                                                                               (3, 'guest.welcome.title', 'مرحبا', NOW(), NOW()),
                                                                                               (3, 'guest.welcome.message', 'يسعدنا استضافتك. راحتك هي أولويتنا ونحن هنا للتأكد من أن إقامتك استثنائية من جميع النواحي. استمتع بوسائل الراحة المميزة لدينا والخدمة الشخصية طوال فترة إقامتك.', NOW(), NOW()),
                                                                                               (3, 'guest.welcome.highlight', 'استثنائية من جميع النواحي', NOW(), NOW()),
                                                                                               (3, 'guest.room.label', 'رقم الغرفة', NOW(), NOW()),

-- Language Selection
                                                                                               (3, 'guest.language.title', 'اختر لغتك للبدء', NOW(), NOW()),
                                                                                               (3, 'guest.language.russian', 'الروسية', NOW(), NOW()),
                                                                                               (3, 'guest.language.chinese', 'الصينية', NOW(), NOW()),
                                                                                               (3, 'guest.language.arabic', 'العربية', NOW(), NOW()),
                                                                                               (3, 'guest.language.german', 'الألمانية', NOW(), NOW()),
                                                                                               (3, 'guest.language.italian', 'الإيطالية', NOW(), NOW()),
                                                                                               (3, 'guest.language.spanish', 'الإسبانية', NOW(), NOW()),
                                                                                               (3, 'guest.language.english', 'الإنجليزية', NOW(), NOW()),
                                                                                               (3, 'guest.language.french', 'الفرنسية', NOW(), NOW()),

-- WiFi Section
                                                                                               (3, 'guest.wifi.title', 'معلومات واي فاي الفندق', NOW(), NOW()),
                                                                                               (3, 'guest.wifi.ssid', 'اسم الشبكة:', NOW(), NOW()),
                                                                                               (3, 'guest.wifi.password', 'كلمة المرور:', NOW(), NOW()),
                                                                                               (3, 'guest.wifi.security', 'الأمان:', NOW(), NOW()),
                                                                                               (3, 'guest.wifi.qr.label', 'امسح للاتصال تلقائياً', NOW(), NOW()),
                                                                                               (3, 'guest.wifi.copy', 'نسخ', NOW(), NOW()),

-- TV & Services
                                                                                               (3, 'guest.tv.live', 'البث المباشر', NOW(), NOW()),
                                                                                               (3, 'guest.tv.channels', 'قنوات التلفزيون', NOW(), NOW()),
                                                                                               (3, 'guest.services.hotel', 'خدمات الفندق', NOW(), NOW()),
                                                                                               (3, 'guest.services.room', 'خدمة الغرف', NOW(), NOW()),
                                                                                               (3, 'guest.services.reception', 'الاستقبال', NOW(), NOW()),

-- Common UI
                                                                                               (3, 'guest.common.settings', 'الإعدادات', NOW(), NOW()),
                                                                                               (3, 'guest.common.volume', 'مستوى الصوت', NOW(), NOW()),
                                                                                               (3, 'guest.common.checkout', 'المغادرة', NOW(), NOW()),
                                                                                               (3, 'guest.common.footer', '© 2023 سامسونج للضيافة. جميع الحقوق محفوظة.', NOW(), NOW());