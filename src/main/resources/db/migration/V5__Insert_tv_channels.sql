-- SQL Script to populate tv_channels table from nd-channels.json
-- Generated for TVBOOT IPTV System - Hotel New Day
-- CORRECTED VERSION - Sans IDs explicites pour éviter les conflits

-- Insert TV channels categories first
INSERT INTO tv_channel_categories(name)
VALUES('News'),('Sport')
ON CONFLICT (name) DO NOTHING;

-- Insert TV channels data WITHOUT explicit IDs
-- Let PostgreSQL auto-generate the IDs using the sequence

INSERT INTO public.tv_channels (channel_number, name, description, ip, port, web_url, category_id, language_id, logo_path, is_active, is_available, sort_order, created_at, updated_at)
VALUES
    (71, 'El WATANIA TV', 'El Watania TV', '239.200.0.89', 1234, 'udp://239.200.0.89:1234', 1, 1, 'elwatania-dz.gif', true, true, 2, NOW(), NOW()),
    (70, 'IQRAA', 'Iqraa TV', '239.200.0.88', 1234, 'udp://239.200.0.88:1234', 1, 1, 'Iqraa_TV_Logo.gif', true, true, 8, NOW(), NOW()),
    (21, 'VOX', 'VOX', '239.200.0.25', 1234, 'udp://239.200.0.25:1234', 1, 1, 'vox_95x30.gif', true, true, 18, NOW(), NOW()),
    (26, 'Russia Today', 'Russia Today', '239.200.0.30', 1234, 'udp://239.200.0.30:1234', 1, 1, 'Russia_Today_Logo.gif', true, true, 19, NOW(), NOW()),
    (23, 'France 24 En', 'France 24 English', '239.200.0.27', 1234, 'udp://239.200.0.27:1234', 1, 1, 'France_24_Logo.gif', true, true, 20, NOW(), NOW()),
    (25, 'France 24', 'France 24 Arabic', '239.200.0.29', 1234, 'udp://239.200.0.29:1234', 1, 1, 'France_24_AR_Logo.gif', true, true, 21, NOW(), NOW()),
    (16, 'CGTN', 'CGTN News', '239.200.0.20', 1234, 'udp://239.200.0.20:1234', 1, 1, 'CCTV_NEWS_Logo.gif', true, true, 22, NOW(), NOW()),
    (22, 'RTL 2 Austria', 'RTL 2 Austria', '239.200.0.26', 1234, 'udp://239.200.0.26:1234', 1, 1, 'tv_rtl.gif', true, true, 23, NOW(), NOW()),
    (24, 'France 24 Fr', 'France 24 French', '239.200.0.28', 1234, 'udp://239.200.0.28:1234', 1, 1, 'France_24_Fr_Logo.gif', true, true, 24, NOW(), NOW()),
    (19, 'RTL CH', 'RTL Switzerland', '239.200.0.23', 1234, 'udp://239.200.0.23:1234', 1, 1, 'tv_rtl.gif', true, true, 25, NOW(), NOW()),
    (18, 'Super RTL', 'Super RTL', '239.200.0.22', 1234, 'udp://239.200.0.22:1234', 1, 1, 'tv_rtl.gif', true, true, 26, NOW(), NOW()),
    (17, 'CGTN Doc', 'CGTN Documentary', '239.200.0.21', 1234, 'udp://239.200.0.21:1234', 1, 1, 'CCTV9_Logo.gif', true, true, 27, NOW(), NOW()),
    (20, 'VOX Austria', 'VOX Austria', '239.200.0.24', 1234, 'udp://239.200.0.24:1234', 1, 1, 'vox_95x30.gif', true, true, 29, NOW(), NOW()),
    (28, 'Canal Algerie', 'ENTV 2 Canal Algerie', '239.200.0.32', 1234, 'udp://239.200.0.32:1234', 1, 1, 'ENTV_2_Canal_Algerie_Logo.gif', true, true, 30, NOW(), NOW()),
    (49, 'Eurosport 1 Deutschland', 'Eurosport 1 Germany', '239.200.0.53', 1234, 'udp://239.200.0.53:1234', 1, 1, 'tv_eurosport.gif', true, true, 31, NOW(), NOW()),
    (30, 'Algeria Amazigh', 'ENTV 4 Tamazigh', '239.200.0.34', 1234, 'udp://239.200.0.34:1234', 1, 1, 'ENTV_4_Tamazigh_Logot.gif', true, true, 32, NOW(), NOW()),
    (31, 'Algeria Coran', 'ENTV 5 Coran', '239.200.0.35', 1234, 'udp://239.200.0.35:1234', 1, 1, 'ENTV_5_Coran_Logo.gif', true, true, 33, NOW(), NOW()),
    (29, 'Algeria A3', 'ENTV 3 Algeria 3', '239.200.0.33', 1234, 'udp://239.200.0.33:1234', 1, 1, 'ENTV_3_Algeria_3_Logo.gif', true, true, 34, NOW(), NOW()),
    (32, 'Echourouk TV', 'Echourouk TV', '239.200.0.36', 1234, 'udp://239.200.0.36:1234', 1, 1, 'echourouk_95x38 (1).gif', true, true, 35, NOW(), NOW()),
    (33, 'Echourouk News', 'Echourouk News', '239.200.0.37', 1234, 'udp://239.200.0.37:1234', 1, 1, 'echorouk_tv_95x38.gif', true, true, 36, NOW(), NOW()),
    (34, 'Echourouk Benna', 'Echourouk Benna TV', '239.200.0.38', 1234, 'udp://239.200.0.38:1234', 1, 1, 'benaTV-95x95.gif', true, true, 37, NOW(), NOW()),
    (35, 'El Djazairia TV', 'El Djazairia TV', '239.200.0.39', 1234, 'udp://239.200.0.39:1234', 1, 1, 'El_Djazairia_LOGO.gif', true, true, 38, NOW(), NOW()),
    (36, 'El Heddaf TV', 'El Heddaf TV', '239.200.0.40', 1234, 'udp://239.200.0.40:1234', 1, 1, 'El_Heddaf_TV_Logo.gif', true, true, 39, NOW(), NOW()),
    (37, 'TRT WORLD HD', 'TRT World HD', '239.200.0.41', 1234, 'udp://239.200.0.41:1234', 1, 1, 'TRT_World-TV.gif', true, true, 40, NOW(), NOW()),
    (38, 'Samira TV', 'Samira TV', '239.200.0.42', 1234, 'udp://239.200.0.42:1234', 1, 1, 'Samira_TV_Logo.gif', true, true, 41, NOW(), NOW()),
    (39, 'Beur', 'Beur TV', '239.200.0.43', 1234, 'udp://239.200.0.43:1234', 1, 1, 'Beur_TV_LOGO.gif', true, true, 42, NOW(), NOW()),
    (41, 'Nessma', 'Nessma TV', '239.200.0.45', 1234, 'udp://239.200.0.45:1234', 1, 1, 'Nessma_TV_Logo.gif', true, true, 43, NOW(), NOW()),
    (42, 'Al DJazeera Doc', 'Al Jazeera Documentary', '239.200.0.46', 1234, 'udp://239.200.0.46:1234', 1, 1, 'alJazeeraDoc_95x105.gif', true, true, 44, NOW(), NOW()),
    (43, 'Al DJazeera Eng', 'Al Jazeera English', '239.200.0.47', 1234, 'udp://239.200.0.47:1234', 1, 1, 'alJazeera_95x90.gif', true, true, 45, NOW(), NOW()),
    (44, 'Al DJazeera HD', 'Al Jazeera HD', '239.200.0.48', 1234, 'udp://239.200.0.48:1234', 1, 1, 'alJazeera_95x90.gif', true, true, 46, NOW(), NOW()),
    (45, 'Al DJazeera Mubasher HD', 'Al Jazeera Mubasher HD', '239.200.0.49', 1234, 'udp://239.200.0.49:1234', 1, 1, 'alJazeeraMubahser_95x102.gif', true, true, 47, NOW(), NOW()),
    (46, 'Al Arabiya', 'Al Arabiya', '239.200.0.50', 1234, 'udp://239.200.0.50:1234', 1, 1, 'al-Arabiya_95x37.gif', true, true, 48, NOW(), NOW()),
    (4, 'NRJ 12', 'NRJ 12', '239.200.0.8', 1234, 'udp://239.200.0.8:1234', 1, 1, 'nrj-12_4.png', true, true, 4, NOW(), NOW()),
    (5, 'W9', 'W9', '239.200.0.9', 1234, 'udp://239.200.0.9:1234', 1, 1, 'w9_5.png', true, true, 9, NOW(), NOW()),
    (6, 'LCP', 'LCP', '239.200.0.10', 1234, 'udp://239.200.0.10:1234', 1, 1, 'lcp_6.png', true, true, 10, NOW(), NOW()),
    (7, 'ARTE', 'ARTE', '239.200.0.11', 1234, 'udp://239.200.0.11:1234', 1, 1, 'arte_7.png', true, true, 11, NOW(), NOW()),
    (9, 'TMC', 'TMC', '239.200.0.13', 1234, 'udp://239.200.0.13:1234', 1, 1, 'tmc_9.png', true, true, 13, NOW(), NOW()),
    (8, 'NT1', 'NT1', '239.200.0.12', 1234, 'udp://239.200.0.12:1234', 1, 1, 'nt1_8.png', true, true, 12, NOW(), NOW()),
    (10, 'TV5 Monde Europe', 'TV5 Monde Europe', '239.200.0.14', 1234, 'udp://239.200.0.14:1234', 1, 1, 'tv5-monde-europe_10.png', true, true, 3, NOW(), NOW()),
    (11, 'Euronews', 'Euronews', '239.200.0.15', 1234, 'udp://239.200.0.15:1234', 1, 1, 'euronews_11.png', true, true, 14, NOW(), NOW()),
    (12, 'RAI NEWS', 'RAI News 24', '239.200.0.16', 1234, 'udp://239.200.0.16:1234', 1, 1, 'rai-news_12.png', true, true, 28, NOW(), NOW()),
    (13, 'ZDF', 'ZDF', '239.200.0.17', 1234, 'udp://239.200.0.17:1234', 1, 1, 'zdf_13.png', true, true, 15, NOW(), NOW()),
    (14, 'Sky News Arabia', 'Sky News Arabia', '239.200.0.18', 1234, 'udp://239.200.0.18:1234', 1, 1, 'sky-news-arabia_14.png', true, true, 16, NOW(), NOW()),
    (15, 'CGTN F', 'CCTV French', '239.200.0.19', 1234, 'udp://239.200.0.19:1234', 1, 1, 'cgtn-f_15.png', true, true, 17, NOW(), NOW()),
    (2, 'M6', 'M6', '239.200.0.2', 1234, 'udp://239.200.0.2:1234', 1, 1, 'm6_2.png', true, false, 6, NOW(), NOW()),
    (47, 'BBC Arabic', 'BBC Arabic', '239.200.0.51', 1234, 'udp://239.200.0.51:1234', 1, 1, 'tv_bbc-news.gif', true, true, 49, NOW(), NOW()),
    (48, 'CNN', 'CNN International', '239.200.0.52', 1234, 'udp://239.200.0.52:1234', 1, 1, 'CNN_Logo.gif', true, true, 50, NOW(), NOW()),
    (50, 'Numidia TV', 'Numidia News', '239.200.0.54', 1234, 'udp://239.200.0.54:1234', 1, 1, 'Numidia_News_Logo.gif', true, true, 51, NOW(), NOW()),
    (51, 'MBC 1', 'MBC 1', '239.200.0.55', 1234, 'udp://239.200.0.55:1234', 1, 1, 'MBC_1_Logo.gif', true, true, 52, NOW(), NOW()),
    (52, 'MBC 2', 'MBC 2', '239.200.0.56', 1234, 'udp://239.200.0.56:1234', 1, 1, 'MBC_2_Logo.gif', true, true, 53, NOW(), NOW()),
    (53, 'MBC 3', 'MBC 3', '239.200.0.57', 1234, 'udp://239.200.0.57:1234', 1, 1, 'MBC_3_Logo.gif', true, true, 54, NOW(), NOW()),
    (54, 'MBC 4', 'MBC 4', '239.200.0.58', 1234, 'udp://239.200.0.58:1234', 1, 1, 'MBC_4_Logo.gif', true, true, 55, NOW(), NOW()),
    (55, 'MBC Action', 'MBC Action', '239.200.0.59', 1234, 'udp://239.200.0.59:1234', 1, 1, 'MBC_Action_Logo.gif', true, true, 56, NOW(), NOW()),
    (56, 'MBC MAX', 'MBC MAX', '239.200.0.60', 1234, 'udp://239.200.0.60:1234', 1, 1, 'MBC_Max_Logo.gif', true, true, 57, NOW(), NOW()),
    (57, 'MBC Drama', 'MBC Drama', '239.200.0.61', 1234, 'udp://239.200.0.61:1234', 1, 1, 'MBC_Drama_Logo.gif', true, true, 58, NOW(), NOW()),
    (58, 'MBC Bollywood', 'MBC Bollywood', '239.200.0.62', 1234, 'udp://239.200.0.62:1234', 1, 1, 'MBC_Bolywood_Logo.gif', true, true, 59, NOW(), NOW()),
    (59, 'Wanasah', 'Wanasah', '239.200.0.63', 1234, 'udp://239.200.0.63:1234', 1, 1, 'Wanasah_Logo.gif', true, true, 60, NOW(), NOW()),
    (64, 'Chams TV', 'Chams TV', '239.200.0.67', 1234, 'udp://239.200.0.67:1234', 1, 1, 'Chams_TV_Logo.gif', true, true, 62, NOW(), NOW()),
    (65, 'EL BILAD', 'El Bilad TV', '239.200.0.81', 1234, 'udp://239.200.0.81:1234', 1, 1, '', true, true, 63, NOW(), NOW()),
    (66, 'ENNAHAR TV', 'Ennahar TV', '239.200.0.85', 1234, 'udp://239.200.0.85:1234', 1, 1, 'Ennahar_TV_Logo.gif', true, true, 64, NOW(), NOW()),
    (67, 'ELFADJER TV DZ', 'El Fadjer TV', '239.200.0.84', 1234, 'udp://239.200.0.84:1234', 1, 1, '', true, true, 65, NOW(), NOW()),
    (68, 'El Hayat TV', 'El Hayat TV', '239.200.0.86', 1234, 'udp://239.200.0.86:1234', 1, 1, 'alhayat-tv.gif', true, true, 66, NOW(), NOW()),
    (69, 'Algerie TV 6', 'Algeria TV 6', '239.200.0.87', 1234, 'udp://239.200.0.87:1234', 1, 1, 'tv6logo.gif', true, true, 67, NOW(), NOW()),
    (27, 'Algerian TV', 'ENTV 1', '239.200.0.31', 1234, 'udp://239.200.0.31:1234', 1, 3, 'ENTV_1_Logo.gif', true, true, 1, NOW(), NOW()),
    (63, 'Bein Sports', 'Bein Sports News', '239.200.0.70', 1234, 'udp://239.200.0.70:1234', 2, 1, 'beIN_Sports_News_Logo.gif', true, true, 61, NOW(), NOW()),
    (1, 'TF 1', 'TF1', '239.200.0.1', 1234, 'udp://239.200.0.1:1234', 1, 1, 'tf-1_1.png', true, false, 5, NOW(), NOW()),
    (100, 'Big Bunny Test', 'tes stream', '239.0.0.1', 1111, 'udp://239.0.0.1:1111', 1, 1, 'big-bunny-test_100.png', true, true, 100, NOW(), NOW()),
    (3, 'France 5', 'France 5', '239.200.0.6', 1234, 'udp://239.200.0.6:1234', 1, 1, 'france-5_3.png', true, false, 777, NOW(), NOW())
ON CONFLICT (channel_number) DO NOTHING;

-- Important: Synchronize the sequence after data insertion
-- This ensures future auto-generated IDs don't conflict with existing data
SELECT setval('tv_channels_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tv_channels));

-- Verify the setup
SELECT
    'Current sequence value' as info,
    nextval('tv_channels_id_seq') as value
UNION ALL
SELECT
    'Max ID in table' as info,
    MAX(id) as value
FROM tv_channels;

-- Reset sequence to correct value (run the nextval above first to see current state)
SELECT setval('tv_channels_id_seq', (SELECT MAX(id) FROM tv_channels));