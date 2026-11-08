-- ============================================================================
-- V3__seed_ozon.sql
-- Загрузка банка Ozon: банк, привязка категорий, MCC-коды, исключения.
-- ============================================================================

-- 1. Добавляем банк
INSERT INTO bank (name, slug, display_order)
VALUES ('Ozon Банк', 'ozon', 1)
ON CONFLICT (slug) DO NOTHING;

-- 2. Привязываем существующие категории к банку
INSERT INTO bank_category (bank_id, category_id, local_name)
SELECT b.id, c.id, v.local_name FROM (
    VALUES
    ('all_purchases', 'Все покупки'), ('education', 'Образование'), ('air_tickets', 'Авиабилеты'), ('railway_tickets', 'ЖД билеты'),
    ('hotels', 'Отели'), ('transport', 'Транспорт'), ('taxi', 'Такси'),
    ('carsharing', 'Каршеринг'), ('auto_services', 'Автоуслуги'), ('fuel', 'Топливо и АЗС'),
    ('pharmacies', 'Аптеки'), ('beauty_spa', 'Салоны красоты и SPA'), ('cosmetics', 'Косметика'),
    ('home_repair', 'Дом, ремонт'), ('art', 'Искусство'), ('entertainment', 'Развлечения'),
    ('museums', 'Выставки и музеи'), ('fitness', 'Фитнес'), ('medical_clinics', 'Медицинские клиники'),
    ('musical_instruments', 'Музыкальные инструменты'), ('supermarkets', 'Супермаркеты'),
    ('electronics', 'Электроника и бытовая техника'), ('restaurants', 'Рестораны'), ('fastfood', 'Фастфуд'),
    ('dry_cleaning', 'Химчистки'), ('photo_video', 'Фото/Видео'), ('flowers', 'Цветы'),
    ('sport_goods', 'Спорттовары'), ('clothes_shoes', 'Одежда, обувь'), ('books', 'Книги'),
    ('kids_goods', 'Товары для детей'), ('jewelry', 'Ювелирные изделия'), ('vet_pets', 'Вет клиники и зоомагазины')
) AS v(category_slug, local_name)
JOIN bank b ON b.slug = 'ozon'
JOIN category c ON c.slug = v.category_slug
ON CONFLICT (bank_id, category_id) DO NOTHING;

-- 3. Загружаем MCC-коды по категориям
-- Образование
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'education'
CROSS JOIN (VALUES ('8211'),('8220'),('8241'),('8244'),('8249'),('8299'),('8351')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Авиабилеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'air_tickets'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3000, 3303) gs
    UNION ALL
    SELECT unnest(ARRAY['3308','4304','4415','4418','4511','4582'])
) m
ON CONFLICT DO NOTHING;

-- ЖД билеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'railway_tickets'
CROSS JOIN (VALUES ('4011'),('4112')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Отели
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'hotels'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3501, 3827) gs
    UNION ALL
    SELECT LPAD(gs::text, 4, '0') FROM generate_series(3830, 3838) gs
    UNION ALL
    SELECT unnest(ARRAY['4722','7011','7032','7033'])
) m
ON CONFLICT DO NOTHING;

-- Транспорт
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'transport'
CROSS JOIN (VALUES ('4111'),('4131'),('4784'),('4789'),('7519'),('7523')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Такси
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '4121' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'taxi'
ON CONFLICT DO NOTHING;

-- Каршеринг
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '7512' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'carsharing'
ON CONFLICT DO NOTHING;

-- Автоуслуги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'auto_services'
CROSS JOIN (VALUES
    ('5013'),('5511'),('5521'),('5531'),('5532'),('5533'),('5571'),('5599'),
    ('7531'),('7534'),('7535'),('7538'),('7542'),('7549'),('8676')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Топливо и АЗС
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fuel'
CROSS JOIN (VALUES ('5172'),('5541'),('5542'),('5552'),('5983')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Аптеки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'pharmacies'
CROSS JOIN (VALUES ('5122'),('5912')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Салоны красоты и SPA
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'beauty_spa'
CROSS JOIN (VALUES ('7230'),('7297'),('7298')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Косметика
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'cosmetics'
CROSS JOIN (VALUES ('5311'),('5977')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Дом, ремонт
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'home_repair'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(5712, 5714) gs
    UNION ALL
    SELECT LPAD(gs::text, 4, '0') FROM generate_series(5718, 5719) gs
    UNION ALL
    SELECT LPAD(gs::text, 4, '0') FROM generate_series(7622, 7623) gs
    UNION ALL
    SELECT unnest(ARRAY[
        '0780','1520','1711','1731','1740','1750','1761','1771',
        '5021','5039','5046','5051','5065','5072','5074','5085',
        '5198','5200','5211','5231','5251','5261','5950','7379',
        '7629','7641','7692','7699'
        ])
) m
ON CONFLICT DO NOTHING;

-- Искусство
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'art'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(5970, 5973) gs
    UNION ALL
    SELECT unnest(ARRAY['5932','5937','5949'])
) m
ON CONFLICT DO NOTHING;

-- Развлечения
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'entertainment'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(7932, 7933) gs
    UNION ALL
    SELECT LPAD(gs::text, 4, '0') FROM generate_series(7998, 7999) gs
    UNION ALL
    SELECT unnest(ARRAY['5816','7829','7832','7841','7911','7922','7929','7992','7993','7994','7996'])
) m
ON CONFLICT DO NOTHING;

-- Выставки и музеи
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '7991' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'museums'
ON CONFLICT DO NOTHING;

-- Фитнес
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fitness'
CROSS JOIN (VALUES ('7941'),('7997')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Медицинские клиники
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'medical_clinics'
CROSS JOIN (VALUES
    ('4119'),('5047'),('5975'),('5976'),('8011'),('8021'),('8031'),('8041'),('8042'),('8043'),('8044'),
    ('8049'),('8050'),('8062'),('8071'),('8099')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Музыкальные инструменты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'musical_instruments'
CROSS JOIN (VALUES ('5733'),('5735')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Супермаркеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'supermarkets'
CROSS JOIN (VALUES ('5411'),('5412'),('5422'),('5441'),('5451'),('5462'),('5499'),('5921')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Электроника и бытовая техника
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'electronics'
CROSS JOIN (VALUES ('5722'),('5732')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Рестораны
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, LPAD(gs::text, 4, '0') FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'restaurants'
CROSS JOIN generate_series(5811, 5813) gs
ON CONFLICT DO NOTHING;

-- Фастфуд
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5814' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fastfood'
ON CONFLICT DO NOTHING;

-- Химчистки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '7216' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'dry_cleaning'
ON CONFLICT DO NOTHING;

-- Фото/Видео
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'photo_video'
CROSS JOIN (VALUES ('5045'),('5544'),('7332'),('7333'),('7338'),('7339'),('7395')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цветы
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'flowers'
CROSS JOIN (VALUES ('5193'),('5992')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Спорттовары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'sport_goods'
CROSS JOIN (VALUES ('5655'),('5940'),('5941')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Одежда, обувь
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'clothes_shoes'
CROSS JOIN (VALUES
    ('5137'),('5139'),('5611'),('5621'),('5631'),('5651'),('5661'),('5681'),('5691'),('5599'),('5931')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Книги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'books'
CROSS JOIN (VALUES ('2741'),('5111'),('5192'),('5942'),('5994')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Товары для детей
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'kids_goods'
CROSS JOIN (VALUES ('5641'),('5945')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Ювелирные изделия
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'jewelry'
CROSS JOIN (VALUES ('5094'),('5944')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Вет клиники и зоомагазины
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'ozon'
JOIN category c ON c.id = bc.category_id AND c.slug = 'vet_pets'
CROSS JOIN (VALUES ('0742'),('5995')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- 4. Исключения (МСС, по которым нельзя получить кешбэк)
INSERT INTO bank_excluded_mcc (bank_id, mcc_code)
SELECT b.id, v.mcc FROM bank b
CROSS JOIN (VALUES
    ('0763'),('1799'),('2791'),('2842'),('4215'),('4225'),('4812'),('4813'),('4814'),('4815'),('4816'),('4821'),('4829'),
    ('4899'),('4900'),('5169'),('5199'),('5271'),('5416'),('5417'),('5933'),('5935'),('5960'),('5962'),('6009'),('6010'),
    ('6011'),('6012'),('6022'),('6023'),('6025'),('6026'),('6028'),('6050'),('6051'),('6211'),('6300'),('6381'),('6399'),
    ('6513'),('6529'),('6530'),('6531'),('6532'),('6533'),('6534'),('6535'),('6536'),('6537'),('6538'),('6539'),('6540'),
    ('6555'),('6611'),('6760'),('7012'),('7273'),('7276'),('7277'),('7280'),('7299'),('7311'),('7321'),('7322'),('7372'),
    ('7375'),('7389'),('7392'),('7393'),('7399'),('7511'),('7800'),('7801'),('7802'),('7995'),('8111'),('8398'),('8641'),
    ('8651'),('8661'),('8675'),('8699'),('8734'),('8911'),('8931'),('8999'),('9211'),('9222'),('9223'),('9311'),('9312'),
    ('9313'),('9314'),('9390'),('9399'),('9400'),('9401'),('9402'),('9405'),('9406'),('9411'),('9700'),('9701'),('9702'),
    ('9751'),('9752'),('9950')
) AS v(mcc)
WHERE b.slug = 'ozon'
ON CONFLICT (bank_id, mcc_code) DO NOTHING;