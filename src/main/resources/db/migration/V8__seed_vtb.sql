-- ============================================================================
-- V8__seed_vtb.sql
-- Загрузка банка ВТБ: банк, привязка категорий, MCC-коды, исключения.
-- ============================================================================

-- 1. Добавляем банк
INSERT INTO bank (name, slug, display_order)
VALUES ('ВТБ', 'vtb', 6)
ON CONFLICT (slug) DO NOTHING;

-- 2. Привязываем существующие категории к банку
INSERT INTO bank_category (bank_id, category_id, local_name)
SELECT b.id, c.id, v.local_name FROM (
    VALUES
    ('all_purchases', 'Все покупки'), ('supermarkets', 'Супермаркеты'), ('pharmacies', 'Аптеки'), ('medical_clinics', 'Здоровье'),
    ('restaurants', 'Кафе и рестораны'), ('fastfood', 'Фастфуд'), ('clothes_shoes', 'Одежда и обувь'),
    ('kids_goods', 'Детские товары'), ('transport', 'Транспорт'), ('taxi', 'Такси'),
    ('carsharing', 'Аренда авто'), ('beauty_spa', 'Красота'), ('cosmetics', 'Красота'),
    ('electronics', 'Электроника'), ('air_tickets', 'Авиабилеты'), ('duty_free', 'Duty Free'),
    ('hotels', 'Отели'), ('railway_tickets', 'Ж/д билеты'), ('fuel', 'АЗС'),
    ('auto_services', 'Автоуслуги'), ('home_repair', 'Дом и ремонт'), ('education', 'Образование'),
    ('fitness', 'Фитнес'), ('sport_goods', 'Спортивные товары'), ('household_services', 'Бытовые услуги'),
    ('flowers', 'Цветы'), ('jewelry', 'Украшения и бижутерия'), ('vet_pets', 'Зоотовары'),
    ('digital_goods', 'Цифровой контент'), ('entertainment', 'Развлечения и Театры'), ('utilities', 'Услуги ЖКХ'),
    ('alcohol', 'Алкоголь'), ('art', 'Искусство'), ('books', 'Книги и канцтовары'),
    ('telecom', 'Услуги связи')
) AS v(category_slug, local_name)
JOIN bank b ON b.slug = 'vtb'
JOIN category c ON c.slug = v.category_slug
ON CONFLICT (bank_id, category_id) DO NOTHING;

-- 3. Загружаем MCC-коды по категориям
-- Супермаркеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'supermarkets'
CROSS JOIN (VALUES ('5412'),('5462'),('5411'),('5422'),('5441'),('5451'),('5499'),('9751')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Аптеки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'pharmacies'
CROSS JOIN (VALUES ('5122'),('5912'),('5292')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Медицинские клиники (Здоровье)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'medical_clinics'
CROSS JOIN (VALUES
    ('8050'),('4119'),('5975'),('5976'),('8011'),('8021'),('8031'),('8041'),('8042'),('8043'),('8044'),('8049'),('8062'),('8071'),('8099')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Рестораны (Кафе и рестораны)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'restaurants'
CROSS JOIN (VALUES ('5812'),('5811'),('5813')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фастфуд
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5814' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fastfood'
ON CONFLICT DO NOTHING;

-- Одежда и обувь
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'clothes_shoes'
CROSS JOIN (VALUES ('5137'),('5611'),('5621'),('5651'),('5691'),('5699'),('5931'),('5681'),('5948'),('5139'),('5661'),('5631')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Детские товары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'kids_goods'
CROSS JOIN (VALUES ('5945'),('5641')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Транспорт (Городской транспорт, Платные дороги)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'transport'
CROSS JOIN (VALUES ('4111'),('4131'),('4789'),('4784'),('7523')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Такси
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '4121' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'taxi'
ON CONFLICT DO NOTHING;

-- Каршеринг (Аренда авто)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'carsharing'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3351, 3441) gs
    UNION ALL
    SELECT unnest(ARRAY['7513','7519','7512'])
) m
ON CONFLICT DO NOTHING;

-- Салоны красоты и SPA (Красота)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'beauty_spa'
CROSS JOIN (VALUES ('5698'),('7230'),('7297'),('7298'),('5977')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Косметика (Красота)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'cosmetics'
CROSS JOIN (VALUES ('5977')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Электроника и бытовая техника
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'electronics'
CROSS JOIN (VALUES ('5997'),('5946'),('5044'),('5045'),('5722'),('5732')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Авиабилеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'air_tickets'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3001, 3005) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3007, 3010) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3012, 3025) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3027, 3031) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3033, 3036) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3038, 3046) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3049, 3110) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3112, 3135) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3137, 3205) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3207, 3301) gs
) m
ON CONFLICT DO NOTHING;

-- Duty Free
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5309' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'duty_free'
ON CONFLICT DO NOTHING;

-- Отели
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'hotels'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3501, 3836) gs
    UNION ALL
    SELECT unnest(ARRAY['7011','7012','7033','7032','5962','4723','4411'])
) m
ON CONFLICT DO NOTHING;

-- ЖД билеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'railway_tickets'
CROSS JOIN (VALUES ('4011'),('4112')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Топливо и АЗС
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fuel'
CROSS JOIN (VALUES ('5541'),('5172'),('5542'),('5983'),('9752'),('5552')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Автоуслуги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'auto_services'
CROSS JOIN (VALUES ('5531'),('5532'),('5533'),('7531'),('7534'),('7535'),('7538'),('7542'),('7549'),('7511'),('5013'),('5511')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Дом, ремонт
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'home_repair'
CROSS JOIN (VALUES
    ('0780'),('1520'),('1711'),('1731'),('1740'),('1750'),('1761'),('1771'),('1799'),('2842'),('5021'),('5039'),('5051'),('5065'),('5072'),('5074'),('5131'),('5169'),('5198'),('5200'),('5211'),('5231'),('5251'),('5261'),('5311'),('5331'),('5712'),('5713'),('5714'),('5718'),('5719'),('5950'),('5998'),('7622'),('7623'),('7629'),('7641'),('7692'),('7699'),('5996'),('5949')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Образование
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'education'
CROSS JOIN (VALUES ('8211'),('8220'),('8241'),('8244'),('8249'),('8299'),('8351')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фитнес
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fitness'
CROSS JOIN (VALUES ('7941'),('7992'),('7997')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Спорттовары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'sport_goods'
CROSS JOIN (VALUES ('5940'),('5941'),('5655')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Бытовые услуги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'household_services'
CROSS JOIN (VALUES ('5697'),('7210'),('7211'),('7216'),('7217'),('7251'),('7342'),('7349')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цветы
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'flowers'
CROSS JOIN (VALUES ('5193'),('5992')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Ювелирные изделия
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'jewelry'
CROSS JOIN (VALUES ('5094'),('5944'),('7631')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Вет клиники и зоомагазины (Зоотовары)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'vet_pets'
CROSS JOIN (VALUES ('0742'),('5995')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цифровые товары (Цифровой контент)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'digital_goods'
CROSS JOIN (VALUES ('5734'),('5735'),('5815'),('5816'),('5817'),('5818'),('7829'),('7841'),('7993'),('7994')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Развлечения (Развлечения, Театры и кино)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'entertainment'
CROSS JOIN (VALUES ('5733'),('5947'),('5968'),('7221'),('7333'),('7395'),('7911'),('7932'),('7933'),('7991'),('7996'),('7998'),('7999'),('7929'),('7832'),('7833'),('7922')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- ЖКХ
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '4900' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'utilities'
ON CONFLICT DO NOTHING;

-- Алкоголь
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'alcohol'
CROSS JOIN (VALUES ('5715'),('5921')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Искусство
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'art'
CROSS JOIN (VALUES ('5973'),('5937'),('5970'),('5971'),('5972'),('5932')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Книги и канцтовары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'books'
CROSS JOIN (VALUES ('5111'),('5943'),('2741'),('5192'),('5942'),('5994')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Связь и интернет
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'vtb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'telecom'
CROSS JOIN (VALUES ('4899'),('4813'),('4815'),('4821'),('4816'),('4814')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- 4. Исключения (Список "Не учитываются во всех случаях" из PDF)
INSERT INTO bank_excluded_mcc (bank_id, mcc_code)
SELECT b.id, v.mcc FROM bank b
CROSS JOIN (VALUES
    ('6538'),('6050'),('6051'),('6010'),('6011'),('6012'),('7995'),('6211'),('6532'),('6536'),
    ('6537'),('6540'),('9311'),('9211'),('9222'),('6529'),('4812'),('4829'),('6531'),('6533'),
    ('6534'),('8398'),('4215'),('5933'),('6530'),('7299'),('7311'),('7372'),('7399'),('8999'),
    ('9399'),('9754'),('7802'),('7276'),('9390'),('9223'),('5046'),('5047'),('5085'),('5960'),
    ('7389'),('7801'),('8641'),('8651'),('8661'),('6535'),('6009'),('6381'),('6399')
) AS v(mcc)
WHERE b.slug = 'vtb'
ON CONFLICT (bank_id, mcc_code) DO NOTHING;