-- ============================================================================
-- V9__seed_wb.sql
-- Загрузка банка ВБ Банк (Wildberries): банк, привязка категорий, MCC-коды, исключения.
-- ============================================================================

-- 1. Добавляем банк
INSERT INTO bank (name, slug, display_order)
VALUES ('ВБ Банк', 'wb', 7)
ON CONFLICT (slug) DO NOTHING;

-- 2. Привязываем существующие категории к банку
INSERT INTO bank_category (bank_id, category_id)
SELECT b.id, c.id FROM (
    VALUES
    ('auto_services'),('fuel'),('transport'),('home_repair'),('household_services'),
    ('vet_pets'),('supermarkets'),('restaurants'),('fastfood'),('entertainment'),
    ('art'),('digital_goods'),('books'),('education'),('sport_goods'),
    ('fitness'),('pharmacies'),('beauty_spa'),('medical_clinics'),('air_tickets'),
    ('railway_tickets'),('hotels'),('duty_free'),('taxi'),('carsharing'),
    ('clothes_shoes'),('kids_goods'),('electronics'),('jewelry'),('flowers'),
    ('marketplaces')
) AS v(category_slug)
JOIN bank b ON b.slug = 'wb'
JOIN category c ON c.slug = v.category_slug
ON CONFLICT (bank_id, category_id) DO NOTHING;

-- 3. Загружаем MCC-коды по категориям
-- Автоуслуги и автозапчасти
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'auto_services'
CROSS JOIN (VALUES
    ('5013'),('5271'),('5511'),('5521'),('5531'),('5532'),('5533'),('5551'),('5561'),('5571'),('5592'),('5598'),('5599'),('7531'),('7534'),('7535'),('7538'),('7542'),('7549')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Заправки (Топливо)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fuel'
CROSS JOIN (VALUES ('5172'),('5541'),('5542'),('5552'),('5983'),('3990')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Платные дороги и парковки (Транспорт)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'transport'
CROSS JOIN (VALUES ('4784'),('7523'),('4111'),('4131'),('4789')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Дом и ремонт
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'home_repair'
CROSS JOIN (VALUES
    ('0780'),('1520'),('1711'),('1731'),('1740'),('1750'),('1761'),('1771'),('1799'),('2842'),('5021'),('5039'),('5046'),('5051'),('5072'),('5074'),('5085'),('5998'),('5198'),('5200'),('5211'),('5231'),('5251'),('5261'),('5415'),('5712'),('5713'),('5714'),('5718'),('5719'),('5950'),('5996'),('7692'),('7699')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Бытовые услуги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'household_services'
CROSS JOIN (VALUES
    ('7210'),('7211'),('7216'),('7217'),('7251'),('7342'),('7349'),('7394'),('7622'),('7623'),('7629'),('7641')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Питомцы (Вет клиники)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'vet_pets'
CROSS JOIN (VALUES ('0742'),('5995')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Продукты (Супермаркеты)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'supermarkets'
CROSS JOIN (VALUES
    ('5310'),('5311'),('5331'),('5411'),('5412'),('5422'),('5441'),('5451'),('5462'),('5499'),('5921'),('3990'),('3991')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Кафе и рестораны
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'restaurants'
CROSS JOIN (VALUES ('5811'),('5812'),('5813'),('3990')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фастфуд
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5814' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fastfood'
ON CONFLICT DO NOTHING;

-- Впечатления и Театры/Кино (Развлечения)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'entertainment'
CROSS JOIN (VALUES
    ('4457'),('7929'),('7932'),('7933'),('7991'),('7994'),('7996'),('7998'),('7999'),
    ('7832'),('7922')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Хобби и творчество, Искусство (Искусство)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'art'
CROSS JOIN (VALUES
    ('5947'),('5949'),('5733'),('5735'),('5970'),('5972'),('7221'),('7333'),('7395'),
    ('5932'),('5937'),('5971'),('5973')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цифровые подписки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'digital_goods'
CROSS JOIN (VALUES ('5734'),('5815'),('5816'),('5817'),('5818'),('7829'),('7841'),('3991')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Книги и канцтовары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'books'
CROSS JOIN (VALUES ('2741'),('5111'),('5192'),('5942'),('5943'),('5994')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Образование
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'education'
CROSS JOIN (VALUES ('7277'),('8211'),('8220'),('8241'),('8244'),('8249'),('8299'),('8351'),('3990')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Спортивные товары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'sport_goods'
CROSS JOIN (VALUES ('5655'),('5940'),('5941')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фитнес
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fitness'
CROSS JOIN (VALUES ('7911'),('7941'),('7992'),('7997')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Аптеки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'pharmacies'
CROSS JOIN (VALUES ('5122'),('5912'),('5975'),('5976'),('8043'),('8044')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Красота
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'beauty_spa'
CROSS JOIN (VALUES ('5698'),('5977'),('7230'),('7297'),('7298')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Медицинские услуги (Медицинские клиники)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'medical_clinics'
CROSS JOIN (VALUES
    ('4119'),('5047'),('8011'),('8021'),('8031'),('8041'),('8042'),('8049'),('8050'),('8062'),('8071'),('8099')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Авиабилеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'air_tickets'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3000, 3302) gs
    UNION ALL
    SELECT unnest(ARRAY['4511','4582'])
) m
ON CONFLICT DO NOTHING;

-- Ж/д билеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'railway_tickets'
CROSS JOIN (VALUES ('4011'),('4112')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Отели и турагентства
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'hotels'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3501, 3836) gs
    UNION ALL
    SELECT unnest(ARRAY['4411','4722','5962','7011','7012','7032','7033'])
) m
ON CONFLICT DO NOTHING;

-- Duty free
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5309' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'duty_free'
ON CONFLICT DO NOTHING;

-- Такси
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'taxi'
CROSS JOIN (VALUES ('4121'),('3990')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Аренда авто (Каршеринг)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, m.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'carsharing'
CROSS JOIN (
    SELECT LPAD(gs::text, 4, '0') AS mcc FROM generate_series(3351, 3441) gs
    UNION ALL
    SELECT unnest(ARRAY['7512','7513','7519','3991'])
) m
ON CONFLICT DO NOTHING;

-- Одежда и обувь
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'clothes_shoes'
CROSS JOIN (VALUES
    ('5131'),('5137'),('5139'),('5611'),('5621'),('5631'),('5651'),('5661'),('5681'),('5691'),('5697'),('5699'),('5931'),('5948'),('7296')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Детские товары
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'kids_goods'
CROSS JOIN (VALUES ('5641'),('5945')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Электроника и техника
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'electronics'
CROSS JOIN (VALUES
    ('5044'),('5045'),('5065'),('5722'),('5732'),('5946'),('5978'),('5997'),('7379'),('7993')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Ювелирные украшения
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'jewelry'
CROSS JOIN (VALUES ('5094'),('5944'),('7631')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цветы
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'flowers'
CROSS JOIN (VALUES ('5193'),('5992')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Маркетплейсы
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'wb'
JOIN category c ON c.id = bc.category_id AND c.slug = 'marketplaces'
CROSS JOIN (VALUES ('5262'),('5300'),('5964'),('3990'),('3991')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- 4. Исключения (Список МСС-кодов, за операции в которых не начисляются Баллы)
INSERT INTO bank_excluded_mcc (bank_id, mcc_code)
SELECT b.id, v.mcc FROM bank b
CROSS JOIN (VALUES
    ('3882'),('4785'),('4812'),('4813'),('4814'),('4815'),('4816'),('4829'),
    ('4899'),('4900'),('5933'),('5960'),('6009'),('6010'),('6011'),('6012'),
    ('6050'),('6051'),('6211'),('6300'),('6381'),('6399'),('6529'),('6530'),
    ('6531'),('6532'),('6533'),('6534'),('6535'),('6536'),('6537'),('6538'),
    ('6540'),('6611'),('6740'),('6760'),('7299'),('7311'),('7322'),('7372'),
    ('7399'),('7995'),('8398'),('8641'),('8651'),('8661'),('8675'),('8699'),
    ('8931'),('8999'),('9211'),('9222'),('9223'),('9311'),('9390'),('9399'),
    ('9400'),('9401'),('9405'),('9406'),('9411'),('9994'),('9995'),('9998')
) AS v(mcc)
WHERE b.slug = 'wb'
ON CONFLICT (bank_id, mcc_code) DO NOTHING;