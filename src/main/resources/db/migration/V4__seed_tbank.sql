-- ============================================================================
-- V4__seed_tbank.sql
-- Загрузка банка Т-Банк (Тинькофф): банк, привязка категорий, MCC-коды.
-- ============================================================================

-- 1. Добавляем банк
INSERT INTO bank (name, slug, display_order)
VALUES ('Т-Банк', 'tbank', 2)
ON CONFLICT (slug) DO NOTHING;

-- 2. Привязываем существующие категории к банку
INSERT INTO bank_category (bank_id, category_id, local_name)
SELECT b.id, c.id, v.local_name FROM (
    VALUES
    ('auto_services', 'Автоуслуги'), ('fuel', 'Заправки'), ('transport', 'Платные дороги'),
    ('vet_pets', 'Животные'), ('home_repair', 'Ремонт и мебель'), ('entertainment', 'Кино и развлечения'),
    ('digital_goods', 'Цифровые товары'), ('cosmetics', 'Косметика'), ('beauty_spa', 'Красота'),
    ('pharmacies', 'Аптеки'), ('restaurants', 'Рестораны'), ('supermarkets', 'Супермаркеты'),
    ('fastfood', 'Фастфуд'), ('art', 'Искусство'), ('musical_instruments', 'Музыка'),
    ('education', 'Образование'), ('books', 'Книги и канцтовары'), ('air_tickets', 'Авиабилеты'),
    ('railway_tickets', 'Ж/д билеты'), ('duty_free', 'Duty Free')
) AS v(category_slug, local_name)
JOIN bank b ON b.slug = 'tbank'
JOIN category c ON c.slug = v.category_slug
ON CONFLICT (bank_id, category_id) DO NOTHING;

-- 3. Загружаем MCC-коды по категориям
-- Автоуслуги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'auto_services'
CROSS JOIN (VALUES
    ('5013'),('5531'),('5532'),('5533'),('5571'),('7523'),('7531'),('7534'),('7535'),('7538'),('7542'),('7549')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Топливо и АЗС (Заправки)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fuel'
CROSS JOIN (VALUES ('5172'),('5541'),('5542'),('5552'),('5983')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Транспорт (Платные дороги)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '4784' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'transport'
ON CONFLICT DO NOTHING;

-- Вет клиники и зоомагазины (Животные)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'vet_pets'
CROSS JOIN (VALUES ('5995'),('0742')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Дом, ремонт (Ремонт и мебель)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'home_repair'
CROSS JOIN (VALUES
    ('1520'),('1711'),('1731'),('1740'),('1750'),('1761'),('1771'),('1799'),('2842'),
    ('5021'),('5039'),('5046'),('5051'),('5065'),('5072'),('5074'),('5085'),('5198'),
    ('5200'),('5211'),('5231'),('5251'),('5261'),('5415'),('5712'),('5713'),('5714'),
    ('5718'),('5719'),('5950'),('7622'),('7623'),('7629'),('7641'),('7692'),('7699')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Развлечения (Кино, Онлайн-кинотеатры, Развлечения)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'entertainment'
CROSS JOIN (VALUES
    ('7829'),('7832'),('7841'), -- Кино и Онлайн-кинотеатры
    ('7929'),('7932'),('7933'),('7993'),('7994'),('7996'),('7998'),('7999'),('8664') -- Развлечения
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цифровые товары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'digital_goods'
CROSS JOIN (VALUES ('0019'),('5815'),('5816'),('5817'),('5818')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Косметика
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5977' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'cosmetics'
ON CONFLICT DO NOTHING;

-- Салоны красоты и SPA (Красота)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'beauty_spa'
CROSS JOIN (VALUES ('5977'),('7230'),('7297'),('7298')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Аптеки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'pharmacies'
CROSS JOIN (VALUES ('5122'),('5292'),('5295'),('5912')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Рестораны
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'restaurants'
CROSS JOIN (VALUES ('5811'),('5812'),('5813')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Супермаркеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'supermarkets'
CROSS JOIN (VALUES
    ('5297'),('5298'),('5411'),('5412'),('5422'),('5441'),('5451'),('5462'),('5499'),('5715'),('5921')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фастфуд
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5814' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fastfood'
ON CONFLICT DO NOTHING;

-- Искусство
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'art'
CROSS JOIN (VALUES ('5932'),('5937'),('5971'),('5973'),('7922'),('7991')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Музыкальные инструменты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'musical_instruments'
CROSS JOIN (VALUES ('5733'),('5735')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Образование
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'education'
CROSS JOIN (VALUES
    ('8211'),('8220'),('8241'),('8244'),('8249'),('8299'),('8493'),('8494'),('8351')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Книги (Книги и канцтовары)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'books'
CROSS JOIN (VALUES ('2741'),('5111'),('5192'),('5942'),('5943'),('5994')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Авиабилеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'air_tickets'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3000, 3303) gs
    UNION ALL
    SELECT unnest(ARRAY['3308','3350','4304','4415','4418','4511','4582'])
) m
ON CONFLICT DO NOTHING;

-- ЖД билеты (Ж/д билеты)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'railway_tickets'
CROSS JOIN (VALUES ('4011'),('4112')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Duty Free
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5309' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'tbank'
JOIN category c ON c.id = bc.category_id AND c.slug = 'duty_free'
ON CONFLICT DO NOTHING;