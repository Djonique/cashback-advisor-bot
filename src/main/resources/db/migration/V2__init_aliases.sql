-- ============================================================================
-- V2__init_aliases.sql
-- Создание таблицы алиасов, базового справочника категорий и загрузка синонимов
-- ============================================================================


-- 1. Загружаем базовый справочник из 42 категорий (глобальные названия)
INSERT INTO category (name, slug) VALUES
('Образование',                 'education'),
('Авиабилеты',                  'air_tickets'),
('ЖД билеты',                   'railway_tickets'),
('Отели',                       'hotels'),
('Транспорт',                   'transport'),
('Такси',                       'taxi'),
('Каршеринг',                   'carsharing'),
('Автоуслуги',                  'auto_services'),
('Топливо и АЗС',               'fuel'),
('Аптеки',                      'pharmacies'),
('Салоны красоты и SPA',        'beauty_spa'),
('Косметика',                   'cosmetics'),
('Дом, ремонт',                 'home_repair'),
('Искусство',                   'art'),
('Развлечения',                 'entertainment'),
('Выставки и музеи',            'museums'),
('Фитнес',                      'fitness'),
('Медицинские клиники',         'medical_clinics'),
('Музыкальные инструменты',     'musical_instruments'),
('Супермаркеты',                'supermarkets'),
('Электроника и бытовая техника','electronics'),
('Рестораны',                   'restaurants'),
('Фастфуд',                     'fastfood'),
('Химчистки',                   'dry_cleaning'),
('Фото/Видео',                  'photo_video'),
('Цветы',                       'flowers'),
('Спорттовары',                 'sport_goods'),
('Одежда, обувь',               'clothes_shoes'),
('Книги',                       'books'),
('Товары для детей',            'kids_goods'),
('Ювелирные изделия',           'jewelry'),
('Вет клиники и зоомагазины',   'vet_pets'),
('Маркетплейсы',                'marketplaces'),
('Цифровые товары',             'digital_goods'),
('Алкоголь',                    'alcohol'),
('ЖКХ',                         'utilities'),
('Связь и интернет',            'telecom'),
('Штрафы',                      'fines'),
('Налоги',                      'taxes'),
('Duty Free',                   'duty_free'),
('Транспортные карты',          'transport_cards'),
('Бытовые услуги',              'household_services')
ON CONFLICT (slug) DO NOTHING;

-- 2. Загружаем алиасы
WITH src(slug, alias) AS (
    VALUES
    -- Образование
    ('education', 'Образование'),
    ('education', 'курсы'),
    ('education', 'обучение'),
    -- Авиабилеты
    ('air_tickets', 'Авиабилеты'),
    ('air_tickets', 'Авиа в Тревел'),
    ('air_tickets', 'Авиакомпании'),
    -- ЖД билеты
    ('railway_tickets', 'ЖД билеты'),
    ('railway_tickets', 'Ж/д билеты'),
    ('railway_tickets', 'Ж/Д в Тревел'),
    ('railway_tickets', 'РЖД'),
    ('railway_tickets', 'Поезд'),
    -- Отели
    ('hotels', 'Отели'),
    ('hotels', 'Отели в Тревел'),
    ('hotels', 'Отели на СпасибоТревел'),
    ('hotels', 'Туры на СпасибоТревел'),
    ('hotels', 'Турагентства'),
    ('hotels', 'Отели и турагентства'),
    -- Транспорт
    ('transport', 'Транспорт'),
    ('transport', 'Городской транспорт'),
    ('transport', 'Платные дороги'),
    ('transport', 'Платные дороги и парковки'),
    -- Такси
    ('taxi', 'Такси'),
    ('taxi', 'Такси и каршеринг'),
    -- Каршеринг
    ('carsharing', 'Каршеринг'),
    ('carsharing', 'Аренда авто'),
    ('carsharing', 'Такси и аренда авто'),
    -- Автоуслуги
    ('auto_services', 'Авто'),
    ('auto_services', 'Автоуслуги'),
    ('auto_services', 'Автозапчасти'),
    ('auto_services', 'Автоуслуги и автозапчасти'),
    -- Топливо и АЗС
    ('fuel', 'АЗС'),
    ('fuel', 'Топливо и АЗС'),
    ('fuel', 'Заправки'),
    ('fuel', 'Альфа-Заправки'),
    ('fuel', 'Бензин'),
    -- Аптеки
    ('pharmacies', 'Аптеки'),
    -- Салоны красоты и SPA
    ('beauty_spa', 'Красота'),
    ('beauty_spa', 'Салоны красоты'),
    ('beauty_spa', 'Салоны красоты и SPA'),
    -- Косметика
    ('cosmetics', 'Косметика'),
    ('cosmetics', 'Парфюмерия и косметика'),
    -- Дом, ремонт
    ('home_repair', 'Дом и ремонт'),
    ('home_repair', 'Дом, ремонт'),
    ('home_repair', 'Товары для дома'),
    ('home_repair', 'Ремонт и мебель'),
    -- Искусство
    ('art', 'Искусство'),
    ('art', 'Хобби и творчество'),
    -- Развлечения
    ('entertainment', 'Развлечения'),
    ('entertainment', 'Активный отдых'),
    ('entertainment', 'Хобби и развлечения'),
    ('entertainment', 'Впечатления'),
    ('entertainment', 'Кино'),
    ('entertainment', 'Кино и театр'),
    ('entertainment', 'Театры и кино'),
    ('entertainment', 'Онлайн-кинотеатры'),
    -- Выставки и музеи
    ('museums', 'Выставки и музеи'),
    -- Фитнес
    ('fitness', 'Фитнес'),
    ('fitness', 'Спорт и фитнес'),
    -- Медицинские клиники
    ('medical_clinics', 'Здоровье'),
    ('medical_clinics', 'Медицина'),
    ('medical_clinics', 'Медицинские услуги'),
    ('medical_clinics', 'Медицинские клиники'),
    -- Музыкальные инструменты
    ('musical_instruments', 'Музыкальные инструменты'),
    ('musical_instruments', 'Музыка'),
    -- Супермаркеты
    ('supermarkets', 'Продукты'),
    ('supermarkets', 'Супермаркеты'),
    -- Электроника и бытовая техника
    ('electronics', 'Техника'),
    ('electronics', 'Электроника'),
    ('electronics', 'Электроника и техника'),
    ('electronics', 'Электроника и бытовая техника'),
    -- Рестораны
    ('restaurants', 'Кафе и рестораны'),
    ('restaurants', 'Рестораны'),
    ('restaurants', 'Кафе, рестораны и бары'),
    -- Фастфуд
    ('fastfood', 'Фастфуд'),
    -- Химчистки
    ('dry_cleaning', 'Химчистки'),
    ('dry_cleaning', 'Бытовые услуги'),
    -- Фото/Видео
    ('photo_video', 'Фото/Видео'),
    -- Цветы
    ('flowers', 'Цветы'),
    -- Спорттовары
    ('sport_goods', 'Спортивные товары'),
    ('sport_goods', 'Спорттовары'),
    -- Одежда, обувь
    ('clothes_shoes', 'Одежда и обувь'),
    ('clothes_shoes', 'Одежда, обувь'),
    -- Книги
    ('books', 'Книги'),
    ('books', 'Книги и канцтовары'),
    -- Товары для детей
    ('kids_goods', 'Товары для детей'),
    ('kids_goods', 'Детские товары'),
    -- Ювелирные изделия
    ('jewelry', 'Ювелирные изделия'),
    ('jewelry', 'Ювелирные украшения'),
    ('jewelry', 'Украшения и бижутерия'),
    -- Вет клиники и зоомагазины
    ('vet_pets', 'Животные'),
    ('vet_pets', 'Питомцы'),
    ('vet_pets', 'Зоотовары'),
    ('vet_pets', 'Вет клиники и зоомагазины'),
    -- Прочее
    ('marketplaces', 'Маркетплейсы'),
    ('digital_goods', 'Цифровые товары'),
    ('alcohol', 'Алкоголь'),
    ('utilities', 'ЖКХ'),
    ('telecom', 'Связь и интернет'),
    ('fines', 'Штрафы'),
    ('taxes', 'Налоги'),
    ('duty_free', 'Duty Free'),
    ('transport_cards', 'Транспортные карты'),
    ('household_services', 'Бытовые услуги')
)
INSERT INTO category_alias (category_id, alias)
SELECT c.id, lower(btrim(s.alias))
FROM src s
JOIN category c ON c.slug = s.slug
ON CONFLICT DO NOTHING;