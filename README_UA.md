# Gym CRM System

Gym CRM System - REST API для керування клієнтами спортивного клубу, тренерами та тренуваннями. Це лише бекенд: використовуйте Swagger UI, Postman або інший HTTP-клієнт.

## Можливості

- Реєстрація клієнтів і тренерів з автоматично згенерованими обліковими даними.
- JWT-автентифікація, logout із відкликанням токена, BCrypt і захист від перебору паролів.
- Керування власним профілем, паролем і статусом активності.
- Призначення тренерів клієнту, створення тренувань та фільтрація історії.
- Початковий довідник типів: `fitness`, `yoga`, `zumba`, `stretching`, `resistance`.
- Health-check, Prometheus-метрики, кореляційні ID і помилки `ProblemDetail`.

## Технології

Java 21, Maven, Spring Boot 4.1, Spring Web, Spring Data JPA/Hibernate, MySQL, Spring Security, JWT HS256, OpenAPI/Swagger, Actuator, Micrometer/Prometheus, JUnit, JaCoCo, Docker і Docker Compose. Для тестів застосовується H2.

## Предметна модель

`User` містить credentials та статус активності. Він пов'язаний або з `Trainee` (дата народження, адреса), або з `Trainer` (спеціалізація). Клієнти й тренери мають зв'язок many-to-many. `Training` зберігає клієнта, тренера, назву, дату, тривалість і тип тренування.

## Запуск через Docker

Потрібен Docker Desktop або Docker Engine із Docker Compose.

```bash
cp .env.example .env
# Відредагуйте .env: замініть приклади паролів і JWT_SECRET.
docker compose up --build
```

API буде доступний за `http://localhost:8080`. Compose запускає MySQL та застосунок в окремих контейнерах. API чекає на MySQL healthcheck; дані БД зберігаються у volume `mysql_data`.

```bash
docker compose down      # зупинити контейнери
docker compose down -v   # також видалити всі локальні дані БД
```

## Локальний запуск

Потрібні Java 21 та MySQL.

```bash
export DB_URL='jdbc:mysql://localhost:3306/gym_crm'
export DB_USERNAME='gym_app'
export DB_PASSWORD='change-me'
export JWT_SECRET='a-random-secret-containing-at-least-32-bytes'
export CORS_ALLOWED_ORIGINS='http://localhost:3000'
./mvnw spring-boot:run
```

За замовчуванням використовується профіль `local`. Профілі `dev`, `stg` і `prod` читають відповідні змінні БД: `DEV_*`, `STG_*`, `PROD_*`. Не використовуйте локальні дефолтні секрети поза одноразовою локальною розробкою.

## Документація API та доступ

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- Postman collection: [postman/Gym CRM System.postman_collection.json](postman/Gym%20CRM%20System.postman_collection.json)

Без авторизації доступні:

- `POST /api/trainees`, `POST /api/trainers` - реєстрація;
- `POST /api/auth/login` - вхід;
- `GET /api/training-types` - довідник типів;
- Swagger UI та OpenAPI-ендпоїнти.

Усі інші API-ендпоїнти вимагають JWT:

```http
Authorization: Bearer <accessToken>
```

Користувач бачить лише ресурси, прив'язані до власного `username`. Створити тренування може будь-який з його учасників. `POST /api/auth/logout` відкликає поточний токен.

### Типовий сценарій

1. Виконайте `GET /api/training-types` і виберіть `specializationId` тренера.
2. Зареєструйте тренера та клієнта і збережіть згенеровані логін і пароль.
3. Виконайте `POST /api/auth/login` і використайте `accessToken` як Bearer token.
4. Виконуйте захищені запити від імені клієнта або тренера, якому належить ресурс.

```bash
curl -X POST http://localhost:8080/api/trainees \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Anna","lastName":"Koval","dateOfBirth":"1998-04-12","address":"Kyiv"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"anna.koval","password":"<пароль-з-реєстрації>"}'
```

## Ендпоїнти

| Метод і шлях | Опис |
| --- | --- |
| `POST /api/trainees` | Реєстрація клієнта: `firstName`, `lastName`, необов'язкові `dateOfBirth`, `address` |
| `GET/PUT/DELETE /api/trainees/{username}` | Перегляд, оновлення або видалення власного профілю клієнта |
| `GET /api/trainees/{username}/available-trainers` | Активні тренери, не призначені клієнту |
| `PUT /api/trainees/{username}/trainers` | Повна заміна тренерів: `{"trainerUsernames":["..."]}` |
| `GET /api/trainees/{username}/trainings` | Фільтри: `periodFrom`, `periodTo`, `trainerName`, `trainingType` |
| `POST /api/trainers` | Реєстрація тренера: `firstName`, `lastName`, `specializationId` |
| `GET/PUT /api/trainers/{username}` | Перегляд або оновлення власного профілю; спеціалізацію не можна змінити |
| `GET /api/trainers/{username}/trainings` | Фільтри: `periodFrom`, `periodTo`, `traineeName` |
| `POST /api/trainings` | Створення тренування для активних клієнта й тренера |
| `GET /api/training-types` | Публічний довідник типів |
| `POST /api/auth/login` | Повертає `accessToken`, `tokenType`, `expiresIn` |
| `POST /api/auth/logout` | Відкликає поточний JWT; повертає `204 No Content` |
| `PUT /api/user/{username}/password` | Зміна власного пароля: `oldPassword`, `newPassword` |
| `PATCH /api/user/{username}/status` | Зміна власного статусу: `{"active":true}` або `false` |

Дати передаються у форматі ISO-8601 `YYYY-MM-DD`. Некоректний інтервал дат повертає `400 Bad Request`.

## Моніторинг і помилки

Помилки мають формат `ProblemDetail` JSON. Основні статуси: `400` - невалідний ввід, `401` - відсутня або невалідна автентифікація, `403` - чужий ресурс, `404` - ресурс не існує, `409` - конфлікт стану, `429` - перевищено ліміт спроб входу.

Кожна відповідь містить `X-Transaction-Id`. Його можна передати у запиті, щоб пов'язати дію клієнта з логами.

Захищені Actuator-ендпоїнти:

- `GET /actuator/health` - стан застосунку та початкового довідника типів;
- `GET /actuator/metrics` - перелік метрик;
- `GET /actuator/prometheus` - формат Prometheus із `gym.trainers.active`, `gym.trainees.active`, `gym.trainings.created`.

## Тести

```bash
./mvnw test
./mvnw verify
```

`verify` створює JaCoCo-звіт і вимагає 80% branch coverage для controller, facade, service та utility шарів.
