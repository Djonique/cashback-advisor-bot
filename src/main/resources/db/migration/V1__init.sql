-- ============================================================================
-- V1__init.sql
-- Начальная схема БД Cashback Advisor Bot.
-- СУБД: PostgreSQL 16
-- ============================================================================

CREATE TABLE bank (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    slug          VARCHAR(50)  NOT NULL,
    display_order INT          NOT NULL DEFAULT 0,
    CONSTRAINT ux_bank_slug UNIQUE (slug)
);
COMMENT ON TABLE  bank               IS 'Банки, карты которых поддерживает бот';
COMMENT ON COLUMN bank.slug          IS 'Уникальный строковый идентификатор для callback_data и URL';
COMMENT ON COLUMN bank.display_order IS 'Порядок отображения в меню бота (по возрастанию)';

CREATE TABLE category (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(50)  NOT NULL,
    CONSTRAINT ux_category_slug UNIQUE (slug)
);
COMMENT ON TABLE category IS 'Категории кэшбэка, общие для всех банков';

CREATE TABLE bank_category (
    id          BIGSERIAL PRIMARY KEY,
    bank_id     BIGINT    NOT NULL,
    category_id BIGINT    NOT NULL,
    CONSTRAINT fk_bank_category_bank FOREIGN KEY (bank_id) REFERENCES bank(id) ON DELETE CASCADE,
    CONSTRAINT fk_bank_category_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT,
    CONSTRAINT ux_bank_category_pair UNIQUE (bank_id, category_id)
);
COMMENT ON TABLE bank_category IS 'Какие категории у какого банка (пары bank↔category). Проценты вводит пользователь.';

CREATE TABLE bank_category_mcc (
    id               BIGSERIAL PRIMARY KEY,
    bank_category_id BIGINT    NOT NULL,
    mcc_code         CHAR(4)   NOT NULL,
    CONSTRAINT fk_bcm_bank_category FOREIGN KEY (bank_category_id) REFERENCES bank_category(id) ON DELETE CASCADE,
    CONSTRAINT chk_bcm_mcc_format CHECK (mcc_code ~ '^[0-9]{4}$')
);
COMMENT ON TABLE  bank_category_mcc          IS 'MCC-коды, относящиеся к категории конкретного банка';
COMMENT ON COLUMN bank_category_mcc.mcc_code IS 'Строка из 4 цифр с ведущими нулями, например 0763';

CREATE INDEX idx_bank_category_mcc_code ON bank_category_mcc (mcc_code);

CREATE TABLE bank_excluded_mcc (
    id       BIGSERIAL PRIMARY KEY,
    bank_id  BIGINT    NOT NULL,
    mcc_code CHAR(4)   NOT NULL,
    CONSTRAINT fk_bem_bank FOREIGN KEY (bank_id) REFERENCES bank(id) ON DELETE CASCADE,
    CONSTRAINT ux_bem_pair UNIQUE (bank_id, mcc_code),
    CONSTRAINT chk_bem_mcc_format CHECK (mcc_code ~ '^[0-9]{4}$')
);
COMMENT ON TABLE bank_excluded_mcc IS 'MCC-коды, по которым банк не начисляет кэшбэк (из PDF)';

CREATE TABLE category_alias (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    alias       VARCHAR(100) NOT NULL,
    CONSTRAINT ux_category_alias UNIQUE (category_id, alias)
);
COMMENT ON TABLE category_alias IS 'Синонимы названий категорий для поиска (например, АЗС для Топливо)';

-- ----------------------------------------------------------------------------
-- Пользовательские данные (создаются в рантайме)
-- ----------------------------------------------------------------------------

CREATE TABLE app_user (
    id               BIGSERIAL   PRIMARY KEY,
    telegram_chat_id BIGINT      NOT NULL,
    username         VARCHAR(255) NULL,
    created_at       TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT ux_app_user_chat_id UNIQUE (telegram_chat_id)
);
COMMENT ON TABLE  app_user                  IS 'Пользователи Telegram-бота';
COMMENT ON COLUMN app_user.telegram_chat_id IS 'ID чата в Telegram — первичный идентификатор пользователя';

CREATE TABLE user_bank_selection (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    bank_id          BIGINT       NOT NULL,
    category_id      BIGINT       NOT NULL,
    year_month       CHAR(7)      NOT NULL,
    cashback_percent NUMERIC(5,2) NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_ubs_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ubs_bank FOREIGN KEY (bank_id) REFERENCES bank(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ubs_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT,
    CONSTRAINT ux_ubs_unique UNIQUE (user_id, bank_id, category_id, year_month),
    CONSTRAINT chk_ubs_percent CHECK (cashback_percent > 0 AND cashback_percent <= 100),
    CONSTRAINT chk_ubs_year_month_format CHECK (year_month ~ '^[0-9]{4}-(0[1-9]|1[0-2])$')
);
COMMENT ON TABLE  user_bank_selection                  IS 'Выбор категорий и процентов пользователем на календарный месяц';
COMMENT ON COLUMN user_bank_selection.year_month       IS 'Месяц в формате YYYY-MM; всегда Europe/Moscow';
COMMENT ON COLUMN user_bank_selection.cashback_percent IS 'Процент, который ввёл пользователь; 0 < x <= 100';

CREATE INDEX idx_ubs_user_month ON user_bank_selection (user_id, year_month);

CREATE TABLE user_state (
    user_id    BIGINT      PRIMARY KEY,
    state      VARCHAR(50) NOT NULL,
    context    JSONB       NULL,
    updated_at TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_state_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);
COMMENT ON TABLE  user_state         IS 'Текущее состояние диалога (FSM) для каждого пользователя';
COMMENT ON COLUMN user_state.state   IS 'IDLE | CHOOSING_BANK | CHOOSING_CATEGORIES | ENTERING_PERCENT | REVIEW | CARD_INPUT_MCC | CARD_INPUT_CATEGORY';
COMMENT ON COLUMN user_state.context IS 'Временные данные диалога (bankId, selectedCategoryIds, percents), JSON';