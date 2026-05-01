# Tech Challenge — User Management API

Shared restaurant management backend for the FIAP Postgraduate in Java Architecture (Phase 1 Tech Challenge). The system provides a complete user-management API — registration, authentication, profile updates, password management, and name search — built with Spring Boot 3.3, secured with Spring Security 6 and stateless JWT (HS256 via JJWT 0.12), and documented with OpenAPI 3.1 / Swagger UI.

---

## Architecture Overview

The project follows a pragmatic hexagonal (ports-and-adapters) architecture in a single Maven module. Dependency direction is strictly enforced: `infrastructure` → `application` → `domain`. The domain layer has zero framework dependencies; all cross-layer communication goes through port interfaces. An ArchUnit test validates these rules on every build.

For full details see [`specs/technical/01-architecture.md`](specs/technical/01-architecture.md).

---

## Stack & Versions

| Component | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.5 |
| Spring Security | 6.x (stateless, JWT filter) |
| JJWT (HS256) | 0.12.6 |
| Spring Data JPA + Hibernate | (Boot-managed) |
| Flyway | 10.x |
| PostgreSQL driver | (Boot-managed) |
| H2 (dev/test) | (Boot-managed) |
| springdoc-openapi | 2.6.0 |
| Testcontainers | 1.20.4 |
| ArchUnit | 1.3.0 |

---

## Running Locally (dev profile — H2)

The `dev` profile uses an in-memory H2 database and Flyway migrations. No Docker required.

```bash
# 1. Set the JWT signing secret (required even in dev)
export JWT_SECRET=$(openssl rand -base64 48)

# 2. Start the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API is available at `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

To use a fixed secret during development, add it to your shell profile:

```bash
export JWT_SECRET=my-dev-only-secret-at-least-32-bytes-long
```

---

## Running with Docker Compose (hom profile — PostgreSQL)

Docker Compose starts the application against a real PostgreSQL instance (`postgres:16-alpine`). The `hom` Spring profile is activated automatically.

```bash
# 1. Copy the example environment file
cp .env.example .env

# 2. Set a real JWT secret (replace the placeholder value)
#    Generate one with:
export JWT_SECRET=$(openssl rand -base64 48)
#    Then paste the output into .env as:  JWT_SECRET=<value>

# 3. Build and start
docker compose up --build
```

Wait until the log shows `Started RmsApplication`. The API is then at `http://localhost:8080`.

**Fail-fast behaviour:** Compose will refuse to start if `JWT_SECRET` is missing or empty and will print:

```
JWT_SECRET is required - generate with: openssl rand -base64 48
```

---

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | yes | `hom` (in Compose) | `dev` / `hom` / `prod` |
| `DB_URL` | hom / prod | — | JDBC URL (e.g. `jdbc:postgresql://db:5432/techchallenge`) |
| `DB_USERNAME` | hom / prod | — | Database user |
| `DB_PASSWORD` | hom / prod | — | Database password |
| `JWT_SECRET` | yes | — (Compose fails fast) | HS256 signing secret, minimum 32 bytes |
| `JWT_EXPIRATION_SECONDS` | no | `3600` | Token TTL in seconds |

---

## API Documentation (Swagger)

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

To call protected endpoints from the UI:

1. Call `POST /api/v1/auth/login` to obtain a JWT token.
2. Click the **Authorize** button (top right of the Swagger UI page).
3. Paste the token value into the **bearerAuth** field and click **Authorize**.
4. All subsequent requests will include the `Authorization: Bearer <token>` header automatically.

Swagger UI is **disabled** in the `prod` profile.

---

## Running Tests

```bash
# Unit tests only (fast, no Docker required)
./mvnw test

# Full test suite including integration tests (requires Docker for Testcontainers)
./mvnw verify
```

The test suite includes unit tests, Spring `@WebMvcTest` slices, ArchUnit architecture rules, and Testcontainers-based integration tests against a real PostgreSQL instance.

---

## Postman Collection

The collection covers every acceptance criterion including the full JWT flow. Both files live in the `postman/` directory.

### Import into Postman Desktop

1. Open Postman → **Import**.
2. Select both files from the `postman/` directory:
   - `tech-challenge.postman_collection.json`
   - `tech-challenge.postman_environment.json`
3. In the top-right environment dropdown, select **Tech Challenge — User Management API (local)**.
4. Open the **Collection Runner**, select the collection, and run with **all folders in order** (Folder 0 must run first — it seeds the admin and captures the JWT token).

### Run with Newman

```bash
# Install Newman (once)
npm install -g newman

# Run the full collection against the local server
newman run postman/tech-challenge.postman_collection.json \
  -e postman/tech-challenge.postman_environment.json
```

Newman requires the application to be running (`docker compose up` or `./mvnw spring-boot:run`).

---

## Project Structure

The project follows a single-module Maven layout. Source code is organized by hexagonal layer; specifications live alongside the code for traceability.

```
src/
├── main/
│   ├── java/com/fiap/rms/
│   │   ├── application/       # Use-case interfaces and implementations (no framework deps)
│   │   ├── domain/            # Entities, value objects, domain exceptions
│   │   ├── infrastructure/    # Adapters (JPA, JWT, REST controllers), Spring config
│   │   └── shared/            # Cross-cutting concerns (exception handlers)
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-hom.yml
│       ├── application-prod.yml
│       └── db/migration/      # Flyway SQL migrations
└── test/
    └── java/com/fiap/rms/
        ├── architecture/      # ArchUnit dependency-rule tests
        ├── application/       # Use-case unit tests
        ├── domain/            # Domain model unit tests
        └── infrastructure/    # WebMvcTest slices + Testcontainers ITs

specs/
├── product/                   # Product requirements and acceptance criteria
├── technical/                 # Architecture decisions, conventions, ADRs
└── modules/                   # Per-module implementation specs (M01–M11)
```

---

## Technical Report

The technical report PDF documenting architecture decisions, design rationale, and challenge requirements is located at:

```
report/tech-challenge-report.pdf
```

Add your PDF to the `report/` directory before final submission.
