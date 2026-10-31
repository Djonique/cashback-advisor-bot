-- ============================================================================
-- V6__seed_sber.sql
-- Загрузка банка Сбербанк: банк, привязка категорий, MCC-коды, исключения.
-- ============================================================================

-- 1. Добавляем банк
INSERT INTO bank (name, slug, display_order)
VALUES ('Сбербанк', 'sber', 4)
ON CONFLICT (slug) DO NOTHING;

-- 2. Привязываем существующие категории к банку
INSERT INTO bank_category (bank_id, category_id)
SELECT b.id, c.id FROM (
    VALUES
    ('supermarkets'),('fuel'),('auto_services'),('fines'),('restaurants'),
    ('fastfood'),('clothes_shoes'),('home_repair'),('kids_goods'),('vet_pets'),
    ('carsharing'),('taxi'),('entertainment'),('cosmetics'),('jewelry'),
    ('digital_goods'),('flowers'),('books'),('education'),('art'), -- Хобби и развлечения частично сюда
    ('beauty_spa'),('transport'),('electronics'),('medical_clinics'),('fitness'),
    ('sport_goods'),('pharmacies'),('hotels') -- Отели на СпасибоТревел
) AS v(category_slug)
JOIN bank b ON b.slug = 'sber'
JOIN category c ON c.slug = v.category_slug
ON CONFLICT (bank_id, category_id) DO NOTHING;

-- 3. Загружаем MCC-коды по категориям
-- Супермаркеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'supermarkets'
CROSS JOIN (VALUES
    ('5309'),('5311'),('5331'),('5399'),('5412'),('5441'),('5451'),('5499'),('5411'),('9751'),('5422'),('5921'),('3990'),('3991')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- АЗС (Топливо)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fuel'
CROSS JOIN (VALUES ('5541'),('5172'),('5542'),('5552'),('5983'),('9752'),('3990'),('3991')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Автоуслуги и Автозапчасти
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'auto_services'
CROSS JOIN (VALUES
    ('5013'),('5271'),('5511'),('5531'),('5532'),('5533'), -- Автозапчасти
    ('7531'),('7534'),('7535'),('7538'),('7549'),('4784'),('7511'),('7523'),('7542'),('5521'),('5551'),('5561'),('5571'),('5592'),('5598'),('5599') -- Автоуслуги
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Штрафы ГИБДД
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '9222' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fines'
ON CONFLICT DO NOTHING;

-- Рестораны (Кафе и рестораны)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'restaurants'
CROSS JOIN (VALUES ('5462'),('5812'),('5811'),('5813'),('3990'),('3991')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фастфуд
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5814' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fastfood'
ON CONFLICT DO NOTHING;

-- Одежда и обувь
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'clothes_shoes'
CROSS JOIN (VALUES ('5137'),('5139'),('5611'),('5621'),('5631'),('5651'),('5661'),('5681'),('5699'),('5948'),('5931'),('5691'),('7296')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Дом, ремонт (Товары для дома)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'home_repair'
CROSS JOIN (VALUES
    ('2842'),('5200'),('5211'),('5231'),('5251'),('5261'),('5712'),('5713'),('5718'),('5719'),('5950'),('5998'),('5996'),('5021'),('5072'),('5074'),('5198'),('0780'),('1520'),('1711'),('1740'),('1750'),('1761'),('1771'),('1799'),('5039'),('5051'),('5714')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Товары для детей
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'kids_goods'
CROSS JOIN (VALUES ('5641'),('5945')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Вет клиники и зоомагазины (Питомцы)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'vet_pets'
CROSS JOIN (VALUES ('0742'),('5995')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Такси
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'taxi'
CROSS JOIN (VALUES ('4121'),('3990'),('3991')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Каршеринг (Аренда авто, Такси и каршеринг)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'carsharing'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3351, 3398) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3400, 3410) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3412, 3423) gs
    UNION ALL SELECT LPAD(gs::text, 4, '0') FROM generate_series(3425, 3439) gs
    UNION ALL SELECT unnest(ARRAY['3441','7512','7513','7519','4121','3990','3991'])
) m
ON CONFLICT DO NOTHING;

-- Развлечения (Кино и театр, Впечатления, Хобби и развлечения)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'entertainment'
CROSS JOIN (VALUES
    ('7832'),('7833'),('7841'),('7922'), -- Кино и театр
    ('7832'),('7922'),('7933'),('7991'),('7996'),('7998'),('7999'), -- Впечатления
    ('4457'),('5832'),('5971'),('7933'),('5972'),('7911'),('7929'),('7932'),('7991'),('7996'),('7998'),('7999'),('5733'),('5735'),('7221'),('7333'),('7395'),('5937'),('5947'),('5970'),('5946'),('5949'),('7993'),('7994') -- Хобби
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Искусство (Хобби и развлечения)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'art'
CROSS JOIN (VALUES
    ('5971'),('5972'),('5970'),('5946'),('5949'),('7993'),('7994')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Косметика (Парфюмерия и косметика)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5977' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'cosmetics'
ON CONFLICT DO NOTHING;

-- Ювелирные изделия
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'jewelry'
CROSS JOIN (VALUES ('5094'),('5944')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цифровые товары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'digital_goods'
CROSS JOIN (VALUES ('5734'),('5735'),('5815'),('5816'),('5817'),('5818')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цветы
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'flowers'
CROSS JOIN (VALUES ('5193'),('5992')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Книги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'books'
CROSS JOIN (VALUES ('2741'),('5111'),('5192'),('5942'),('5943'),('5994')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Образование
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'education'
CROSS JOIN (VALUES ('8211'),('8220'),('8241'),('8244'),('8249'),('8299'),('8351')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Салоны красоты и SPA
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'beauty_spa'
CROSS JOIN (VALUES ('7230'),('7297'),('7298')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Транспорт
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'transport'
CROSS JOIN (VALUES ('4111'),('4131')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Электроника и бытовая техника
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'electronics'
CROSS JOIN (VALUES ('5065'),('5722'),('5732'),('5997')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Медицинские клиники (Медицинские услуги)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'medical_clinics'
CROSS JOIN (VALUES
    ('4119'),('5975'),('5976'),('8011'),('8021'),('8031'),('8041'),('8042'),('8043'),('8044'),('8049'),('8050'),('8062'),('8071'),('8099')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фитнес (Спорт и фитнес, Фитнес)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fitness'
CROSS JOIN (VALUES ('7941'),('7992'),('7997')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Спорттовары (Спорт и фитнес, Спортивные товары)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'sport_goods'
CROSS JOIN (VALUES ('5655'),('5940'),('5941')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Аптеки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'sber'
JOIN category c ON c.id = bc.category_id AND c.slug = 'pharmacies'
CROSS JOIN (VALUES ('5122'),('5912')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- 4. Исключения (Стандартные финансовые, казино, переводы, снятия)
INSERT INTO bank_excluded_mcc (bank_id, mcc_code)
SELECT b.id, v.mcc FROM bank b
CROSS JOIN (VALUES
    ('6010'),('6011'),('6012'),('6050'),('6051'),('6211'),('6513'),
    ('6529'),('6530'),('6531'),('6532'),('6533'),('6534'),('6535'),('6536'),('6537'),('6538'),('6539'),('6540'),
    ('7800'),('7801'),('7802'),('7995'),
    ('9400'),('9401'),('9402'),('9405'),('9406'),('9411'),
    ('9700'),('9701'),('9702')
) AS v(mcc)
WHERE b.slug = 'sber'
ON CONFLICT (bank_id, mcc_code) DO NOTHING;