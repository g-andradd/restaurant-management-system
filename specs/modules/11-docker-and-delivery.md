# M11 — Docker & Delivery Package

## Goal
Wrap the application for delivery: multi-stage Dockerfile,
Docker Compose orchestrating app + PostgreSQL, a Postman collection
covering every acceptance criterion, and a README that gets a fresh
reviewer running the system in under five minutes.

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
    - Depends on `db` (condition: service_healthy).
    - Ports: `8080:8080`.

## Postman collection
One folder per acceptance area, each request including:
- Pre-request scripts when needed (e.g. capture `userId` from a 201
  response into a collection variable).
- Tests asserting status code and key fields.

Folders and requests (every acceptance criterion from
`03-acceptance-criteria.md` covered):

1. **User registration**
    - Register valid user (CUSTOMER) → expect 201, save `customerId`.
    - Register valid user (RESTAURANT_OWNER) → expect 201.
    - Register valid user (ADMIN) → expect 201.
    - Register duplicate email → expect 409.
    - Register missing field → expect 400.
    - Register invalid role → expect 400.

2. **Update user data**
    - Update existing user → expect 200, assert `updatedAt` advanced.
    - Update non-existing user → expect 404.
    - Update with payload containing `password` → expect 200, password
      unchanged (verify by attempting login afterwards).

3. **Change password**
    - Valid change → expect 204.
    - Change for non-existing user → expect 404.
    - Change with weak password → expect 400.

4. **Delete user**
    - Delete existing user → expect 204.
    - Delete non-existing user → expect 404.

5. **Search by name**
    - Search hitting multiple users → expect 200, length > 0.
    - Search with no match → expect 200, length == 0.
    - Case-insensitive search → expect 200, length > 0.

6. **Login validation**
    - Valid credentials → expect 200, `authenticated=true`.
    - Wrong password → expect 401.
    - Unknown login → expect 401, body identical to wrong password.

Environment file ships:
- `baseUrl=http://localhost:8080/api/v1`
- Empty `customerId`, `ownerId`, etc., to be populated by scripts.

## README.md
Sections in order:
1. Project name + one-paragraph description.
2. Architecture overview (link to `specs/technical/01-architecture.md`).
3. Stack & versions.
4. How to run locally (dev, with H2):
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
5. How to run with Docker Compose (hom profile, Postgres):
   docker compose up --build
6. Environment variables table.
7. Swagger URL: `http://localhost:8080/swagger-ui.html`.
8. How to run tests (`./mvnw test`).
9. How to import the Postman collection (path + steps).
10. Project structure (one paragraph + tree).
11. Pointer to the technical report PDF.

## Tests
- `DockerComposeIT` (optional, only if you have time): use
  Testcontainers' `DockerComposeContainer` to spin compose and
  hit `/actuator/health`.
- Manual checklist (no automation needed):
    - [ ] Fresh clone → `docker compose up --build` → API responds.
    - [ ] Postman runner executes the full collection green.

## Definition of done
- [ ] `docker compose up --build` succeeds on a clean machine.
- [ ] Every acceptance criterion in `03-acceptance-criteria.md` has a
  corresponding green Postman request.
- [ ] README walks a stranger from clone to working API in <5 min.
- [ ] Commit: `feat(M11): docker, postman collection and readme`.