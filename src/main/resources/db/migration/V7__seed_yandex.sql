-- ============================================================================
-- V7__seed_yandex.sql
-- Загрузка банка Яндекс Банк: банк, привязка категорий, MCC-коды, исключения.
-- ============================================================================

-- 1. Добавляем банк
INSERT INTO bank (name, slug, display_order)
VALUES ('Яндекс Банк', 'yandex', 5)
ON CONFLICT (slug) DO NOTHING;

-- 2. Привязываем существующие категории к банку
INSERT INTO bank_category (bank_id, category_id, local_name)
SELECT b.id, c.id, v.local_name FROM (
    VALUES
    ('all_purchases', 'Все покупки'), ('restaurants', 'Кафе, бары и рестораны'), ('fastfood', 'Фастфуд'), ('education', 'Образование'),
    ('clothes_shoes', 'Одежда и обувь'), ('entertainment', 'Развлечения и Кино'), ('transport', 'Городской транспорт'),
    ('flowers', 'Цветы'), ('pharmacies', 'Аптеки'), ('home_repair', 'Товары для дома'),
    ('beauty_spa', 'Красота'), ('cosmetics', 'Парфюмерия и косметика'), ('fitness', 'Спорт и фитнес'),
    ('sport_goods', 'Спорт и фитнес'), ('medical_clinics', 'Медицина'), ('supermarkets', 'Супермаркеты'),
    ('auto_services', 'Автоуслуги'), ('vet_pets', 'Питомцы'), ('books', 'Книги'),
    ('electronics', 'Электроника и бытовая техника'), ('jewelry', 'Ювелирные изделия'),
    ('kids_goods', 'Товары для детей'), ('fuel', 'АЗС'), ('taxi', 'Яндекс Такси')
) AS v(category_slug, local_name)
JOIN bank b ON b.slug = 'yandex'
JOIN category c ON c.slug = v.category_slug
ON CONFLICT (bank_id, category_id) DO NOTHING;

-- 3. Загружаем MCC-коды по категориям
-- Кафе, бары и рестораны
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'restaurants'
CROSS JOIN (VALUES ('5811'),('5812'),('5813'),('5462')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фастфуд
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5814' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fastfood'
ON CONFLICT DO NOTHING;

-- Образование
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'education'
CROSS JOIN (VALUES ('8211'),('8220'),('8241'),('8244'),('8249'),('8299'),('8351'),('8493'),('8494')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Одежда и обувь
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'clothes_shoes'
CROSS JOIN (VALUES
    ('5137'),('5139'),('5611'),('5621'),('5631'),('5641'),('5651'),('5661'),('5681'),('5691'),('5697'),('5698'),('5699'),('5931'),('5948'),('5949'),('7296')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Развлечения (включая Кино)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'entertainment'
CROSS JOIN (VALUES
    ('4457'),('7012'),('7221'),('7911'),('7922'),('7929'),('7932'),('7933'),('7991'),('7993'),('7994'),('7996'),('7998'),('7999'),('8664'),
    ('7832'),('7841'),('7829') -- Кино
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Транспорт (Городской транспорт, Платные дороги и парковки)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'transport'
CROSS JOIN (VALUES ('4111'),('4131'),('4784'),('7033'),('7523')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Цветы
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5992' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'flowers'
ON CONFLICT DO NOTHING;

-- Аптеки
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'pharmacies'
CROSS JOIN (VALUES ('5122'),('5292'),('5295'),('5912')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Дом, ремонт (Товары для дома)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'home_repair'
CROSS JOIN (VALUES
    ('0780'),('1520'),('1711'),('1731'),('1740'),('1750'),('1761'),('1771'),('2842'),('5021'),('5039'),('5051'),('5072'),('5074'),('5198'),('5200'),('5211'),('5231'),('5251'),('5261'),('5415'),('5712'),('5713'),('5714'),('5718'),('5719'),('5932'),('5950'),('5996'),('7641'),('7692')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Косметика (Парфюмерия и косметика)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5977' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'cosmetics'
ON CONFLICT DO NOTHING;

-- Салоны красоты и SPA (Красота)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'beauty_spa'
CROSS JOIN (VALUES ('5977'),('7230'),('7297'),('7298')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Фитнес (Спорт и фитнес)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fitness'
CROSS JOIN (VALUES ('7941'),('7992'),('7997')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Спорттовары (Спорт и фитнес)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'sport_goods'
CROSS JOIN (VALUES ('5655'),('5940'),('5941')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Медицинские клиники (Медицина)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'medical_clinics'
CROSS JOIN (VALUES
    ('4119'),('5975'),('5976'),('8011'),('8021'),('8031'),('8041'),('8042'),('8043'),('8049'),('8050'),('8062'),('8071'),('8099')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Супермаркеты
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'supermarkets'
CROSS JOIN (VALUES
    ('5297'),('5298'),('5300'),('5309'),('5331'),('5411'),('5412'),('5422'),('5441'),('5451'),('5499'),('5715')
) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Автоуслуги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'auto_services'
CROSS JOIN (VALUES ('5521'),('5531'),('5532'),('5533'),('7531'),('7534'),('7535'),('7538'),('7542')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Вет клиники и зоомагазины (Питомцы)
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'vet_pets'
CROSS JOIN (VALUES ('0742'),('5995')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Книги
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'books'
CROSS JOIN (VALUES ('2741'),('5111'),('5192'),('5942'),('5994')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Электроника и бытовая техника
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'electronics'
CROSS JOIN (VALUES ('5732'),('5065'),('5722'),('7622'),('7623'),('7629'),('7699'),('5997'),('5978'),('7379')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Ювелирные изделия
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'jewelry'
CROSS JOIN (VALUES ('5094'),('5944')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Товары для детей
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '5945' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'kids_goods'
ON CONFLICT DO NOTHING;

-- Топливо и АЗС
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, v.mcc FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'fuel'
CROSS JOIN (VALUES ('5541'),('5172'),('5542'),('5552'),('5983'),('9752')) AS v(mcc)
ON CONFLICT DO NOTHING;

-- Такси
-- 4121 НЕ добавлять: категория «Такси» в Яндекс Банке работает только
-- для Яндекс Такси (MCC 3990). Обычные агрегаторы с 4121 сюда не входят.
INSERT INTO bank_category_mcc (bank_category_id, mcc_code)
SELECT bc.id, '3990' FROM bank_category bc
JOIN bank b ON b.id = bc.bank_id AND b.slug = 'yandex'
JOIN category c ON c.id = bc.category_id AND c.slug = 'taxi'
ON CONFLICT DO NOTHING;

-- 4. Исключения (Огромный список из Приложения 1)
INSERT INTO bank_excluded_mcc (bank_id, mcc_code)
SELECT b.id, v.mcc FROM bank b
CROSS JOIN (VALUES
    ('3882'),('4812'),('4813'),('4814'),('4815'),('4816'),('4821'),('4829'),
    ('6531'),('6534'),('6536'),('6537'),('6538'),('9400'),('4899'),('4900'),
    ('5933'),('5960'),('5968'),('6009'),('6010'),('6011'),('6012'),('6050'),
    ('6051'),('6211'),('6399'),('6529'),('6530'),('6532'),('6533'),('6535'),
    ('6539'),('6540'),('6542'),('7299'),('7311'),('7399'),('7801'),('7802'),
    ('7995'),('9754'),('8641'),('8651'),('8661'),('9211'),('9222'),('9223'),
    ('9311'),('9999'),('8398'),('4468'),('3991'),('8699'),('7800'),('7273'),
    ('7322'),('7511'),('6611'),('9411'),('6740'),('5816'),('5817'),('5818'),
    ('9390'),('6300'),('4722'),('4215'),('9406'),('7277'),('4214'),('9402'),
    ('4789'),('5193'),('5047'),('5131'),('5045'),('5046'),('5044'),('5199'),
    ('5099'),('2791'),('5085'),('5169'),('4011'),('8999'),('8111'),('4225'),
    ('1799'),('7375'),('8911'),('7393'),('7392'),('8391'),('5962'),('5972'),
    ('9399'),('7372')
) AS v(mcc)
WHERE b.slug = 'yandex'
ON CONFLICT (bank_id, mcc_code) DO NOTHING;