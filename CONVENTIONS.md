# CONVENTIONS.md — стиль и версии

## Версии (зафиксировано)
- Java 21
- Spring Boot 4.1.2
- Hibernate 7.4.13.Final
- Flyway 12.4.0
- PostgreSQL 16
- telegrambots 6.9.7.1
- Базовый пакет: `com.djonique.cashbackadvbot`

## Именование
- Сервисы: `*Service`, Хендлеры: `*Handler`, Репозитории: `*Repository`, DTO: `*Dto` / `*Input`
- SQL: таблицы/колонки в `snake_case`, индексы `idx_`, уникальные `ux_`, внешние ключи `fk_`, проверки `chk_`.

## Lombok
- Сущности: `@Getter`, `@Setter`, `@NoArgsConstructor`. **`@Data` ЗАПРЕЩЁН**.
- Сервисы: `@Slf4j`, `@RequiredArgsConstructor`.
- DTO: `record` (без Lombok).

## Время
- Всегда `YearMonth.now(ZoneId.of("Europe/Moscow"))`. Никаких `LocalDate.now()` без зоны.

## Тесты
- JUnit 6 + Mockito.
- Названия: `shouldDoSomething_whenCondition()`.
- На каждый сервис — минимум happy path + граничный случай.