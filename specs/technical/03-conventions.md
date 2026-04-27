# Conventions

## Code language
All code, comments, identifiers and commit messages in **English**.
User-facing messages (validation errors) in **Portuguese**.

## Naming
- Domain entities: nouns, singular — `User`, `Address`.
- Use case interfaces: `<Action><Entity>UseCase` — `RegisterUserUseCase`.
- Use case implementations: `<Action><Entity>Service` — `RegisterUserService`.
- Ports out: `<Capability>Port` — `UserRepositoryPort`, `PasswordEncoderPort`.
- Adapters: `<Tech><Capability>Adapter` — `JpaUserRepositoryAdapter`.
- DTOs: `<Action><Entity>Request`, `<Entity>Response`.
- JPA entities: `<Entity>JpaEntity`.

## REST
- Base path: `/api/v1`
- Resource: `/users`
- Verbs:
    - `POST   /api/v1/users`                   → create
    - `GET    /api/v1/users?name=...`          → search by name
    - `GET    /api/v1/users/{id}`              → find by id
    - `PUT    /api/v1/users/{id}`              → update profile data
    - `PATCH  /api/v1/users/{id}/password`     → change password
    - `DELETE /api/v1/users/{id}`              → delete
    - `POST   /api/v1/auth/login`              → validate credentials

## HTTP status codes
- 201 Created — successful creation, with `Location` header.
- 200 OK — successful read/update.
- 204 No Content — successful delete or password change.
- 400 Bad Request — validation failure.
- 401 Unauthorized — auth failure.
- 404 Not Found — resource not found.
- 409 Conflict — business conflict (duplicate email).

## DTOs
- Use Java records.
- Validate with Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`).
- Never expose password fields in responses.

## Persistence
- IDs are UUID, generated in the domain (not by the database).
- All tables snake_case; all columns snake_case.
- Auditing fields: `created_at`, `updated_at` (the spec calls
  `updated_at` "data da última alteração").
- Migrations in `src/main/resources/db/migration/V{n}__{name}.sql`.

## Testing
- Unit tests for domain and use cases (no Spring context).
- Slice tests for web layer (`@WebMvcTest`).
- Integration tests with Testcontainers for repository + full flow.
- ArchUnit test class: `ArchitectureTest` enforcing dependency rules.
- Coverage target: ≥ 80% on `application` and `domain` packages.

## Logging
- SLF4J via Lombok `@Slf4j` (only in infrastructure).
- Never log passwords, tokens, or full request bodies.

## Profiles
- `dev`   → H2 in-memory, Flyway enabled, Swagger enabled, verbose logs.
- `hom`   → Postgres, Flyway enabled, Swagger enabled.
- `prod`  → Postgres, Flyway enabled, Swagger disabled, info logs.