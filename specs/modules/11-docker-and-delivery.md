# M11 — Docker & Delivery Package

## Goal
Wrap the application for delivery: multi-stage Dockerfile,
Docker Compose orchestrating app + PostgreSQL, a Postman collection
covering every acceptance criterion (including the JWT flow), and
a README that gets a fresh reviewer running the system in under
five minutes.

## Scope
- `Dockerfile` (multi-stage: build with Maven, run with JRE).
- `docker-compose.yml` (services: `app`, `db`).
- `.dockerignore`.
- `postman/tech-challenge.postman_collection.json`.
- `postman/tech-challenge.postman_environment.json`.
- Updated `README.md`.

## Dockerfile
- Stage 1: `maven:3.9-eclipse-temurin-21` — copy `pom.xml`, run
  `mvn -B dependency:go-offline`, copy sources, run
  `mvn -B -DskipTests package`.
- Stage 2: `eclipse-temurin:21-jre-alpine` — copy the jar from
  stage 1, expose 8080, `ENTRYPOINT ["java","-jar","/app/app.jar"]`.
- Default profile: `hom` (so `docker compose up` runs against
  postgres, not H2).

## docker-compose.yml
- Service `db` (postgres:16-alpine):
    - Env: `POSTGRES_DB=techchallenge`, `POSTGRES_USER=postgres`,
      `POSTGRES_PASSWORD=postgres`.
    - Volume `pgdata:/var/lib/postgresql/data`.
    - Healthcheck on `pg_isready`.
- Service `app`:
    - Build from local Dockerfile.
    - Env:
        - `SPRING_PROFILES_ACTIVE=hom`
        - `DB_URL=jdbc:postgresql://db:5432/techchallenge`
        - `DB_USERNAME=postgres`
        - `DB_PASSWORD=postgres`
        - `JWT_SECRET=${JWT_SECRET:?JWT_SECRET is required}`
        - `JWT_EXPIRATION_SECONDS=3600`
    - Depends on `db` (condition: service_healthy).
    - Ports: `8080:8080`.
- A `.env.example` file at the repo root documents every variable
  with safe placeholder values. The README explains how to copy
  it to `.env`.

## Postman collection
The collection ships with a **collection-level Authorization** of
type `Bearer Token` whose value is `{{token}}`. Every protected
request inherits this automatically.

The environment file ships with these variables (all empty
initially, populated by scripts as the runner progresses):
- `baseUrl` = `http://localhost:8080/api/v1`
- `token`
- `customerId`
- `ownerId`
- `adminId`

Folders are executed in order. Each request includes a Tests script
asserting the status code and key fields, and (where relevant)
saving values into the environment.

### Folder 0 — Auth setup (must run first)
Establishes the `{{token}}` for all protected calls below.
- **Register admin (seed)** — public endpoint, expect 201,
  save `adminId`.
- **Login admin** — expect 200, Tests script:
```js
  const json = pm.response.json();
  pm.test("token present", () => pm.expect(json.token).to.be.a("string"));
  pm.environment.set("token", json.token);
```
- **Login admin — wrong password** — expect 401. MUST NOT update
  `token`.
- **Login — unknown user** — expect 401. Body byte-identical to the
  wrong-password case.

### Folder 1 — User registration
- **Register customer (valid)** — public, expect 201, save
  `customerId`.
- **Register restaurant owner (valid)** — public, expect 201, save
  `ownerId`.
- **Register duplicate email** — expect 409.
- **Register missing required field** — expect 400.
- **Register invalid role** — expect 400.

### Folder 2 — Update user data
- **Update existing user** — protected, expect 200. Tests script
  asserts `updatedAt` is later than the value captured during
  registration.
- **Update non-existing user** — protected, expect 404.
- **Update payload containing `password` field** — protected,
  expect 200. Followed by a login attempt with the OLD password
  asserting 200 (proving the password did not change).

### Folder 3 — Change password
- **Valid change** — protected, expect 204.
- **Login with new password** — expect 200 (smoke check that the
  change actually took effect).
- **Change for non-existing user** — protected, expect 404.
- **Change with weak password** — protected, expect 400.

### Folder 4 — Delete user
- **Delete existing user** — protected, expect 204.
- **Delete non-existing user** — protected, expect 404.

### Folder 5 — Search by name
- **Search hitting multiple users** — protected, expect 200,
  length > 0.
- **Search with no match** — protected, expect 200, length == 0.
- **Case-insensitive search** — protected, expect 200, length > 0.

### Folder 6 — Authorization edge cases
Validates the JWT filter behavior in addition to credential checks.
- **Protected endpoint without Authorization header** — expect 401
  with `type=/errors/unauthorized`.
- **Protected endpoint with malformed token** — expect 401.
- **Protected endpoint with expired token** — expect 401. (Use a
  hard-coded expired JWT generated once by the developer; document
  it in the request description.)

### Postman runner expectation
Running the entire collection in order on a fresh database must
finish with every test green. The README documents the command:
newman run postman/tech-challenge.postman_collection.json
-e postman/tech-challenge.postman_environment.json

## README.md
Sections in order:
1. Project name + one-paragraph description.
2. Architecture overview (link to
   `specs/technical/01-architecture.md`).
3. Stack & versions (mention Spring Security + JWT explicitly).
4. How to run locally (dev, with H2):
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   Note: `dev` still requires `JWT_SECRET` — the README shows how to
   export it inline:
   export JWT_SECRET=$(openssl rand -base64 48)
5. How to run with Docker Compose (hom profile, Postgres):
   cp .env.example .env
   edit .env, set JWT_SECRET to a 32+ byte random value
   docker compose up --build
6. **Environment variables table:**

   | Variable                  | Required | Default                | Description                              |
   |---------------------------|----------|------------------------|------------------------------------------|
   | `SPRING_PROFILES_ACTIVE`  | yes      | `hom` (in compose)     | `dev` / `hom` / `prod`                   |
   | `DB_URL`                  | hom/prod | —                      | JDBC URL                                 |
   | `DB_USERNAME`             | hom/prod | —                      | DB user                                  |
   | `DB_PASSWORD`             | hom/prod | —                      | DB password                              |
   | `JWT_SECRET`              | yes      | — (compose fails fast) | HS256 secret, ≥ 32 bytes                 |
   | `JWT_EXPIRATION_SECONDS`  | no       | `3600`                 | Token TTL                                |

7. Swagger URL: `http://localhost:8080/swagger-ui.html`. Includes a
   short note: "Click 'Authorize', paste the token returned by
   `POST /api/v1/auth/login`, and you can call protected endpoints
   from the UI."
8. How to run tests (`./mvnw test`).
9. How to import and run the Postman collection:
    - Open Postman → Import → select both files in `postman/`.
    - Select the imported environment.
    - Run the collection in order (Folder 0 first).
    - Or via Newman: command shown in section 5.
10. Project structure (one paragraph + `tree -L 2` output of the
    `src/` and `specs/` directories).
11. Pointer to the technical report PDF in `report/`.

## Tests
- `DockerComposeIT` (optional, only if time allows): use
  Testcontainers' `DockerComposeContainer` to spin compose and hit
  `/actuator/health`.
- Manual checklist (no automation needed):
    - [ ] Fresh clone → `cp .env.example .env` → set `JWT_SECRET` →
      `docker compose up --build` → API responds.
    - [ ] Newman runs the full collection green.
    - [ ] `compose up` fails fast with a clear message if
      `JWT_SECRET` is missing.

## Definition of done
- [ ] `docker compose up --build` succeeds on a clean machine after
  copying `.env.example` to `.env`.
- [ ] Compose fails to start with a clear error if `JWT_SECRET` is
  not provided.
- [ ] Every acceptance criterion in `03-acceptance-criteria.md` —
  including the JWT-related ones — has a corresponding green
  Postman request.
- [ ] README walks a stranger from clone to working API in <5 min.
- [ ] Commit: `feat(M11): docker, postman collection and readme`.