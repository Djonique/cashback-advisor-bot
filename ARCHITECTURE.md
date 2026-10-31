# ARCHITECTURE.md — архитектура

## Пакеты (базовый: com.djonique.cashbackadvbot)
- `config` — BotConfig, AppConfig (ZoneId), AppProperties
- `bot` — Telegram-слой
    - `handler` — MenuHandler, SetupDialogHandler, CardHandler, SupportHandler
    - `keyboard` — построение inline-клавиатур
    - `state` — DialogState (enum)
- `service` — SelectionService, CashbackAdvisorService, UserService, UserStateService
- `repository` — Spring Data JPA
- `domain` — JPA-сущности (8 штук)
- `dto` — record'ы (SelectionInput, RecommendationDto)
- `exception` — ValidationException

## Схема БД (8 таблиц)
1. `bank` (id, name, slug, display_order)
2. `category` (id, name, slug)
3. `bank_category` (id, bank_id, category_id) — БЕЗ ПРОЦЕНТОВ
4. `bank_category_mcc` (id, bank_category_id, mcc_code)
5. `bank_excluded_mcc` (id, bank_id, mcc_code)
6. `app_user` (id, telegram_chat_id, username, created_at)
7. `user_bank_selection` (id, user_id, bank_id, category_id, year_month, cashback_percent, created_at)
8. `user_state` (user_id, state, context JSONB, updated_at)

Актуальная DDL — в `docs/schema.sql`.