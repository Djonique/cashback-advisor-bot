-- ============================================================
-- STORE: Магазины, сети и сервисы РФ по категориям
-- ============================================================

CREATE TABLE store (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    aliases VARCHAR(500),
    category_id BIGINT NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    mcc_code CHAR(4) NOT NULL,
    CONSTRAINT ux_store_name UNIQUE (name),
    CONSTRAINT chk_store_mcc_format CHECK (mcc_code ~ '^[0-9]{4}$')
);

CREATE INDEX idx_store_category_id ON store (category_id);

CREATE INDEX idx_store_mcc_code ON store (mcc_code);
COMMENT ON INDEX idx_store_mcc_code IS 'Поиск магазинов по MCC-коду';

CREATE INDEX idx_store_name_trgm ON store USING GIN (lower(name) gin_trgm_ops);
CREATE INDEX idx_store_aliases_trgm ON store USING GIN (lower(aliases) gin_trgm_ops);

INSERT INTO store (name, aliases, category_id, mcc_code) VALUES

-- ============================================================
-- 1. СУПЕРМАРКЕТЫ И ПРОДУКТЫ (MCC 5411 / 5331)
-- ============================================================
('Пятёрочка', 'пятерочка, пятёрочка, pyaterochka', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Магнит', 'магнит, magnit', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Перекрёсток', 'перекресток, perekrestok', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Лента', 'лента, lenta', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Ашан', 'ашан, auchan', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Дикси', 'дикси, dixy', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('ВкусВилл', 'вкусвилл, vkusvill', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('О’КЕЙ', 'окей, okey', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Монетка', 'монетка, monetka', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Верный', 'верный, verniy', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Азбука Вкуса', 'азбука вкуса, azbuka vkusa', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('METRO', 'метро, metro, cash and carry', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Fix Price', 'фикс прайс, fix price', (SELECT id FROM category WHERE slug = 'supermarkets'), '5331'),
('Галамарт', 'галамарт, galamart', (SELECT id FROM category WHERE slug = 'home_repair'), '5331'),
('Светофор', 'светофор, svetofor', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Ярче!', 'ярче, yarche', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Бахетле', 'бахетле, bahetle', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Самбери', 'самбери, samberi', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Красный Яр', 'красный яр, krasny yar', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Мария-Ра', 'мария-ра, mariya-ra', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Холидей', 'холидей, holiday', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Доброцен', 'доброцен, dobrotsen', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Чижик', 'чижик, chizhik', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Карусель', 'карусель, karusel', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),
('Глобус', 'глобус, globus', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),

-- ============================================================
-- 2. ФАСТФУД (MCC 5814)
-- ============================================================
('Макдоналдс', 'макдоналдс, mcdonalds, вкусно и точка', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('KFC', 'kfc, кфс, ростикс', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Бургер Кинг', 'бургер кинг, burger king', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Вкусно — и точка', 'вкусно и точка, vkusno i tochka', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Rostic’s', 'ростикс, rostics', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Теремок', 'теремок, teremok', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Крошка Картошка', 'крошка картошка, kroshka kartoshka', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Coffee Like', 'coffee like, кофе лайк', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Subway', 'сабвей, subway', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Papa John’s', 'папа джонс, papa johns', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Додо Пицца', 'додо пицца, dodo pizza', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Чайхона №1', 'чайхона, chaihona', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('One Price Coffee', 'ван прайс кофе, one price coffee', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Surf Coffee', 'серф кофе, surf coffee', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Cofix', 'кофикс, cofix', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),

-- ============================================================
-- 3. РЕСТОРАНЫ (MCC 5812)
-- ============================================================
('Кофемания', 'кофемания, cofemania', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Шоколадница', 'шоколадница, shokoladnitsa', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('IL Патио', 'il patio, иль патио', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Тануки', 'тануки, tanuki', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Якитория', 'якитория, yakitoria', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Хачапури Марико', 'хачапури марико, khachapuri mariko', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Сыроварня', 'сыроварня, syrovarnya', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Перчини', 'перчини, perchini', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('АндерСон', 'андерсон, anderson', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Хочу Пури', 'хочу пури, khochu puri', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Novikov Group', 'новиков, novikov group', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),
('Ginza Project', 'гинза, ginza project', (SELECT id FROM category WHERE slug = 'restaurants'), '5812'),

-- ============================================================
-- 4. АПТЕКИ (MCC 5912)
-- ============================================================
('Ригла', 'ригла, rigla', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Аптека 36.6', '36.6, тридцать шесть и шесть', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Здравсити', 'здравсити, zdravsiti', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Планета Здоровья', 'планета здоровья, planeta zdorovya', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Апрель', 'апрель, aprel', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Здоров.ру', 'здоров.ру, zdorov.ru', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Имплозия', 'имплозия, imploziya', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Нео-Фарм', 'нео-фарм, neo-farm', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Еаптека', 'еаптека, eapteka', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Вита', 'вита, vita', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Мелодия Здоровья', 'мелодия здоровья, melodiya zdorovya', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Аптека.ру', 'аптека ру, apteka.ru', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Магнит Аптека', 'магнит аптека, magnit apteka', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),
('Аптека №1', 'аптека номер 1, apteka nomer 1', (SELECT id FROM category WHERE slug = 'pharmacies'), '5912'),

-- ============================================================
-- 5. ОДЕЖДА, ОБУВЬ, АКСЕССУАРЫ (MCC 5691 / 5651 / 5661)
-- ============================================================
('Lime', 'лайм, lime', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Melon Fashion Group', 'melon fashion, зарина, befree, love republic', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Gloria Jeans', 'глория джинс, gloria jeans', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Kari', 'кари, kari', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5661'),
('Henderson', 'хендерсон, henderson', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Familia', 'фамилия, familia', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5691'),
('Kanzler', 'канцлер, kanzler', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Savage', 'саваж, savage', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Funday', 'фандей, funday', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Rendez-Vous', 'рандеву, rendez-vous', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5661'),
('Zenden', 'зенден, zenden', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5661'),
('Ekonika', 'эконика, ekonika', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5661'),
('Centro', 'центро, centro', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5661'),
('Юничел', 'юничел, yunichel', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5661'),
('Вестфалика', 'вестфалика, westfalika', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5661'),
('Ostin', 'остин, ostin', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Befree', 'бифри, befree', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Love Republic', 'лав репаблик, love republic', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Zarina', 'зарина, zarina', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Sela', 'села, sela', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Cropp', 'crop, cropp', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Reserved', 'резервед, reserved', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('House', 'хаус, house', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Modis', 'модис, modis', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Твое', 'твое, tvoe', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Zara', 'зара, zara', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Uniqlo', 'юникло, uniqlo', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('H&M', 'аш эм, h&m, hm', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Bershka', 'бершка, bershka', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Stradivarius', 'страдивариус, stradivarius', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Oysho', 'ойшо, oysho', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Massimo Dutti', 'массимо дутти, massimo dutti', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Pull&Bear', 'пул энд бэр, pull&bear, pull and bear', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Mango', 'манго, mango', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('COS', 'кос, cos', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),
('Arket', 'аркет, arket', (SELECT id FROM category WHERE slug = 'clothes_shoes'), '5651'),

-- ============================================================
-- 6. СПОРТТОВАРЫ (MCC 5655)
-- ============================================================
('Спортмастер', 'спортмастер, sportmaster', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('Decathlon', 'декатлон, decathlon', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('Desport', 'деспорт, desport', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('Триал-Спорт', 'триал спорт, trial sport', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('Кант', 'кант, kant', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('АльпИндустрия', 'альпиндустрия, alpinindustriya', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('Street Beat', 'стрит бит, street beat', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('Amazing Red', 'эмейзинг ред, amazing red', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('Спорт-Марафон', 'спорт-марафон, sport-marafon', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),
('Runlab', 'ранлаб, runlab', (SELECT id FROM category WHERE slug = 'sport_goods'), '5655'),

-- ============================================================
-- 7. ЭЛЕКТРОНИКА И БЫТОВАЯ ТЕХНИКА (MCC 5732 / 5722)
-- ============================================================
('DNS', 'днс, dns', (SELECT id FROM category WHERE slug = 'electronics'), '5722'),
('М.Видео', 'мвидео, m.video', (SELECT id FROM category WHERE slug = 'electronics'), '5722'),
('Эльдорадо', 'эльдорадо, eldorado', (SELECT id FROM category WHERE slug = 'electronics'), '5722'),
('Ситилинк', 'ситилинк, citilink', (SELECT id FROM category WHERE slug = 'electronics'), '5722'),
('ОнлайнТрейд', 'онлайнтрейд, onlinetrade', (SELECT id FROM category WHERE slug = 'electronics'), '5732'),
('Регард', 'регард, regard', (SELECT id FROM category WHERE slug = 'electronics'), '5732'),
('Холодильник.Ру', 'холодильник.ру, kholodilnik.ru', (SELECT id FROM category WHERE slug = 'electronics'), '5722'),
('Vela', 'вела, vela', (SELECT id FROM category WHERE slug = 'electronics'), '5732'),
('Технопарк', 'технопарк, tekhnopark', (SELECT id FROM category WHERE slug = 'electronics'), '5732'),

-- ============================================================
-- 8. КОСМЕТИКА И ПАРФЮМЕРИЯ (MCC 5311 / 5977)
-- ============================================================
('Золотое яблоко', 'золотое яблоко, golden apple', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5311'),
('Л’Этуаль', 'летуаль, letu', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Рив Гош', 'рив гош, riv gosh', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Иль де Ботэ', 'иль де ботэ, ile de beaute', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Улыбка Радуги', 'улыбка радуги, ulybka radugi', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Магнит Косметик', 'магнит косметик, magnit kosmetik', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5311'),
('Подружка', 'подружка, podruzhka', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Foam', 'foam, фоам', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Yves Rocher', 'ив роше, yves rocher', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Aroma-Butik', 'арома-бутик, aroma-butik', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Randewoo', 'рандеву, randewoo', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Odiva', 'одива, odiva', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('4fresh', '4фреш, 4fresh', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Фитомаркет', 'фитомаркет, fitomarket', (SELECT id FROM category WHERE slug = 'beauty_spa'), '5977'),
('Локситан', 'локситан, l''occitane, loccitane, l occitane', (SELECT id FROM category WHERE slug = 'cosmetics'), '5977'),

-- ============================================================
-- 9. ДОМ, РЕМОНТ, DIY (MCC 5200 / 5211 / 5712)
-- ============================================================
('Лемана ПРО', 'лемана про, lemana pro, леруа мерлен', (SELECT id FROM category WHERE slug = 'home_repair'), '5200'),
('ВсеИнструменты.ру', 'всеинструменты, vseinstrumenti', (SELECT id FROM category WHERE slug = 'home_repair'), '5211'),
('Петрович', 'петрович, petrovich', (SELECT id FROM category WHERE slug = 'home_repair'), '5211'),
('Строительный Двор', 'строительный двор, stroitelny dvor', (SELECT id FROM category WHERE slug = 'home_repair'), '5211'),
('Максидом', 'максидом, maksidom', (SELECT id FROM category WHERE slug = 'home_repair'), '5211'),
('Бауцентр', 'бауцентр, baucenter', (SELECT id FROM category WHERE slug = 'home_repair'), '5211'),
('Askona', 'аскона, askona', (SELECT id FROM category WHERE slug = 'home_repair'), '5712'),
('Hoff', 'хофф, hoff', (SELECT id FROM category WHERE slug = 'home_repair'), '5200'),
('Сатурн', 'сатурн, saturn', (SELECT id FROM category WHERE slug = 'home_repair'), '5211'),
('Castorama', 'касторама, castorama', (SELECT id FROM category WHERE slug = 'home_repair'), '5200'),
('Порядок', 'порядок, poryadok', (SELECT id FROM category WHERE slug = 'home_repair'), '5211'),
('Твой Дом', 'твой дом, tvoy dom', (SELECT id FROM category WHERE slug = 'home_repair'), '5712'),
('KuchenLand', 'кухенленд, kuchenland, kuchenland home', (SELECT id FROM category WHERE slug = 'home_repair'), '5200'),
('Cozy Home', 'кози хоум, cozy home, cozyhome', (SELECT id FROM category WHERE slug = 'home_repair'), '5200'),

-- ============================================================
-- 10. ТОПЛИВО И АЗС (MCC 5541)
-- ============================================================
('Лукойл', 'лукойл, lukoil', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Газпромнефть', 'газпромнефть, gazpromneft', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Роснефть', 'роснефть, rosneft', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Татнефть', 'татнефть, tatneft', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Башнефть', 'башнефть, bashneft', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Сургутнефтегаз', 'сургутнефтегаз, surgutneftegaz', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Газпром', 'газпром, gazprom', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('ТАИФ', 'таиф, taif', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Shell', 'шелл, shell', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Нефтьмагистраль', 'нефтьмагистраль, neftmagistral', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('Трасса', 'трасса, trassa', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),
('ЕКА', 'ека, eka', (SELECT id FROM category WHERE slug = 'fuel'), '5541'),

-- ============================================================
-- 11. КНИГИ (MCC 5942)
-- ============================================================
('Читай-город', 'читай город, chitai gorod', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Буквоед', 'буквоед, bukvoed', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Московский Дом Книги', 'московский дом книги, mdk', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Новый Книжный', 'новый книжный, novy knizhny', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Гоголь-Моголь', 'гоголь-моголь, gogol-mogol', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Дом книги', 'дом книги, dom knigi', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Пегас', 'пегас, pegas', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Книга+', 'книга плюс, kniga plus', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Амиталь', 'амиталь, amital', (SELECT id FROM category WHERE slug = 'books'), '5942'),
('Топ-книга', 'топ-книга, top-kniga', (SELECT id FROM category WHERE slug = 'books'), '5942'),

-- ============================================================
-- 12. ТОВАРЫ ДЛЯ ДЕТЕЙ (MCC 5641 / 5945)
-- ============================================================
('Детский мир', 'детский мир, detsky mir', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Ещё', 'ещё, eshe', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Кораблик', 'кораблик, korablik', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Lapsi', 'лапси, lapsi', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Acoola', 'акула, acoola', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Choupette', 'шупет, choupette', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Кенгуру', 'кенгуру, kenguru', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Bebakids', 'бебакидс, bebakids', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Huggies', 'хаггис, huggies', (SELECT id FROM category WHERE slug = 'kids_goods'), '5641'),
('Мир Кубиков', 'мир кубиков, mir kubikov, миркубиков', (SELECT id FROM category WHERE slug = 'kids_goods'), '5945'),

-- ============================================================
-- 13. ЮВЕЛИРНЫЕ ИЗДЕЛИЯ (MCC 5944)
-- ============================================================
('Sunlight', 'санлайт, sunlight', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('Sokolov', 'соколов, sokolov', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('585 Золотой', '585 золотой, 585 zolotoy', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('MIUZ Diamonds', 'миуз, miuz', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('Адамас', 'адамас, adamas', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('Ювелирочка', 'ювелирочка, yuvelirochka', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('Бронницкий ювелир', 'бронницкий ювелир, bronnitsky yuvelir', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('Линии любви', 'линии любви, linii lyubvi', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('585 Gold', '585 голд, 585 gold', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('ЭПЛ Даймонд', 'эпл даймонд, epl diamond', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),
('1 Ювелирная сеть', '1 ювелирная сеть, 1 yuvelirnaya set', (SELECT id FROM category WHERE slug = 'jewelry'), '5944'),

-- ============================================================
-- 14. ВЕТКЛИНИКИ И ЗООМАГАЗИНЫ (MCC 0742 / 5995)
-- ============================================================
('Четыре Лапы', 'четыре лапы, chetyre lapy', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('Бетховен', 'бетховен, betkhoven', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('Зоозавр', 'зоозавр, zoozavr', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('Ле’Муррр', 'ле муррр, le murrr', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('Красный Кролик', 'красный кролик, krasny krolik', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('Свой доктор', 'свой доктор, svoy doktor', (SELECT id FROM category WHERE slug = 'vet_pets'), '0742'),
('БиоВет', 'биовет, biovet', (SELECT id FROM category WHERE slug = 'vet_pets'), '0742'),
('ЗооМир', 'зоомир, zoomir', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('Кот и Пес', 'кот и пес, kot i pes', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('Валта', 'валта, valta', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('Petshop.ru', 'петшоп, petshop, petshop.ru, петшоп.ру', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),
('ЗооОптТорг', 'зоооптторг, zoooptorg, зоо опт торг', (SELECT id FROM category WHERE slug = 'vet_pets'), '5995'),

-- ============================================================
-- 15. МАРКЕТПЛЕЙСЫ (MCC 5300)
-- ============================================================
('Wildberries', 'вайлдберриз, wildberries, wb', (SELECT id FROM category WHERE slug = 'marketplaces'), '5300'),
('Ozon', 'озон, ozon', (SELECT id FROM category WHERE slug = 'marketplaces'), '5300'),
('Ozon Fresh', 'озон фреш, ozon fresh', (SELECT id FROM category WHERE slug = 'supermarkets'), '5300'),
('Авито', 'авито, avito', (SELECT id FROM category WHERE slug = 'marketplaces'), '7278'),
('Lamoda', 'ламода, lamoda', (SELECT id FROM category WHERE slug = 'marketplaces'), '5300'),
('AliExpress', 'алиэкспресс, aliexpress', (SELECT id FROM category WHERE slug = 'marketplaces'), '5300'),

-- ============================================================
-- 16. ЦВЕТЫ (MCC 5992)
-- ============================================================
('Цветовик', 'цветовик, tsvetovik', (SELECT id FROM category WHERE slug = 'flowers'), '5992'),
('Флорист.ру', 'флорист.ру, florist.ru', (SELECT id FROM category WHERE slug = 'flowers'), '5992'),
('Megaflowers', 'мегафлауэрс, megaflowers', (SELECT id FROM category WHERE slug = 'flowers'), '5992'),
('Аленький цветочек', 'аленький цветочек, alenky tsvetochek', (SELECT id FROM category WHERE slug = 'flowers'), '5992'),
('Расцветай', 'расцветай, rastsvetay', (SELECT id FROM category WHERE slug = 'flowers'), '5992'),
('FMART by flowwow', 'фмарт, fmart', (SELECT id FROM category WHERE slug = 'flowers'), '5992'),
('Клумба', 'клумба, klumba', (SELECT id FROM category WHERE slug = 'flowers'), '5992'),
('Цветочный ряд', 'цветочный ряд, tsvetochny ryad', (SELECT id FROM category WHERE slug = 'flowers'), '5992'),

-- ============================================================
-- 17. АЛКОГОЛЬ (MCC 5921 / 5499)
-- ============================================================
('Красное&Белое', 'красное белое, krasnoe beloe', (SELECT id FROM category WHERE slug = 'alcohol'), '5499'),
('Бристоль', 'бристоль, bristol', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('ВинЛаб', 'винлаб, vinlab', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('Ароматный мир', 'ароматный мир, aromatny mir', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('Горилка', 'горилка, gorilka', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('Градусы', 'градусы, gradusy', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('КуулКлевер', 'куулклевер, coolclever, отдохни', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('SimpleWine', 'симплвайн, simplewine', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('Drinx', 'дринкс, drinx', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('Винный склад', 'винный склад, vinny sklad', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),
('Алкотека', 'алкотека, alkoteka, алко тека', (SELECT id FROM category WHERE slug = 'alcohol'), '5921'),

-- ============================================================
-- 18. ФИТНЕС (MCC 7997)
-- ============================================================
('DDX Fitness', 'ддикс фитнес, ddx fitness', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('Alex Fitness', 'алекс фитнес, alex fitness', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('Spirit Fitness', 'спирит фитнес, spirit fitness', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('Fitness House', 'фитнес хаус, fitness house', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('Bright Fit', 'брайт фит, bright fit', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('YoBody Fitness', 'йобади фитнес, yobody fitness', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('World Class', 'ворлд класс, world class', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('Crocus Fitness', 'крокус фитнес, crocus fitness', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('Территория Фитнеса', 'территория фитнеса, territoriya fitness', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),
('Orange Fitness', 'оранж фитнес, orange fitness', (SELECT id FROM category WHERE slug = 'fitness'), '7997'),

-- ============================================================
-- 19. МЕДИЦИНСКИЕ КЛИНИКИ (MCC 8011 / 8071 / 8099)
-- ============================================================
('Медси', 'медси, medsi', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Мать и дитя', 'мать и дитя, mat i ditya', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('ЕМС', 'емс, emc', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('МедИнвестГрупп', 'мединвестгрупп, medinvestgroup', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Медскан', 'медскан, medskan', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('СМ-Клиника', 'см-клиника, sm-klinika', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Поликлиника.ру', 'поликлиника.ру, poliklinika.ru', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Скандинавия', 'скандинавия, skandinaviya', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Клиника Фомина', 'клиника фомина, klinika fomina', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Екатерининская', 'екатерининская, ekaterininskaya', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('МЦ Эксперт', 'мц эксперт, mts expert', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Инвитро', 'инвитро, invitro', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Хеликс', 'хеликс, helix', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8011'),
('Гемотест', 'гемотест, gemotest', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8071'),
('KDL', 'кдл, kdl', (SELECT id FROM category WHERE slug = 'medical_clinics'), '8071'),

-- ============================================================
-- 20. МУЗЫКАЛЬНЫЕ ИНСТРУМЕНТЫ (MCC 5732 / 5999)
-- ============================================================
('Музторг', 'музторг, muztorg', (SELECT id FROM category WHERE slug = 'musical_instruments'), '5732'),
('Мир Музыки', 'мир музыки, mir muzyki', (SELECT id FROM category WHERE slug = 'musical_instruments'), '5732'),
('ZonaZvuka', 'зона звука, zonazvuka', (SELECT id FROM category WHERE slug = 'musical_instruments'), '5732'),
('DMTR PEDAL SHOP', 'dmtr pedal shop, дмтр', (SELECT id FROM category WHERE slug = 'musical_instruments'), '5999'),
('Слами', 'слами, slami', (SELECT id FROM category WHERE slug = 'musical_instruments'), '5732'),
('Динатон', 'динатон, dinaton', (SELECT id FROM category WHERE slug = 'musical_instruments'), '5732'),

-- ============================================================
-- 21. ХИМЧИСТКИ (MCC 7216)
-- ============================================================
('Диана', 'диана, diana', (SELECT id FROM category WHERE slug = 'dry_cleaning'), '7216'),
('Бианка', 'бианка, bianca', (SELECT id FROM category WHERE slug = 'dry_cleaning'), '7216'),
('Хозяюшка', 'хозяюшка, khozyayushka', (SELECT id FROM category WHERE slug = 'dry_cleaning'), '7216'),
('Белый Город', 'белый город, bely gorod', (SELECT id FROM category WHERE slug = 'dry_cleaning'), '7216'),
('Главхимчистка', 'главхимчистка, glavkhimchistka', (SELECT id FROM category WHERE slug = 'dry_cleaning'), '7216'),
('Лавандерия', 'лавандерия, lavanderia', (SELECT id FROM category WHERE slug = 'dry_cleaning'), '7216'),
('Химчистка №1', 'химчистка номер 1, khimchistka nomer 1', (SELECT id FROM category WHERE slug = 'dry_cleaning'), '7216'),
('Лисичка', 'лисичка, lisichka', (SELECT id FROM category WHERE slug = 'dry_cleaning'), '7216'),

-- ============================================================
-- 22. ФОТО/ВИДЕО (MCC 5946)
-- ============================================================
('Яркий фотомаркет', 'яркий фотомаркет, yarkiy fotomarket', (SELECT id FROM category WHERE slug = 'photo_video'), '5946'),
('Мультифото', 'мультифото, multifoto', (SELECT id FROM category WHERE slug = 'photo_video'), '5946'),
('Фото-М', 'фото-м, foto-m', (SELECT id FROM category WHERE slug = 'photo_video'), '5946'),
('Kodak Express', 'кодак экспресс, kodak express', (SELECT id FROM category WHERE slug = 'photo_video'), '5946'),
('ФотоЛюкс', 'фотолюкс, fotolyuks', (SELECT id FROM category WHERE slug = 'photo_video'), '5946'),
('Пленка', 'пленка, plenka', (SELECT id FROM category WHERE slug = 'photo_video'), '5946'),

-- ============================================================
-- 23. ТАКСИ (MCC 4121, Яндекс Такси — 3990)
-- ============================================================
('Maxim', 'максим, maxim', (SELECT id FROM category WHERE slug = 'taxi'), '4121'),
('Ситимобил', 'ситимобил, citimobil', (SELECT id FROM category WHERE slug = 'taxi'), '4121'),
('Везёт', 'везёт, vezet', (SELECT id FROM category WHERE slug = 'taxi'), '4121'),
('Таксовичкоф', 'таксовичкоф, taksiovichkof', (SELECT id FROM category WHERE slug = 'taxi'), '4121'),
('Drivee', 'драйви, drivee, inDrive', (SELECT id FROM category WHERE slug = 'taxi'), '4121'),
('Gett', 'гетт, gett', (SELECT id FROM category WHERE slug = 'taxi'), '4121'),
('Яндекс Такси', 'яндекс такси, yandex taxi, такси, яндекс го, yandex go', (SELECT id FROM category WHERE slug = 'taxi'), '3990'),

-- ============================================================
-- 24. КАРШЕРИНГ (MCC 7512)
-- ============================================================
('Делимобиль', 'делимобиль, delimobil', (SELECT id FROM category WHERE slug = 'carsharing'), '7512'),
('Ситидрайв', 'ситидрайв, citydrive', (SELECT id FROM category WHERE slug = 'carsharing'), '7512'),
('BelkaCar', 'белка кар, belkacar', (SELECT id FROM category WHERE slug = 'carsharing'), '7512'),
('YouDrive', 'юдрайв, youdrive', (SELECT id FROM category WHERE slug = 'carsharing'), '7512'),
('AnyTime', 'энитайм, anytime', (SELECT id FROM category WHERE slug = 'carsharing'), '7512'),
('Мобили', 'мобили, mobili', (SELECT id FROM category WHERE slug = 'carsharing'), '7512'),

-- ============================================================
-- 25. АВТОУСЛУГИ (MCC 7538 / 5511)
-- ============================================================
('FIT SERVICE', 'фит сервис, fit service', (SELECT id FROM category WHERE slug = 'auto_services'), '7538'),
('G-Energy Service РОЛЬФ', 'джи энерджи сервис рольф, g-energy rolf', (SELECT id FROM category WHERE slug = 'auto_services'), '7538'),
('ВилГуд', 'вилгуд, vilgood', (SELECT id FROM category WHERE slug = 'auto_services'), '7538'),
('РОЛЬФ', 'рольф, rolf', (SELECT id FROM category WHERE slug = 'auto_services'), '5511'),
('Автомир', 'автомир, avtomir', (SELECT id FROM category WHERE slug = 'auto_services'), '5511'),
('Мэйджор', 'мэйджор, major', (SELECT id FROM category WHERE slug = 'auto_services'), '5511'),
('Автодом', 'автодом, avtodom', (SELECT id FROM category WHERE slug = 'auto_services'), '5511'),
('Авилон', 'авилон, avilon', (SELECT id FROM category WHERE slug = 'auto_services'), '5511'),
('Корс Групп', 'корс групп, kors group', (SELECT id FROM category WHERE slug = 'auto_services'), '5511'),
('VAG', 'ваг, vag', (SELECT id FROM category WHERE slug = 'auto_services'), '7538'),
('Inchcape', 'инчкейп, inchcape', (SELECT id FROM category WHERE slug = 'auto_services'), '5511'),

-- ============================================================
-- 26. РАЗВЛЕЧЕНИЯ (MCC 7999 / 7832)
-- ============================================================
('COLIZEUM', 'колизеум, colizeum', (SELECT id FROM category WHERE slug = 'entertainment'), '7999'),
('Клаустрофобия', 'клаустрофобия, klaustrofobiya', (SELECT id FROM category WHERE slug = 'entertainment'), '7999'),
('НЕБО', 'небо, nebo', (SELECT id FROM category WHERE slug = 'entertainment'), '7999'),
('Zамания', 'замания, zamaniya', (SELECT id FROM category WHERE slug = 'entertainment'), '7999'),
('Play Day', 'плей дей, play day', (SELECT id FROM category WHERE slug = 'entertainment'), '7999'),
('Hello Park', 'хелло парк, hello park', (SELECT id FROM category WHERE slug = 'entertainment'), '7999'),
('Формула Кино', 'формула кино, formula kino', (SELECT id FROM category WHERE slug = 'entertainment'), '7832'),
('Синема Парк', 'синема парк, cinema park', (SELECT id FROM category WHERE slug = 'entertainment'), '7832'),
('КАРО', 'каро, karo', (SELECT id FROM category WHERE slug = 'entertainment'), '7832'),
('Киномакс', 'киномакс, kinomaks', (SELECT id FROM category WHERE slug = 'entertainment'), '7832'),
('Премьер-зал', 'премьер-зал, premier-zal', (SELECT id FROM category WHERE slug = 'entertainment'), '7832'),
('Люксор', 'люксор, lyuksor', (SELECT id FROM category WHERE slug = 'entertainment'), '7832'),
('Мираж Синема', 'мираж синема, mirazh cinema', (SELECT id FROM category WHERE slug = 'entertainment'), '7832'),

-- ============================================================
-- 27. ОТЕЛИ (MCC 7011)
-- ============================================================
('Azimut Hotels', 'азимут отели, azimut hotels', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Cosmos Hotel Group', 'космос отели, cosmos hotel group', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Mantera', 'мантера, mantera', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Amaks', 'амакс, amaks', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Accor', 'аккор, accor', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Radisson', 'радиссон, radisson', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Hilton', 'хилтон, hilton', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Marriott', 'марриотт, marriott', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Альфа', 'альфа, alfa', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),
('Хелиопарк', 'хелиопарк, heliopark', (SELECT id FROM category WHERE slug = 'hotels'), '7011'),

-- ============================================================
-- 28. ТРАНСПОРТ (MCC 4111 / 4511 / 4214)
-- ============================================================
('РЖД', 'ржд, rzd, российские железные дороги', (SELECT id FROM category WHERE slug = 'transport'), '4111'),
('Аэрофлот', 'аэрофлот, aeroflot', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('S7 Airlines', 's7, сибирь, s7 airlines', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Россия', 'россия, rossiya', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Победа', 'победа, pobeda', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Уральские авиалинии', 'уральские авиалинии, ural airlines', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Nordwind', 'нордвинд, nordwind', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Smartavia', 'смартавиа, smartavia', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Red Wings', 'ред вингс, red wings', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('AZUR Air', 'азур эйр, azur air', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Ютэйр', 'ютэйр, utair', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Деловые Линии', 'деловые линии, delovye linii', (SELECT id FROM category WHERE slug = 'transport'), '4214'),
('СДЭК', 'сдэк, cdek', (SELECT id FROM category WHERE slug = 'transport'), '4214'),
('ПЭК', 'пэк, pek', (SELECT id FROM category WHERE slug = 'transport'), '4214'),
('Байкал Сервис', 'байкал сервис, baikal servis', (SELECT id FROM category WHERE slug = 'transport'), '4214'),
('ТК КИТ', 'тк кит, tk kit', (SELECT id FROM category WHERE slug = 'transport'), '4214'),
('Азимут', 'азимут, azimuth', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('ИрАэро', 'ираэро, iraero', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('ЮВТ Аэро', 'ювт аэро, yuvt aero', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('РусЛайн', 'руслайн, rusline', (SELECT id FROM category WHERE slug = 'transport'), '4511'),
('Boxberry', 'боксберри, boxberry', (SELECT id FROM category WHERE slug = 'transport'), '4214'),
('Dostavista', 'достависта, dostavista', (SELECT id FROM category WHERE slug = 'transport'), '4214'),

-- ============================================================
-- 29. АВИАБИЛЕТЫ / ЖД БИЛЕТЫ (СЕРВИСЫ)
-- ============================================================
('Туту.ру', 'туту.ру, tutu.ru', (SELECT id FROM category WHERE slug = 'air_tickets'), '4511'),
('Biletix', 'билетикс, biletix', (SELECT id FROM category WHERE slug = 'air_tickets'), '4511'),
('Aviasales', 'авиасейлс, aviasales', (SELECT id FROM category WHERE slug = 'air_tickets'), '4511'),
('OneTwoTrip', 'вантутрип, onetwotrip', (SELECT id FROM category WHERE slug = 'air_tickets'), '4511'),
('Купибилет', 'купибилет, kupibilet', (SELECT id FROM category WHERE slug = 'air_tickets'), '4511'),
('Едем.рф', 'едем.рф, edem.rf', (SELECT id FROM category WHERE slug = 'railway_tickets'), '4111'),
('RailRoute', 'раилроут, railroute', (SELECT id FROM category WHERE slug = 'railway_tickets'), '4111'),

-- ============================================================
-- 30. ОБРАЗОВАНИЕ (EDTECH, MCC 7277 / 8299)
-- ============================================================
('Учи.ру', 'учи.ру, uchi.ru', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('ЯКласс', 'якласс, yaklass', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('Фоксфорд', 'фоксфорд, foxford', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('Skyeng', 'скайенг, skyeng', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('ИнтернетУрок', 'интернетурок, interneturok', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('Яндекс Учебник', 'яндекс учебник, yandex uchebnik', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('Синергия', 'синергия, sinergiya', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('Skillbox', 'скиллбокс, skillbox', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('GetCourse', 'геткурс, getcourse', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('Тетрика', 'тетрика, tetrika', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('Нетология', 'нетология, netologiya', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('GeekBrains', 'гикбрейнс, geekbrains', (SELECT id FROM category WHERE slug = 'education'), '8299'),
('Stepik', 'степик, stepik', (SELECT id FROM category WHERE slug = 'education'), '7277'),
('Международная школа профессий', 'международная школа профессий, mshp', (SELECT id FROM category WHERE slug = 'education'), '8299'),

-- ============================================================
-- 31. СВЯЗЬ И ИНТЕРНЕТ (MCC 4814 / 4816 / 4899)
-- ============================================================
('МТС', 'мтс, mts', (SELECT id FROM category WHERE slug = 'telecom'), '4814'),
('Билайн', 'билайн, beeline', (SELECT id FROM category WHERE slug = 'telecom'), '4814'),
('МегаФон', 'мегафон, megafon', (SELECT id FROM category WHERE slug = 'telecom'), '4814'),
('Т2', 'т2, tele2, t2', (SELECT id FROM category WHERE slug = 'telecom'), '4814'),
('Ростелеком', 'ростелеком, rostelecom', (SELECT id FROM category WHERE slug = 'telecom'), '4816'),
('Yota', 'йота, yota', (SELECT id FROM category WHERE slug = 'telecom'), '4814'),
('СберМобайл', 'сбермобайл, sbermobile', (SELECT id FROM category WHERE slug = 'telecom'), '4814'),
('Тинькофф Мобайл', 'тинькофф мобайл, tinkoff mobile', (SELECT id FROM category WHERE slug = 'telecom'), '4814'),
('Дом.ру', 'дом.ру, dom.ru', (SELECT id FROM category WHERE slug = 'telecom'), '4899'),
('МТС Home', 'мтс home, mts home', (SELECT id FROM category WHERE slug = 'telecom'), '4899'),

-- ============================================================
-- 32. ШТРАФЫ / НАЛОГИ / ЖКХ / ГОСУСЛУГИ
-- ============================================================
('Госуслуги', 'госуслуги, gosuslugi', (SELECT id FROM category WHERE slug = 'fines'), '9399'),
('РосШтрафы', 'росштрафы, rosshtrafy', (SELECT id FROM category WHERE slug = 'fines'), '9399'),
('Штрафы', 'штрафы, shtrafy', (SELECT id FROM category WHERE slug = 'fines'), '9222'),
('Финуслуги', 'финуслуги, finuslugi', (SELECT id FROM category WHERE slug = 'fines'), '9399'),
('Личный кабинет налогоплательщика', 'лк налогоплательщика, lk nalog', (SELECT id FROM category WHERE slug = 'taxes'), '9311'),
('Налоги ФЛ', 'налоги фл, nalogi fl', (SELECT id FROM category WHERE slug = 'taxes'), '9311'),
('Уплата налогов и пошлин', 'уплата налогов и пошлин, uplata nalogov', (SELECT id FROM category WHERE slug = 'taxes'), '9311'),
('Госуслуги.Дом', 'госуслуги дом, gosuslugi dom', (SELECT id FROM category WHERE slug = 'utilities'), '4900'),
('ГИС ЖКХ', 'гис жкх, gis zhkh', (SELECT id FROM category WHERE slug = 'utilities'), '4900'),
('Квартплата+', 'квартплата+, kvartplata+', (SELECT id FROM category WHERE slug = 'utilities'), '4900'),

-- ============================================================
-- 33. ТРАНСПОРТНЫЕ КАРТЫ / DUTY FREE / БЫТОВЫЕ УСЛУГИ
-- ============================================================
('Тройка', 'тройка, troika', (SELECT id FROM category WHERE slug = 'transport_cards'), '4111'),
('Нижегородская транспортная карта', 'нижегородская транспортная карта, nizhegorodskaya karta', (SELECT id FROM category WHERE slug = 'transport_cards'), '4111'),
('Подорожник', 'подорожник, podorozhnik', (SELECT id FROM category WHERE slug = 'transport_cards'), '4111'),
('Единый билет', 'единый билет, ediny bilet', (SELECT id FROM category WHERE slug = 'transport_cards'), '4111'),
('Duty Free Домодедово', 'duty free домодедово, duty free domodedovo', (SELECT id FROM category WHERE slug = 'duty_free'), '5309'),
('Duty Free Шереметьево', 'duty free шереметьево, duty free sheremetyevo', (SELECT id FROM category WHERE slug = 'duty_free'), '5309'),
('Duty Free Пулково', 'duty free пулково, duty free pulkovo', (SELECT id FROM category WHERE slug = 'duty_free'), '5309'),
('Секундочку', 'секундочку, sekundochku', (SELECT id FROM category WHERE slug = 'household_services'), '7629'),
('Мастер на час', 'мастер на час, master na chas', (SELECT id FROM category WHERE slug = 'household_services'), '7629'),
('YouDo', 'юду, youdo', (SELECT id FROM category WHERE slug = 'household_services'), '7629'),
('Профи.ру', 'профи.ру, profi.ru', (SELECT id FROM category WHERE slug = 'household_services'), '7629'),

-- ============================================================
-- 34. ИСКУССТВО / МУЗЕИ / ВЫСТАВКИ
-- ============================================================
('Третьяковская галерея', 'третьяковка, tretyakov gallery', (SELECT id FROM category WHERE slug = 'art'), '7999'),
('Эрмитаж', 'эрмитаж, hermitage', (SELECT id FROM category WHERE slug = 'museums'), '7999'),
('Пушкинский музей', 'пушкинский музей, pushkin museum', (SELECT id FROM category WHERE slug = 'museums'), '7999'),
('Русский музей', 'русский музей, russian museum', (SELECT id FROM category WHERE slug = 'museums'), '7999'),
('ВДНХ', 'вднх, vdnkh', (SELECT id FROM category WHERE slug = 'museums'), '7999'),
('Москвариум', 'москвариум, moskvarium', (SELECT id FROM category WHERE slug = 'museums'), '7999'),
('Музей «АТОМ»', 'музей атом, museum atom', (SELECT id FROM category WHERE slug = 'museums'), '7999'),
('Этномир', 'этномир, etnomir', (SELECT id FROM category WHERE slug = 'museums'), '7999'),
('Музей Фаберже', 'музей фаберже, faberge museum', (SELECT id FROM category WHERE slug = 'museums'), '7999'),
('Царицыно', 'царицыно, tsaritsyno', (SELECT id FROM category WHERE slug = 'museums'), '7999'),

-- ============================================================
-- 35. ДОСТАВКА ЕДЫ И ПРОДУКТОВ (MCC 5411 / 5814)
-- ============================================================
('Яндекс Еда', 'яндекс еда, yandex eda', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Delivery Club', 'деливери клаб, delivery club', (SELECT id FROM category WHERE slug = 'fastfood'), '5814'),
('Купер', 'купер, kuper', (SELECT id FROM category WHERE slug = 'supermarkets'), '5411'),

-- ============================================================
-- 36. ЭКОСИСТЕМА ЯНДЕКСА (MCC 3990)
-- ============================================================
('Яндекс Маркет', 'яндекс маркет, yandex market', (SELECT id FROM category WHERE slug = 'yandex_ecosystem'), '3990'),
('Яндекс Драйв', 'яндекс драйв, yandex drive', (SELECT id FROM category WHERE slug = 'yandex_ecosystem'), '3990'),
('Яндекс Путешествия', 'яндекс путешествия, yandex travel', (SELECT id FROM category WHERE slug = 'yandex_ecosystem'), '3990'),
('Яндекс Практикум', 'яндекс практикум, yandex praktikum', (SELECT id FROM category WHERE slug = 'yandex_ecosystem'), '3990'),
('Яндекс Лавка', 'яндекс лавка, yandex lavka', (SELECT id FROM category WHERE slug = 'yandex_ecosystem'), '3990'),
('Яндекс Афиша', 'яндекс афиша, yandex afisha, афиша', (SELECT id FROM category WHERE slug = 'yandex_ecosystem'), '3990'),

-- ============================================================
-- 37. ЭКОСИСТЕМА СБЕРА (MCC 3991)
-- ============================================================
('Мегамаркет', 'мегамаркет, megamarket', (SELECT id FROM category WHERE slug = 'sber_ecosystem'), '3991'),
('Самокат', 'самокат, samokat', (SELECT id FROM category WHERE slug = 'sber_ecosystem'), '3991')
ON CONFLICT (name) DO NOTHING;