# Module Roadmap

Implementation order. Each module depends only on previous ones.
Detailed specs live in `specs/modules/<NN>-<name>.md`.

## Foundation
- **M01 — Project skeleton**
  Maven setup, package structure, profiles (dev/hom/prod),
  application boots empty.

- **M02 — Architecture guardrails**
  ArchUnit tests enforcing dependency rules, base domain exception,
  shared error types.

- **M03 — Error handling**
  GlobalExceptionHandler with ProblemDetail (RFC 7807),
  validation error extension, error type URIs.

## User Management
- **M04 — User domain**
  Pure Java domain: User entity, Address value object, Role enum,
  domain exceptions. Unit tests, no Spring.

- **M05 — User persistence**
  JpaUserEntity, mapper, JpaUserRepositoryAdapter implementing
  UserRepositoryPort, Flyway V1 migration. Testcontainers
  integration test.

- **M06 — User registration**
  RegisterUserUseCase, POST /api/v1/users, request/response DTOs,
  email uniqueness, BCrypt hashing, full slice test.

- **M07 — User read & search**
  GET /api/v1/users/{id}, GET /api/v1/users?name=...
  case-insensitive partial match, empty list on no match.

- **M08 — User update, password change & delete**
  PUT /api/v1/users/{id}, PATCH /api/v1/users/{id}/password,
  DELETE /api/v1/users/{id}. updatedAt refreshed on every change.

## Authentication
- **M09 — Authentication**
  AuthenticationStrategyPort, DatabaseAuthenticationAdapter,
  AuthenticateUserUseCase, POST /api/v1/auth/login.
  Generic 401 for both invalid password and unknown login
  (no user enumeration leak).

## Delivery
- **M10 — OpenAPI / Swagger**
  springdoc configuration, request/response examples for every
  endpoint, error schema referenced from ProblemDetail.

- **M11 — Docker & delivery package**
  Dockerfile (multi-stage), docker-compose.yml (app + postgres),
  Postman collection covering all acceptance criteria,
  README with run instructions.

## Definition of done per module
Every module is only "closed" when:
1. Code compiles and `mvn test` is green.
2. ArchUnit test is green.
3. Tests required by the module's spec exist and pass.
4. Module commit message follows: `feat(MNN): <short description>`.