# Лабораторная работа №4 — Flyway: миграции базы данных

В существующий проект Spring Boot (CRUD «Любительский футбол» из ЛР №3: команды и игроки,
MySQL) интегрирован **Flyway**. Теперь схемой базы данных управляют версионированные
SQL-скрипты миграций, а Hibernate только проверяет соответствие сущностей схеме
(`ddl-auto: validate`). Добавлена таблица `PRODUCT` (миграции из методички) и сущность
`Product` с REST-эндпоинтами только для чтения.

## Технологии

- Spring Boot 3.5 (Web, Data JPA, Validation), Java 17, Maven;
- **Flyway** (`flyway-core`, `flyway-mysql`; версия управляется Spring Boot parent);
- MySQL 8.4 в Docker (основной профиль), H2 in-memory в режиме MySQL (профиль `test`);
- Lombok, MapStruct, jackson-databind-nullable;
- JUnit 5, Mockito, MockMvc, Instancio, Datafaker, JsonUnit.

## Как устроен Flyway в проекте

Зависимости в `pom.xml`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

Настройки в `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:${MYSQL_HOST_PORT:3306}/football?...}
    username: ${DB_USERNAME:football}
    password: ${DB_PASSWORD:football}
  flyway:
    enabled: true                     # включает миграцию базы данных Flyway
    locations: classpath:db/migration # где искать скрипты
    validate-on-migrate: true         # проверять контрольные суммы применённых скриптов
  jpa:
    hibernate:
      ddl-auto: validate              # Hibernate не создаёт таблицы, только сверяет
```

Скрипты миграций — `src/main/resources/db/migration/`, имена по соглашению
`V<версия>__<описание>.sql` (версии уникальны, порядок выполнения — по версии):

| Версия | Файл | Что делает |
|--------|------|------------|
| 1 | `V1__CREATE_PRODUCT_TABLE.sql` | создаёт таблицу `PRODUCT` (id, name, description, price, created_at, modified_at) |
| 1.1 | `V1.1__INSERT_PRODUCT_DATA.sql` | вставляет 3 продукта: Product 1 (10.99), Product 2 (19.99), Product 3 (29.99) |
| 2 | `V2__CREATE_TEAMS_AND_PLAYERS_TABLES.sql` | таблицы `teams` и `players` существующего проекта (раньше их создавал Hibernate) |

При запуске приложения Flyway находит скрипты, создаёт служебную таблицу
`flyway_schema_history` и применяет ещё не выполненные миграции. Каждая миграция
выполняется только один раз: при повторном запуске в логе будет
`Schema `football` is up to date. No migration necessary.` Уже применённые скрипты
изменять нельзя — `validate-on-migrate` обнаружит изменение контрольной суммы;
изменения схемы оформляются новым скриптом с большей версией (например, `V3__...sql`).

## Требования

- JDK 17, Maven 3.8+;
- Docker Engine и Docker Compose v2 (для MySQL);
- свободные порты **8081** (приложение) и **3306** (MySQL).

## 1. Запуск MySQL в Docker

В `docker-compose.yml` compose-проект называется `lab4-flyway` (свои контейнер
`lab4-flyway-mysql-1` и том `lab4-flyway_mysql_data`, не пересекаются с ЛР №3).
Для лабораторной поднимается только сервис `mysql`:

```bash
cd project
docker compose up -d mysql
docker compose ps            # mysql должен стать (healthy)
```

Параметры MySQL (учебные значения): база `football`, пользователь `football`,
пароль `football`, root-пароль `rootpassword`, порт хоста `3306`.
MySQL запускается с `--lower-case-table-names=1`, чтобы таблица `PRODUCT` из миграции и имя
`product`, которое использует Hibernate, были одной таблицей (на Linux MySQL по умолчанию
различает регистр имён таблиц). Параметр применяется только при создании нового тома.

> Если порт 3306 занят, задайте другой порт хоста:
> `MYSQL_HOST_PORT=3307 docker compose up -d mysql`, и запускайте приложение с той же
> переменной: `MYSQL_HOST_PORT=3307 mvn spring-boot:run`.

## 2. Запуск приложения

```bash
mvn spring-boot:run
```

Приложение слушает порт **8081**. В логе запуска видны сообщения Flyway:

```
Creating Schema History table `football`.`flyway_schema_history` ...
Current version of schema `football`: << Empty Schema >>
Migrating schema `football` to version "1 - CREATE PRODUCT TABLE"
Migrating schema `football` to version "1.1 - INSERT PRODUCT DATA"
Migrating schema `football` to version "2 - CREATE TEAMS AND PLAYERS TABLES"
Successfully applied 3 migrations to schema `football`, now at version v2
Started Lab4Application in ... seconds
```

Переменные окружения:

| Переменная | По умолчанию |
|------------|--------------|
| `DB_URL` | `jdbc:mysql://localhost:${MYSQL_HOST_PORT}/football?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8` |
| `MYSQL_HOST_PORT` | `3306` |
| `DB_USERNAME` / `DB_PASSWORD` | `football` / `football` |
| `SERVER_PORT` | `8081` |

## 3. Проверка результата миграций в MySQL

```bash
# история миграций
docker compose exec mysql mysql -ufootball -pfootball football \
  -e "SELECT installed_rank, version, description, script, success FROM flyway_schema_history;"

# таблица PRODUCT, созданная и заполненная миграциями
docker compose exec mysql mysql -ufootball -pfootball football -e "SELECT * FROM PRODUCT;"
```

Ожидаемо: в `flyway_schema_history` строки с версиями `1`, `1.1`, `2` и `success = 1`;
в `PRODUCT` — три записи `Product 1`, `Product 2`, `Product 3`.

## 4. Демонстрация через REST (curl)

```bash
# продукты из миграции V1.1
curl -s http://localhost:8081/api/products
curl -s http://localhost:8081/api/products/1
curl -s -w '\n%{http_code}\n' http://localhost:8081/api/products/999   # Product Not Found: 999 / 404

# CRUD существующего проекта работает на таблицах из миграции V2
curl -s -X POST http://localhost:8081/api/teams \
  -H 'Content-Type: application/json' \
  -d '{"name":"ФК Немига","city":"Минск","foundedYear":2015}'
curl -s -X POST http://localhost:8081/api/players \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Иван","lastName":"Петров","position":"Нападающий","shirtNumber":9,"teamId":1}'
curl -s http://localhost:8081/api/teams/1/players
curl -s -X PUT http://localhost:8081/api/teams/1 -H 'Content-Type: application/json' -d '{"city":"Брест"}'
curl -s -w '%{http_code}\n' -X DELETE http://localhost:8081/api/players/1   # 204
curl -s -w '%{http_code}\n' -X DELETE http://localhost:8081/api/teams/1     # 204
```

Полный список эндпоинтов:

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/api/products`, `/api/products/{id}` | продукты (только чтение), 404 если нет |
| GET/POST | `/api/teams` | список / создание команды |
| GET/PUT/DELETE | `/api/teams/{id}` | просмотр / частичное обновление / удаление |
| GET | `/api/teams/{id}/players` | игроки команды |
| GET/POST | `/api/players` | список / создание игрока |
| GET/PUT/DELETE | `/api/players/{id}` | просмотр / частичное обновление / удаление |

## 5. Остановка

```bash
# приложение — Ctrl+C в терминале mvn spring-boot:run
docker compose stop          # остановить MySQL (данные сохраняются)
docker compose down -v       # удалить контейнер и том (следующий запуск применит миграции заново)
```

## Тесты

```bash
mvn test
```

Тесты используют профиль `test`: H2 in-memory в режиме `MODE=MySQL`. Flyway включён и в
тестах — применяет те же миграции из `db/migration`, Hibernate валидирует схему.

- `FlywayMigrationTest` — в `flyway_schema_history` есть версии `1`, `1.1`, `2`, нет
  ожидающих миграций, в `PRODUCT` ровно 3 записи с данными из методички;
- `ProductsControllerTest` — `GET /api/products`, `/api/products/{id}`, 404;
- `TeamsControllerTest`, `PlayersControllerTest` — интеграционные тесты CRUD из ЛР №3;
- `ProductServiceTest`, `TeamServiceTest`, `PlayerServiceTest` — модульные (Mockito).

## Структура проекта

```
project/
├── docker-compose.yml          # compose-проект lab4-flyway: mysql (+ app при необходимости)
├── Dockerfile, .dockerignore   # образ приложения (из ЛР №3)
├── pom.xml                     # + flyway-core, flyway-mysql
└── src/
    ├── main/java/io/hexlet/spring/
    │   ├── Lab4Application.java
    │   ├── config/JacksonConfig.java
    │   ├── controller/         # TeamsController, PlayersController, ProductsController
    │   ├── dto/                # Team*/Player* DTO, ProductDTO
    │   ├── exception/          # GlobalExceptionHandler (404/409/400)
    │   ├── mapper/             # MapStruct-мапперы, JsonNullableMapper, ReferenceMapper
    │   ├── model/              # Team, Player, Product, BaseEntity
    │   ├── repository/         # TeamRepository, PlayerRepository, ProductRepository
    │   └── service/            # TeamService, PlayerService, ProductService
    ├── main/resources/
    │   ├── application.yml     # MySQL + Flyway + ddl-auto=validate
    │   └── db/migration/       # V1__..., V1.1__..., V2__...
    └── test/
        ├── java/io/hexlet/spring/{controller,service,migration}/
        └── resources/application-test.yml   # H2 (MODE=MySQL) + Flyway
```
