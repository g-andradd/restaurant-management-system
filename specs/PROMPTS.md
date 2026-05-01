## template universal de prompt
<contexto>
You are implementing module M{NN} of the Tech Challenge.
Read /specs/CLAUDE.md, /specs/product/, /specs/technical/, and
specifically /specs/modules/{NN}-{name}.md before writing code.

If anything in the spec is ambiguous, STOP and ask me — do not guess.
</contexto>

<tarefa>
Implement M{NN} fully according to its spec, including all tests
defined in the "Tests" section.
</tarefa>

<restrições>
- Follow the conventions in /specs/technical/03-conventions.md.
- Respect the architecture rules — ArchUnit must stay green.
- Do not modify code from previous modules unless the current
  spec explicitly requires it (M09b is the exception — it updates
  M06–M08 tests).
- Generate the commit message exactly as specified in the spec's
  "Definition of done".
  </restrições>

<entrega>
At the end:
1. Run `mvn clean verify` and report the result.
2. List every file you created or modified.
3. Confirm each item of the "Definition of done" with ✅ or ❌.
4. Stop. Wait for my review before moving to the next module.
</entrega>

##  PROMPT M01 — Project Skeleton
<contexto>
You are implementing module M01 of the Tech Challenge.

Read these files first, in order:
- /CLAUDE.md
- /specs/product/01-vision.md
- /specs/product/02-requirements.md
- /specs/technical/01-architecture.md
- /specs/technical/02-stack.md
- /specs/technical/03-conventions.md
- /specs/modules/00-roadmap.md
- /specs/modules/01-project-skeleton.md

If anything in the spec is ambiguous, STOP and ask me before writing
any code.
</contexto>

<tarefa>
Implement M01 — Project Skeleton, fully according to its spec.

This includes:
- pom.xml with all dependencies declared in 02-stack.md.
- Package structure under com.fiap.rms with package-info.java
  in each leaf package.
- application.yml + application-dev.yml + application-hom.yml +
  application-prod.yml.
- Main class TechChallengeApplication.
- .gitignore for Java/Maven.
- TechChallengeApplicationTests asserting context loads with the dev
  profile.
  </tarefa>

<restrições>
- Java 21, Maven 3.9+, Spring Boot 3.3.x.
- Pin all dependencies via Spring Boot BOM — no loose versions.
- Do NOT add Spring Security yet (that comes in M09b).
- Do NOT enable Flyway in any profile yet (M01 spec is explicit
  about this).
- Follow the conventions in /specs/technical/03-conventions.md.
  </restrições>

<entrega>
At the end:
1. Run `mvn clean verify` and report the result.
2. Run `mvn spring-boot:run` long enough to confirm
   /actuator/health returns 200 (a smoke test is fine).
3. List every file you created.
4. Confirm each item of the "Definition of done" in the spec
   with ✅ or ❌.
5. Use exactly this commit message: feat(M01): project skeleton
6. Stop. Wait for my review before M02.
</entrega>

## PROMPT M02 — Architecture Guardrails
<contexto>
You are implementing module M02 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/technical/01-architecture.md
- /specs/modules/02-architecture-guardrails.md

M01 is already implemented and committed. Do not modify it.
</contexto>

<tarefa>
Implement M02 — Architecture Guardrails, fully according to its spec.

This includes:
- ArchitectureTest class with one @Test per architectural rule.
- DomainException base class in com.fiap.rms.domain.exception.
- DomainExceptionTest unit test.
  </tarefa>

<restrições>
- Use ArchUnit 1.3.x (already in pom.xml from M01).
- Test rules must be expressive — use ArchUnit's classes()/noClasses()
  DSL with descriptive .because(...) clauses.
- DomainException MUST NOT import Spring, Jakarta, or any framework.
- Do NOT add any business exceptions yet (those come per-module
  starting in M04).
  </restrições>

<entrega>
At the end:
1. Run `mvn clean verify` and confirm all architecture tests pass.
2. List files created/modified.
3. Confirm each "Definition of done" item.
4. Commit message: feat(M02): architecture guardrails
5. Stop and wait for my review.
</entrega>

## PROMPT M03 — Error Handling
<contexto>
You are implementing module M03 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/technical/04-error-handling.md
- /specs/modules/03-error-handling.md

M01 and M02 are already committed. Do not modify them.
</contexto>

<tarefa>
Implement M03 — Error Handling, fully according to its spec.

This includes:
- GlobalExceptionHandler in shared.exception annotated with
  @RestControllerAdvice.
- Handler methods for: MethodArgumentNotValidException,
  ConstraintViolationException, NotFoundException (defined in
  shared.exception in this module), and Exception (catch-all).
- ProblemDetail responses per RFC 7807, with the type URI prefix
  https://api.techchallenge.com.
- ISO-8601 UTC timestamp extension field on every error.
- ErrorProbeController under profile "dev" only, exposing endpoints
  that throw each handled exception type for testing.
- GlobalExceptionHandlerTest using @WebMvcTest(ErrorProbeController.class).
  </tarefa>

<restrições>
- Content-Type of error responses MUST be application/problem+json.
- Stack traces MUST NEVER appear in the response body.
- The catch-all handler logs at ERROR level with the full stack but
  returns a generic Portuguese message: "Erro interno. Tente
  novamente mais tarde."
- Validation responses include an `errors` array with {field, message}.
- Do NOT add domain-specific exceptions like EmailAlreadyExistsException
  yet — those come with the use cases that throw them (M06+).
- Architecture tests must stay green.
  </restrições>

<entrega>
At the end:
1. Run `mvn clean verify` — all tests must pass.
2. List files created/modified.
3. Confirm each "Definition of done" item.
4. Commit message: feat(M03): global error handling with ProblemDetail
5. Stop and wait for my review.
</entrega>

## PROMPT M04 — User Domain
<contexto>
You are implementing module M04 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/technical/01-architecture.md
- /specs/technical/03-conventions.md
- /specs/modules/04-user-domain.md

M01, M02, M03 are committed. Do not modify them.
</contexto>

<tarefa>
Implement M04 — User Domain, fully according to its spec.

This includes:
- User entity with static factory User.create(...) and
  rehydration constructor User.rehydrate(...).
- Address value object (Java record).
- Role enum (RESTAURANT_OWNER, CUSTOMER, ADMIN).
- Domain exceptions: InvalidUserDataException, InvalidAddressException
  (both extending DomainException).
- Unit tests UserTest and AddressTest covering every behavior listed
  in the spec.
  </tarefa>

<restrições>
- ZERO framework imports in domain. No Spring, no Jakarta, no Lombok.
  Pure Java only.
- User has no public no-arg constructor and no setters.
- toString() MUST NOT include passwordHash.
- equals/hashCode based on id only.
- All validations done inside the domain — InvalidUserDataException /
  InvalidAddressException carry only a message, no HTTP status.
- ArchUnit tests must stay green (they will fail loudly if any
  framework leaks into domain).
  </restrições>

<entrega>
At the end:
1. Run `mvn clean verify` — all tests must pass.
2. List files created.
3. Confirm each "Definition of done" item.
4. Commit message: feat(M04): user domain model
5. Stop and wait for my review.
</entrega>

## PROMPT M05 — User Persistence
<contexto>
You are implementing module M05 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/technical/01-architecture.md
- /specs/modules/04-user-domain.md  (the domain you must persist)
- /specs/modules/05-user-persistence.md

M01–M04 are committed.
</contexto>

<tarefa>
Implement M05 — User Persistence, fully according to its spec.

This includes:
- UserRepositoryPort interface in application.port.out.
- UserJpaEntity, SpringDataUserRepository (JpaRepository),
  JpaUserRepositoryAdapter implementing UserRepositoryPort.
- UserPersistenceMapper (domain ↔ JPA entity).
- Flyway migration V1__create_users_table.sql in
  src/main/resources/db/migration/.
- Enable Flyway across all profiles in application*.yml.
- JpaUserRepositoryAdapterIT (Testcontainers PostgreSQL) covering
  every scenario in the spec.
- UserPersistenceMapperTest unit test (round-trip preservation).
  </tarefa>

<restrições>
- The port returns and accepts only domain types — JPA entities
  NEVER cross the adapter boundary.
- application.port.out.UserRepositoryPort MUST NOT import any JPA
  or Spring class (only domain types and JDK).
- SQL must be ANSI-compatible with both H2 (Postgres mode) and
  PostgreSQL — no jsonb, arrays, or Postgres-only features.
- Indexes: unique on email, plus an index on LOWER(name) for
  case-insensitive search.
- Use Testcontainers' PostgreSQLContainer in the integration test —
  do not hit a real local DB.
  </restrições>

<entrega>
At the end:
1. Run `mvn clean verify` — including the Testcontainers IT.
2. List files created.
3. Confirm each "Definition of done" item.
4. Commit message: feat(M05): user persistence adapter
5. Stop and wait for my review.
</entrega>

## PROMPT M06 — User Registration
<contexto>
You are implementing module M06 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/technical/03-conventions.md
- /specs/technical/04-error-handling.md
- /specs/modules/04-user-domain.md
- /specs/modules/05-user-persistence.md
- /specs/modules/06-user-registration.md

M01–M05 are committed.
</contexto>

<tarefa>
Implement M06 — User Registration, fully according to its spec.

This includes:
- RegisterUserUseCase interface and RegisterUserService implementation.
- PasswordEncoderPort interface and BCryptPasswordEncoderAdapter
  (cost 12, using spring-security-crypto).
- EmailAlreadyExistsException in domain.exception.
- UserController with POST /api/v1/users.
- RegisterUserRequest, UserResponse records (DTOs) with Jakarta
  Bean Validation per the spec.
- UserWebMapper (DTO ↔ domain).
- Wire EmailAlreadyExistsException into GlobalExceptionHandler with
  type=/errors/email-conflict, status 409.
- Tests: RegisterUserServiceTest (Mockito), UserControllerWebMvcTest
  (@WebMvcTest), UserRegistrationIT (Testcontainers, full slice).
  </tarefa>

<restrições>
- Password validation per spec: @Size(min=8, max=72) + @Pattern
  requiring at least 1 uppercase and 1 digit.
- Response body MUST NOT include password or passwordHash.
- Persisted passwordHash MUST start with $2a$ (BCrypt).
- 201 response MUST include Location: /api/v1/users/{id} header.
- Use only ports — controller never touches the JPA adapter directly.
  </restrições>

<entrega>
At the end:
1. Run `mvn clean verify`.
2. List files created/modified.
3. Confirm each "Definition of done" item.
4. Commit message: feat(M06): user registration endpoint
5. Stop and wait for my review.
</entrega>

## PROMPT M07 — User Read & Search
<contexto>
You are implementing module M07 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/modules/06-user-registration.md
- /specs/modules/07-user-read-and-search.md

M01–M06 are committed.
</contexto>

<tarefa>
Implement M07 — User Read & Search, fully according to its spec.

This includes:
- FindUserByIdUseCase + FindUserByIdService.
- SearchUsersByNameUseCase + SearchUsersByNameService.
- UserNotFoundException in domain.exception.
- New endpoints on UserController:
    - GET /api/v1/users/{id} → 200 / 404
    - GET /api/v1/users?name={term} → 200 (empty list when no match)
- Wire UserNotFoundException into GlobalExceptionHandler with
  type=/errors/user-not-found, status 404.
- Unit tests for both services and extended UserControllerWebMvcTest.
- Integration test verifying case-insensitive search.
  </tarefa>

<restrições>
- Empty search MUST return 200 with [], NEVER 404.
- name query param is required — validation should produce 400.
- Search uses findByNameContainingIgnoreCase from M05 — do NOT add
  another repository method.
- No pagination, no sorting (out of scope for phase 1).
  </restrições>

<entrega>
1. Run `mvn clean verify`.
2. List files created/modified.
3. Confirm "Definition of done".
4. Commit message: feat(M07): user read and search endpoints
5. Stop and wait for review.
</entrega>

## PROMPT M08 — Update, Password Change & Delete
<contexto>
You are implementing module M08 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/modules/06-user-registration.md
- /specs/modules/07-user-read-and-search.md
- /specs/modules/08-user-update-password-delete.md

M01–M07 are committed.
</contexto>

<tarefa>
Implement M08 — Update, Password Change & Delete, fully per spec.

This includes:
- UpdateUserUseCase + UpdateUserService.
- ChangePasswordUseCase + ChangePasswordService.
- DeleteUserUseCase + DeleteUserService.
- DTOs: UpdateUserRequest, ChangePasswordRequest.
- Endpoints on UserController:
    - PUT    /api/v1/users/{id}            → 200 / 400 / 404 / 409
    - PATCH  /api/v1/users/{id}/password   → 204 / 400 / 404
    - DELETE /api/v1/users/{id}            → 204 / 404
- Unit tests for all three services (Mockito).
- Extended UserControllerWebMvcTest covering every status code.
- Integration test verifying updatedAt advances on update and on
  password change, plus the cycle update → password → delete.
- A test asserting that posting `{"password": "..."}` to PUT
  is ignored (hash unchanged in DB).
  </tarefa>

<restrições>
- UpdateUserRequest record has NO password field at all (Jackson
  ignores extras by default — the test confirms this works).
- update() checks email collision only when the email actually
  changed.
- changePassword() goes through PasswordEncoderPort — same encoder
  as M06.
- updatedAt MUST strictly advance on every change (use Instant
  comparisons, not equality).
  </restrições>

<entrega>
1. Run `mvn clean verify`.
2. List files created/modified.
3. Confirm "Definition of done".
4. Commit message: feat(M08): update, password change and delete
5. Stop and wait for review.
</entrega>

## PROMPT M09 — Authentication with JWT
<contexto>
You are implementing module M09 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/technical/decisions/ADR-002-pluggable-authentication.md
- /specs/technical/decisions/ADR-004-spring-security-jwt.md
- /specs/technical/02-stack.md
- /specs/modules/09-authentication.md

M01–M08 are committed.

Note: M09 issues JWTs; M09b (next module) wires Spring Security as
a filter. This module does NOT touch SecurityConfig — endpoints
remain unprotected after M09. M09b will lock them down.
</contexto>

<tarefa>
Implement M09 — Authentication, fully per spec.

This includes:
- AuthenticationStrategyPort and JwtTokenProviderPort interfaces.
- TokenPayload and AuthenticationResult value objects in
  application.usecase.
- AuthenticateUserUseCase + AuthenticateUserService.
- DatabaseAuthenticationAdapter (credentials check).
- JjwtTokenProviderAdapter (HS256, JJWT 0.12).
- InvalidCredentialsException in domain.exception.
- AuthController with POST /api/v1/auth/login.
- LoginRequest, LoginResponse DTOs.
- Wire InvalidCredentialsException into GlobalExceptionHandler with
  type=/errors/unauthorized, status 401.
- Add jjwt-api/jjwt-impl/jjwt-jackson 0.12.6 to pom.xml.
- All tests listed in the spec.
  </tarefa>

<restrições>
- JWT secret read from JWT_SECRET env / property; minimum 32 bytes
  enforced at startup. The prod profile MUST refuse the default
  placeholder secret.
- Failure response is byte-identical for unknown login and wrong
  password.
- The use case never imports JJWT — only the port. JJWT is used
  only inside JjwtTokenProviderAdapter.
- Tokens MUST never be logged.
- No Spring Security yet — that's M09b.
  </restrições>

<entrega>
1. Run `mvn clean verify`.
2. Manually generate a JWT_SECRET (e.g.,
   `openssl rand -base64 48`), run the app, hit POST /auth/login
   and report the returned token.
3. List files created/modified.
4. Confirm "Definition of done".
5. Commit message: feat(M09): authentication with JWT issuance
6. Stop and wait for review.
</entrega>

## PROMPT M09b — Spring Security Filter
<contexto>
You are implementing module M09b of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/technical/decisions/ADR-004-spring-security-jwt.md
- /specs/modules/09-authentication.md
- /specs/modules/09b-spring-security-filter.md

M01–M09 are committed.

CRITICAL: This is the only module that intentionally modifies code
from previous modules. Specifically, the @WebMvcTest classes in
M06–M08 must be updated to authenticate (e.g., via
SecurityMockMvcRequestPostProcessors.jwt() or @WithMockUser) so
they keep passing now that the endpoints are protected.
</contexto>

<tarefa>
Implement M09b — Spring Security Filter & Endpoint Protection,
fully per spec.

This includes:
- Add spring-boot-starter-security to pom.xml.
- SecurityConfig in infrastructure.config (stateless, CSRF disabled,
  authorization rules per spec).
- JwtAuthenticationFilter in
  infrastructure.adapter.in.web.security.
- AuthenticatedUser principal record.
- RestAuthenticationEntryPoint (401 ProblemDetail).
- RestAccessDeniedHandler (403 ProblemDetail).
- SecurityConfigTest covering the cases listed in the spec.
- EndToEndSecuredFlowIT (Testcontainers).
- UPDATE the @WebMvcTest classes from M06, M07, M08 so they pass
  with security enabled (use jwt() post-processor or import a
  test-only configuration that bypasses the filter cleanly).
  </tarefa>

<restrições>
- The filter MUST NOT throw on bad tokens — it just doesn't
  authenticate, letting the entry point produce a clean 401.
- Public endpoints per spec: POST /api/v1/auth/login,
  POST /api/v1/users, /actuator/health, swagger paths,
  H2 console (dev only).
- All previous module tests must stay green after the update.
- Architecture tests must stay green.
- ProblemDetail responses for 401/403 follow the exact same shape
  as the rest of the application (per 04-error-handling.md).
  </restrições>

<entrega>
1. Run `mvn clean verify` — every test from every module must pass.
2. Demonstrate a curl flow:
   - POST /auth/login → get token
   - GET /users/{id} without token → 401
   - GET /users/{id} with token → 200 (or 404 if user missing)
3. List every file created or modified (including the updated
   M06–M08 test classes).
4. Confirm "Definition of done".
5. Commit message: feat(M09b): spring security jwt filter
6. Stop and wait for review.
</entrega>

## PROMPT M10 — OpenAPI / Swagger
<contexto>
You are implementing module M10 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/modules/10-openapi-swagger.md

M01–M09b are committed.
</contexto>

<tarefa>
Implement M10 — OpenAPI / Swagger, fully per spec.

This includes:
- OpenApiConfig in infrastructure.config with the Bearer security
  scheme (bearerAuth, HTTP/JWT).
- Schema for ProblemDetail and ValidationProblemDetail registered
  as reusable components.
- @Operation, @ApiResponses, @ExampleObject annotations on every
  endpoint and DTO listed in the spec, covering all status codes
  including 401 for protected endpoints.
- @SecurityRequirements({}) on the public endpoints
  (POST /auth/login, POST /users) so Swagger UI marks them as
  not requiring a token.
- Disable Swagger and api-docs in the prod profile.
- OpenApiSmokeTest verifying GET /v3/api-docs returns a document
  containing every endpoint and the ProblemDetail schema.
  </tarefa>

<restrições>
- Examples MUST use realistic Brazilian data (names, addresses,
  CEP format).
- No password/passwordHash field anywhere in the OpenAPI document.
- The "Authorize" button in Swagger UI must accept a Bearer token.
  </restrições>

<entrega>
1. Run `mvn clean verify`.
2. Run the app, open /swagger-ui.html, confirm all 7 endpoints are
   listed, the Authorize button works, and examples render.
3. List files created/modified.
4. Confirm "Definition of done".
5. Commit message: feat(M10): openapi/swagger documentation
6. Stop and wait for review.
</entrega>

## PROMPT M11 — Docker & Delivery
<contexto>
You are implementing module M11 of the Tech Challenge.

Read these files first:
- /CLAUDE.md
- /specs/product/03-acceptance-criteria.md
- /specs/modules/11-docker-and-delivery.md

M01–M10 are committed.
</contexto>

<tarefa>
Implement M11 — Docker & Delivery Package, fully per spec.

This includes:
- Multi-stage Dockerfile (Maven build → JRE runtime).
- .dockerignore.
- docker-compose.yml with services `db` (postgres:16-alpine,
  healthcheck) and `app` (depends_on db, env vars including
  JWT_SECRET with the ${JWT_SECRET:?...} fail-fast syntax).
- .env.example at repo root documenting every env var.
- Postman collection at postman/tech-challenge.postman_collection.json
  with collection-level Bearer auth using {{token}}, plus folders
  0–6 exactly as specified (Auth setup → Registration → Update →
  Password → Delete → Search → Authorization edge cases).
- Postman environment file with baseUrl, token, customerId,
  ownerId, adminId variables.
- README.md with the 11 sections listed in the spec.
  </tarefa>

<restrições>
- Dockerfile MUST be multi-stage; the final image MUST use a JRE
  base, not a JDK.
- docker-compose MUST fail fast with a clear error if JWT_SECRET
  is not provided.
- Every Postman test script must assert status code AND key fields.
- Folder 0 MUST run before any protected request — its login
  test populates {{token}}.
- The README MUST include the openssl command to generate a secret.
  </restrições>

<entrega>
1. Run `docker compose up --build` from a clean state with .env
   set. Confirm the app responds at /actuator/health and Swagger
   loads.
2. Run the full Postman collection (via Newman if available) and
   confirm every test green.
3. List every file created or modified.
4. Confirm "Definition of done".
5. Commit message: feat(M11): docker, postman collection and readme
6. Stop and wait for review.
</entrega>

## PROMPT M12 — Technical Report
<contexto>
You are implementing module M12 of the Tech Challenge — the FINAL
deliverable required by FIAP.

Read these files first:
- /CLAUDE.md
- The entire /specs/ directory (this is a synthesis task).
- /specs/modules/12-technical-report.md

M01–M11 are committed and the application runs end-to-end.
</contexto>

<tarefa>
Produce the Technical Report deliverable, fully per spec.

This includes:
- report/technical-report.md (source).
- report/diagrams/architecture.png (hexagonal layers + dependency
  direction).
- report/diagrams/er-diagram.png (users table).
- report/screenshots/ — Swagger UI (≥2 shots) and Postman runner
  output (≥1 shot, all green).
- report/technical-report.pdf (generated from the markdown).

The report MUST be in portuguese Brazil and contain all 9 sections listed in the spec, in
order: Cover, Architecture, ER, Endpoints, Swagger, Postman,
Database, Docker run guide, Appendix.
</tarefa>

<restrições>
- All examples in the report (request/response payloads) MUST be
  copied from real responses of the running application — do NOT
  invent payloads.
- No password/passwordHash visible in any screenshot or example.
- Use Mermaid or PlantUML for diagrams if no other tool is
  available; export to PNG.
- The PDF must be generated and committed (Pandoc is fine:
  `pandoc technical-report.md -o technical-report.pdf`).
  </restrições>

<entrega>
1. List every file created.
2. Confirm "Definition of done".
3. Commit message: docs(M12): technical report
4. Stop. The project is complete after this commit.
</entrega>

## Quando o teste falha e eu não entendo
The test {NomeDoTeste} is failing with this output:

{COLE A SAÍDA AQUI}

The relevant spec is /specs/modules/{NN}-{name}.md.

Diagnose the failure WITHOUT modifying the spec. Either:
(a) the production code is wrong — fix it; or
(b) the test is wrong — fix it; or
(c) the spec is genuinely ambiguous — stop and tell me which
paragraph needs clarification.

Do not "make the test pass" by relaxing assertions.

## Quando a IA fugiu do escopo
Stop. You modified files outside the scope of M{NN}. The spec
explicitly limits this module to:

{COLE A SEÇÃO "Scope" DA SPEC}

Revert any change outside that scope and report what you reverted.

## Quando eu quero revisar antes de codar
<plano-primeiro>
Before writing any code, produce an implementation plan listing:
1. Files you will create.
2. Files you will modify (with the reason).
3. Order of implementation.
4. Any spec ambiguity you spotted.

STOP after the plan. Wait for my approval before coding.
</plano-primeiro>