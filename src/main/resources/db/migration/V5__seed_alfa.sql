-- ============================================================================
-- V5__seed_alfa.sql
-- Загрузка банка Альфа-Банк: банк, привязка категорий, MCC-коды, исключения.
-- ============================================================================

-- 1. Добавляем банк
INSERT INTO bank (name, slug, display_order)
VALUES ('Альфа-Банк', 'alfa', 3)
ON CONFLICT (slug) DO NOTHING;

-- 2. Привязываем существующие категории к банку
INSERT INTO bank_category (bank_id, category_id, local_name)
SELECT b.id, c.id, v.local_name FROM (
    VALUES
    ('all_purchases', 'Все покупки'), ('auto_services', 'Авто'), ('fuel', 'АЗС'), ('fitness', 'Активный отдых'),
    ('entertainment', 'Развлечения'), ('alcohol', 'Алкоголь'), ('pharmacies', 'Аптеки'),
    ('carsharing', 'Аренда авто'), ('kids_goods', 'Детские товары'), ('home_repair', 'Дом и ремонт'),
    ('vet_pets', 'Животные'), ('medical_clinics', 'Здоровье'), ('restaurants', 'Кафе и рестораны'),
    ('books', 'Книги'), ('beauty_spa', 'Красота'), ('marketplaces', 'Маркетплейсы'),
    ('taxes', 'Налоги'), ('fines', 'Штрафы ГАИ'), ('education', 'Образование'),
    ('clothes_shoes', 'Одежда и обувь'), ('supermarkets', 'Продукты'), ('telecom', 'Связь, интернет и ТВ'),
    ('sport_goods', 'Спортивные товары'), ('taxi', 'Такси'), ('electronics', 'Техника'),
    ('transport', 'Транспорт'), ('transport_cards', 'Транспортные карты'), ('fastfood', 'Фастфуд'),
    ('flowers', 'Цветы'), ('digital_goods', 'Цифровые товары'), ('jewelry', 'Ювелирные изделия'),
    ('air_tickets', 'Авиа в Тревел'), ('hotels', 'Отели в Тревел'), ('railway_tickets', 'Ж/Д в Тревел')
) AS v(category_slug, local_name)
JOIN bank b ON b.slug = 'alfa'
JOIN category c ON c.slug = v.category_slug
ON CONFLICT (bank_id, category_id) DO NOTHING;

-- 3. Загружаем MCC-коды по категориям
-- Авто (Автоуслуги, Автозапчасти, Авто)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'auto_services'
CROSS JOIN (VALUES
    ('4784'),('5013'),('5271'),('5531'),('5532'),('5533'),('5571'),
    ('7511'),('7523'),('7531'),('7534'),('7535'),('7538'),('7542'),('7549')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- АЗС (Топливо)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fuel'
CROSS JOIN (VALUES ('5172'),('5541'),('5542'),('5552'),('5983'),('9752')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фитнес (Активный отдых - спортклубы)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fitness'
CROSS JOIN (VALUES ('7941'),('7997')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Развлечения (Активный отдых, Развлечения)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'entertainment'
CROSS JOIN (VALUES
    ('7032'),('7932'),('7933'),('7996'),('7998'),('7999'),('7992'), -- Активный отдых
    ('5946'),('5947'),('5949'),('5998'),('7221'),('7395'),('7993'),('7994'),
    ('5970'),('5971'),('5972'),('7911'),('7922'),('7991'),('7829'),('7832'),('7841'),('5733'),('5735'),('7929') -- Развлечения
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Алкоголь
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5921' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'alcohol'
ON CONFLICT DO NOTHING;

-- Аптеки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'pharmacies'
CROSS JOIN (VALUES ('5122'),('5912'),('5975'),('5976'),('8044')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Каршеринг (Аренда авто)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'carsharing'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3351, 3398) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3400, 3410) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3412, 3423) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3425, 3439) gs
    UNION ALL SELECT unnest(ARRAY['3441','7512','7513','7519'])
) m
ON CONFLICT DO NOTHING;

-- Товары для детей
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'kids_goods'
CROSS JOIN (VALUES ('5641'),('5945')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Дом, ремонт
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'home_repair'
CROSS JOIN (VALUES
    ('0780'),('1520'),('1711'),('1731'),('1740'),('1750'),('1761'),('1771'),('2842'),
    ('5021'),('5039'),('5051'),('5072'),('5074'),('5198'),('5200'),('5211'),('5231'),
    ('5251'),('5261'),('5712'),('5713'),('5714'),('5718'),('5719'),('5950'),('5996'),
    ('7217'),('7641'),('7692'),('7699')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Вет клиники и зоомагазины
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'vet_pets'
CROSS JOIN (VALUES ('0742'),('5995')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Медицинские клиники
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'medical_clinics'
CROSS JOIN (VALUES
    ('4119'),('5122'),('5912'),('5975'),('5976'),('8011'),('8021'),('8031'),('8041'),
    ('8042'),('8043'),('8044'),('8049'),('8050'),('8062'),('8071'),('8099')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Рестораны (Кафе и рестораны)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'restaurants'
CROSS JOIN (VALUES ('5811'),('5812'),('5813')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Книги (Книги и канцтовары)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'books'
CROSS JOIN (VALUES ('2741'),('5111'),('5192'),('5942'),('5943'),('5994')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Салоны красоты и SPA (Красота)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'beauty_spa'
CROSS JOIN (VALUES ('5977'),('7230'),('7297'),('7298')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Маркетплейсы
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'marketplaces'
CROSS JOIN (VALUES ('5262'),('5300'),('5399'),('5964')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Образование
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'education'
CROSS JOIN (VALUES ('8211'),('8220'),('8241'),('8244'),('8249'),('8299'),('8351')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Одежда, обувь
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'clothes_shoes'
CROSS JOIN (VALUES
    ('5137'),('5611'),('5621'),('5631'),('5651'),('5681'),('5691'),('5699'),('5931'),
    ('7296'),('5139'),('5661'),('5948'),('7251'),('7631')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Супермаркеты (Продукты)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'supermarkets'
CROSS JOIN (VALUES
    ('5310'),('5311'),('5331'),('5411'),('5422'),('5441'),('5451'),('5462'),('5499'),('9751')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Связь и интернет
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'telecom'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(4813, 4816) gs
    UNION ALL
    SELECT unnest(ARRAY['4899','7372'])
) m
ON CONFLICT DO NOTHING;

-- Спорттовары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'sport_goods'
CROSS JOIN (VALUES ('5655'),('5940'),('5941')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Такси
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '4121' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'taxi'
ON CONFLICT DO NOTHING;

-- Электроника и бытовая техника (Техника)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'electronics'
CROSS JOIN (VALUES
    ('5044'),('5045'),('5065'),('5722'),('5732'),('5978'),('5997'),('7379'),('7622'),('7623'),('7629')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Транспорт
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'transport'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(4012, 4112) gs
    UNION ALL
    SELECT unnest(ARRAY['4131','4729','4789'])
) m
ON CONFLICT DO NOTHING;

-- Фастфуд
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5814' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fastfood'
ON CONFLICT DO NOTHING;

-- Цветы
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'flowers'
CROSS JOIN (VALUES ('5193'),('5992')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цифровые товары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'digital_goods'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(5815, 5818) gs
    UNION ALL
    SELECT unnest(ARRAY['5734','5735'])
) m
ON CONFLICT DO NOTHING;

-- Ювелирные изделия
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'jewelry'
CROSS JOIN (VALUES ('5094'),('5944')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Отели
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '4722' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'alfa'
JOIN category c ON c.id = bc.category_id AND c.slug = 'hotels'
ON CONFLICT DO NOTHING;

-- 4. Исключения (Базовые MCC и специфичные для Альфа-Банка)
INSERT INTO bank_excluded_mcc (bank_id, mcc_code)
SELECT b.id, v.mcc FROM bank b
CROSS JOIN (VALUES
    -- Стандартные финансовые/казино/переводы (из PDF и общих правил)
    ('7800'),('7801'),('7802'),('7995'),('9400'),('9411'),('9700'),('9701'),('9702'),
    ('6010'),('6011'),('6012'),('6050'),('6051'),('6211'),('6513'),
    ('6529'),('6530'),('6531'),('6532'),('6533'),('6534'),('6535'),('6536'),('6537'),('6538'),('6539'),('6540'),
    ('9311'),('9399'),('9402'),('9405'),('9406')
) AS v(mcc)
WHERE b.slug = 'alfa'
ON CONFLICT (bank_id, mcc_code) DO NOTHING;