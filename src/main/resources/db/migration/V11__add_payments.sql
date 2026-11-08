-- ============================================================================
-- V11__add_payments.sql
-- Идемпотентный учёт успешных платежей Telegram и выставленных счетов.
-- ============================================================================

CREATE TABLE payment (
    id                          BIGSERIAL    PRIMARY KEY,
    user_id                     BIGINT       NOT NULL,
    telegram_payment_charge_id  VARCHAR(255) NOT NULL,
    provider_payment_charge_id  VARCHAR(255) NULL,
    invoice_payload             VARCHAR(64)  NULL,
    amount                      BIGINT       NOT NULL,
    currency                    VARCHAR(3)   NOT NULL,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT ux_payment_charge UNIQUE (telegram_payment_charge_id),
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);

CREATE INDEX idx_payment_user_id ON payment (user_id);

COMMENT ON TABLE  payment                             IS 'Успешные платежи Telegram (донаты)';
COMMENT ON COLUMN payment.telegram_payment_charge_id  IS 'Уникальный ID платежа Telegram для идемпотентности';
COMMENT ON COLUMN payment.invoice_payload             IS 'Payload выставленного счёта, по которому прошла оплата';
COMMENT ON COLUMN payment.amount                      IS 'Сумма в минимальных единицах валюты (копейки/звёзды)';

-- Счёт, выставленный ботом. PreCheckoutQuery проверяется по payload/сумме/валюте,
-- чтобы подтверждать только реально выставленные счета.
CREATE TABLE payment_invoice (
    id               BIGSERIAL    PRIMARY KEY,
    payload          VARCHAR(64)  NOT NULL,
    telegram_chat_id BIGINT       NOT NULL,
    amount           BIGINT       NOT NULL,
    currency         VARCHAR(3)   NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'CREATED',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    paid_at          TIMESTAMPTZ  NULL,
    CONSTRAINT ux_payment_invoice_payload UNIQUE (payload),
    CONSTRAINT chk_payment_invoice_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_invoice_status CHECK (status IN ('CREATED', 'PAID'))
);

CREATE INDEX idx_payment_invoice_chat_id ON payment_invoice (telegram_chat_id);
CREATE INDEX idx_payment_invoice_status ON payment_invoice (status);

COMMENT ON TABLE  payment_invoice            IS 'Счета, выставленные ботом (для проверки PreCheckoutQuery)';
COMMENT ON COLUMN payment_invoice.payload    IS 'Значение invoice_payload из SendInvoice';
COMMENT ON COLUMN payment_invoice.amount     IS 'Сумма в минимальных единицах валюты (копейки/звёзды)';
COMMENT ON COLUMN payment_invoice.status     IS 'CREATED | PAID';
