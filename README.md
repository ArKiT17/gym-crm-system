# Gym CRM System

Gym CRM System is a REST API for managing gym trainees, trainers, and training sessions. It is backend-only: use Swagger UI, Postman, or another HTTP client.

## Features

- Trainee and trainer registration with generated credentials.
- JWT authentication, logout with token revocation, BCrypt password hashing, and brute-force protection.
- Self-service profile, password, and activation-status management.
- Trainee-to-trainer assignment, training creation, and filtered training history.
- Seeded training types: `fitness`, `yoga`, `zumba`, `stretching`, `resistance`.
- Health checks, Prometheus metrics, correlation IDs, and `ProblemDetail` error responses.

## Stack

Java 21, Maven, Spring Boot 4.1, Spring Web, Spring Data JPA/Hibernate, MySQL, Spring Security, JWT HS256, OpenAPI/Swagger, Actuator, Micrometer/Prometheus, JUnit, JaCoCo, Docker, and Docker Compose. H2 is used for tests.

## Domain model

`User` holds credentials and active status. It belongs to either a `Trainee` (date of birth, address) or a `Trainer` (specialization). Trainees and trainers have a many-to-many relationship. A `Training` records one trainee, one trainer, name, date, duration, and training type.

## Run with Docker

Requirements: Docker Desktop or Docker Engine with Docker Compose.

```bash
cp .env.example .env
# Edit .env and replace example passwords and JWT_SECRET.
docker compose up --build
```

The API is served on `http://localhost:8080`. Compose starts MySQL and the API in separate containers. The API waits for MySQL's health check, while database data persists in the `mysql_data` volume.

```bash
docker compose down      # stop containers
docker compose down -v   # also remove all local database data
```

## Run locally

Requirements: Java 21 and MySQL.

```bash
export DB_URL='jdbc:mysql://localhost:3306/gym_crm'
export DB_USERNAME='gym_app'
export DB_PASSWORD='change-me'
export JWT_SECRET='a-random-secret-containing-at-least-32-bytes'
export CORS_ALLOWED_ORIGINS='http://localhost:3000'
./mvnw spring-boot:run
```

The default profile is `local`. Profiles `dev`, `stg`, and `prod` use the respective `DEV_*`, `STG_*`, and `PROD_*` database environment variables. Do not use the local default secrets outside disposable local development.

## API documentation and access

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- Postman collection: [postman/Gym CRM System.postman_collection.json](postman/Gym%20CRM%20System.postman_collection.json)

Public endpoints:

- `POST /api/trainees`, `POST /api/trainers` - registration;
- `POST /api/auth/login` - login;
- `GET /api/training-types` - training type catalogue;
- Swagger UI and OpenAPI endpoints.

All other API endpoints require a JWT:

```http
Authorization: Bearer <accessToken>
```

Users can access only resources associated with their own `username`. Either participant can create a training. `POST /api/auth/logout` revokes the current token.

### Typical workflow

1. Call `GET /api/training-types` and select a trainer `specializationId`.
2. Register a trainer and trainee; save the generated username and password.
3. Call `POST /api/auth/login` and use its `accessToken` as a Bearer token.
4. Make protected requests as the trainee or trainer who owns the requested resource.

```bash
curl -X POST http://localhost:8080/api/trainees \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Anna","lastName":"Koval","dateOfBirth":"1998-04-12","address":"Kyiv"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"anna.koval","password":"<password-from-registration>"}'
```

## Endpoints

| Method and path | Description |
| --- | --- |
| `POST /api/trainees` | Register a trainee: `firstName`, `lastName`, optional `dateOfBirth`, `address` |
| `GET/PUT/DELETE /api/trainees/{username}` | Read, update, or delete own trainee profile |
| `GET /api/trainees/{username}/available-trainers` | Active trainers not assigned to trainee |
| `PUT /api/trainees/{username}/trainers` | Replace trainers: `{"trainerUsernames":["..."]}` |
| `GET /api/trainees/{username}/trainings` | Filters: `periodFrom`, `periodTo`, `trainerName`, `trainingType` |
| `POST /api/trainers` | Register trainer: `firstName`, `lastName`, `specializationId` |
| `GET/PUT /api/trainers/{username}` | Read or update own trainer profile; specialization cannot change |
| `GET /api/trainers/{username}/trainings` | Filters: `periodFrom`, `periodTo`, `traineeName` |
| `POST /api/trainings` | Create a training for active trainee and trainer |
| `GET /api/training-types` | Public training type catalogue |
| `POST /api/auth/login` | Return `accessToken`, `tokenType`, `expiresIn` |
| `POST /api/auth/logout` | Revoke current token; returns `204 No Content` |
| `PUT /api/user/{username}/password` | Change own password: `oldPassword`, `newPassword` |
| `PATCH /api/user/{username}/status` | Change own status: `{"active":true}` or `false` |

Dates use ISO-8601 `YYYY-MM-DD`. An invalid range returns `400 Bad Request`.

## Monitoring and errors

Errors follow the `ProblemDetail` JSON format. Main statuses: `400` invalid input, `401` invalid or missing authentication, `403` access to another user's resource, `404` missing resource, `409` state conflict, and `429` too many login attempts.

Every response includes `X-Transaction-Id`, which may be supplied by the client to correlate the request with logs.

Protected Actuator endpoints:

- `GET /actuator/health` - application and training-type seed status;
- `GET /actuator/metrics` - registered metrics;
- `GET /actuator/prometheus` - Prometheus output, including `gym.trainers.active`, `gym.trainees.active`, and `gym.trainings.created`.

## Tests

```bash
./mvnw test
./mvnw verify
```

`verify` creates a JaCoCo report and enforces 80% branch coverage for the controller, facade, service, and utility layers.
