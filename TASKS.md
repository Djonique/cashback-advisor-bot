# TASKS.md — план работ

## 🔄 Текущий спринт: №1 — Схема БД
- [x] Docker Compose с PostgreSQL 16
- [x] Настроен `application.yml` (jpa open-in-view: false, flyway locations)
- [x] Написана миграция `V1__init.sql` (8 таблиц)
- [ ] Запустить приложение и проверить, что Flyway накатил V1
- [ ] Создать `docs/schema.sql` (копия DDL)

## 📋 Следующий спринт: №2 — JPA-сущности и каркас бота
- [ ] 8 JPA-сущностей с Lombok и `@ManyToOne(fetch = LAZY)`
- [ ] Репозитории Spring Data JPA
- [ ] `BotConfig` (регистрация `TelegramBot` бина)
- [ ] `/start` → главное меню с 3 кнопками

## ⏸ Отложено (НЕ предлагать!)
- Kafka, REST API, ЮKassa, Ozon Premium, учет лимитов (1500/3000 руб), Веб-интерфейс.