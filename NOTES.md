# Project Notes

## 2026-04-27 — M01: Project Skeleton
- M01: stack confirmada Boot 3.3.5 + Java 21 + Maven 3.9+.
- M01: groupId `com.fiap.rms`, package raiz `com.fiap.rms`, classe
  principal `RmsApplication`.
- Resolvido: "Maven 4.0+" foi corrigido para "Maven 3.9+" em
  `specs/technical/02-stack.md` durante a auditoria pós-M05.

## 2026-04-27 — M04: User Domain
- M04: email uniqueness is NOT a domain invariant — it will be
  enforced by the persistence schema (UNIQUE constraint, M05) and
  by the create-user use case checking the repository before insert
  (M06). The domain only validates email FORMAT.
- M04: Role enum includes ADMIN beyond the FIAP-required
  RESTAURANT_OWNER and CUSTOMER. ADMIN is intended for administrative
  endpoints to be added in M07/M08, gated by the authorization rules
  of M09b. The FIAP brief explicitly allows additional roles if useful.
- M04: 'data da última alteração' is modeled as Instant (UTC) rather
  than java.util.Date. Instant is the correct modern Java 21 choice —
  immutable, timezone-safe, ISO-8601 native.

## 2026-04-28 — M05: User Persistence
- M05: The comment "not functional until M02 brings migrations" in
  application-hom.yml and application-prod.yml was incorrect — the
  blocking module was M05 (user persistence + first Flyway migration),
  not M02 (architecture guardrails). Both comments have been removed.
- M05: Flyway is now enabled in all three profiles (dev, hom, prod).
  ddl-auto is set to validate in all profiles — Flyway owns the
  schema; Hibernate only validates it.
- M05: TIMESTAMP WITH TIME ZONE is used for created_at and updated_at
  in the users table. Plain TIMESTAMP was rejected because it silently
  discards timezone info and can corrupt round-trips when the DB
  server timezone differs from UTC.

## 2026-04-28 — M05 retroactive: pom.xml fixes that belonged in M01
The M05 implementation surfaced three gaps in the original M01 spec.
The fixes were applied during M05 but conceptually belong in the
project skeleton. Both `01-project-skeleton.md` and `02-stack.md`
were updated retroactively to reflect this.

- maven-failsafe-plugin: was missing from the M01 pom. Without it,
  `*IT` classes never run during `mvn verify` — the build silently
  passes while skipping integration tests. Spring Boot Starter Parent
  binds Surefire (for `*Test`) but NOT Failsafe (for `*IT`). Failsafe
  must be declared explicitly with `integration-test` and `verify`
  goals.

- flyway-database-postgresql: was missing from the M01 dependency
  list. Flyway 10 (which Spring Boot 3.3.5 brings via BOM) split
  per-database support into separate modules. flyway-core alone
  cannot run migrations against PostgreSQL 16. Adding
  flyway-database-postgresql as a runtime dependency fixes it.

- Testcontainers 1.20.4 explicit override: the Spring Boot 3.3.5 BOM
  manages Testcontainers at 1.19.x. That version's bundled docker-java
  client defaults to API version 1.41, which Docker Desktop 4.28+
  rejects with HTTP 400. Two interventions: override the
  Testcontainers version to 1.20.4 (newer docker-java) AND pass
  `-Dapi.version=1.44` to the Failsafe argLine so the running JVM
  forces docker-java to use the supported API version. Both are
  needed; either one alone leaves the IT broken on Docker Desktop.

## 2026-04-28 — Retroactive correction to spec hygiene
Going forward, when a module discovers a gap that belongs in an
earlier module's scope, the convention is:
1. Apply the fix in the current module to keep the build green.
2. Update the earlier spec retroactively so a fresh clone of the
   repo at any future date will reproduce the working state.
3. Log the discovery here in NOTES.md with date + module that
   surfaced it, so the relatório técnico (M12) can tell the story
   accurately.

## 2026-04-28 — M06: User Registration
- M06: First end-to-end vertical slice. POST /api/v1/users wires
  domain → application → persistence → web. 36 tests green
  (29 prior + 7 new for M06).
- M06: BCrypt cost 12 chosen for password hashing. Persisted
  passwordHash starts with `$2a$` (BCrypt default prefix). Plain
  password is hashed in `RegisterUserService` via `PasswordEncoderPort`
  BEFORE `User.create(...)` is called — domain never sees plaintext.
- M06: Password validation regex
  `^(?=.*[A-Z])(?=.*\d).+$` plus `@Size(min=8, max=72)`. Max 72 is
  BCrypt's hard limit (longer inputs are silently truncated by the
  algorithm).

## 2026-04-28 — M06 retroactive: convention discoveries
The M06 implementation surfaced three convention gaps that were not
explicit in the earlier specs. All three are now documented in
`03-conventions.md` and `04-error-handling.md`:

- application layer is strictly framework-free: ArchUnit Rule 2
  rejects `@Service`, `@Component`, etc., on classes in
  `application..`. Use case implementations are POJOs registered as
  Spring beans through explicit `@Bean` methods in
  `infrastructure.config.UseCaseBeanConfiguration`. The bean's
  return type is the port interface, not the implementation, so
  controllers depend on ports.

- exception handler split: `shared.exception.GlobalExceptionHandler`
  cannot import from `domain.exception` (ArchUnit Rule 4 forbids
  `shared..` from depending on any project layer). A second advice,
  `infrastructure.adapter.in.web.DomainExceptionHandler`, was
  introduced to translate domain exceptions to HTTP. New domain
  exceptions are wired in this second advice.

- @WebMvcTest with stateless @Component collaborators: use
  `@Import(MyMapper.class)` to make them available, NOT `@MockBean`.
  Mock only behavior-carrying collaborators (use case interfaces,
  external clients, etc.). Stateless mappers run as real beans so
  the slice test exercises actual mapping logic.

## 2026-04-28 — Decision deferred to M07
The `Address` value object is currently flattened into the `users`
table (street, number, city, zip_code as columns). If a future user
story requires multiple addresses per user, this needs to become a
separate `addresses` table. Out of scope for M07 unless the spec
says otherwise — flag in M07's plan if relevant.

## 2026-04-28 — M07: User Read & Search
- M07: Three use cases now wired in UseCaseBeanConfiguration
  (Register, FindById, SearchByName). The pattern documented in M06
  scales cleanly — adding a new use case is one @Bean per service.
- M07: Missing required @RequestParam triggers Spring's
  MissingServletRequestParameterException, which is a framework
  exception. New handler in shared.exception.GlobalExceptionHandler
  converts it to RFC 7807 with the same errors[] shape used for
  bean-validation 400s. ValidationError was lifted from
  method-private to class-private record so all three handlers
  reuse the same JSON structure.
- M07: Empty search returns 200 with []. This is explicit per spec
  to distinguish "no users matched" from "endpoint not found".
- M07: GET /users/{id} 404 routes through DomainExceptionHandler
  (UserNotFoundException is in domain.exception), confirming the
  M06 split works as expected for new domain exceptions.

## 2026-04-28 — M08: User Update, Password Change & Delete
- M08: CRUD complete. 67 tests green. The hexagonal pattern from
  M06 scaled cleanly through 6 use cases — adding new behavior is
  a 5-step recipe (port + service + DTO + @Bean + endpoint).
- M08: UserController now takes 6 use case ports. Tolerable for
  phase 1 but flagged as a candidate for split (e.g. UserCommand
  and UserQuery controllers) if more endpoints land in phase 2.
- M08: PUT semantics intentionally chosen as full replace of the
  4 mutable fields. Partial updates would be PATCH; out of scope.
- M08: PATCH /password takes only newPassword. Authorization via
  current password / JWT belongs to M09. The endpoint is
  intentionally vulnerable until M09 lands — documented so the
  M12 report can address security posture honestly.
- M08: DELETE is hard delete. If phase 2 introduces Order with
  FK to users, this becomes a problem (FK violation or unwanted
  cascade). Soft delete (deleted_at column) is the canonical
  evolution path.
- M08: Thread.sleep(50) used in IT to ensure Instant.now()
  monotonicity. Windows clock granularity can collide at sub-50ms
  intervals despite Java 21 improvements.

## 2026-04-30 — M09: Authentication with JWT issuance
- M09: AuthController injects JwtProperties directly to read
  expiration-seconds for LoginResponse.expiresIn. This couples the
  web layer to the config layer. Cleaner refactor in phase 2: have
  JwtTokenProviderPort.generateToken return an IssuedToken record
  (value + expiresInSeconds), or carry expiresInSeconds inside
  AuthenticationResult so the controller never touches JwtProperties.
  Deferred — JwtProperties is a simple record and the coupling is
  mechanical, not behavioural.
- M09: AuthenticationStrategyPort returns AuthenticationResult with
  token=null; the use case (AuthenticateUserService) calls the
  JwtTokenProviderPort and builds a new AuthenticationResult with
  the token populated. The adapter never issues tokens — single
  responsibility held cleanly.
- M09: Both "unknown login" and "wrong password" paths produce
  byte-identical 401 ProblemDetail bodies (timestamp excluded).
  No information leakage about which field failed validation.

## 2026-04-30 — M09b: Spring Security JWT Filter
- M09b: Spring Security 6 wired with stateless JWT filter. CSRF
  disabled, no sessions. JwtAuthenticationFilter never throws —
  bad tokens result in unauthenticated chain, entry point fires
  clean 401 ProblemDetail.
- M09b: 4 @WebMvcTest classes updated with @WithMockUser at class
  level (UserController, AuthController, GlobalExceptionHandler).
  Reason: @WebMvcTest auto-loads default SecurityAutoConfiguration
  (NOT our SecurityConfig), which blocks all requests by default.
  @WithMockUser is the idiomatic fix for slice tests.
- M09b: 2 ITs (UserSearchIT, UserUpdateAndDeleteIT) updated with
  loginAndGetToken helper. Each IT remains self-contained — helper
  copied verbatim, no base class. Confirms the IT auth flow exercises
  the real filter chain end-to-end.
- M09b: H2 console works in dev because frameOptions(sameOrigin)
  is applied globally. Acceptable trade-off in prod (same-origin
  iframes only, not arbitrary cross-origin embedding).
- M09b: ErrorProbeController stays in production code per M03 spec
  ("remove after M06 if you want; for now it stays so the handler
  is verifiable in isolation"). Could be removed in M10 cleanup
  pass, but its endpoints are not security-relevant since
  GlobalExceptionHandlerTest uses @WithMockUser.

## 2026-04-30 — M11: Docker, Postman, README + critical bug fix

- M11: Multi-stage Dockerfile, docker-compose with Postgres
  healthcheck and JWT_SECRET fail-fast, Postman collection with
  25 requests / 66 assertions across 7 folders, README in 11
  sections. 100 tests still green; Newman fully green.

- M11 surfaced a LATENT bug introduced in M07 (and silently
  inherited through M08, M09, M09b, M10): both @RestControllerAdvice
  classes (GlobalExceptionHandler and DomainExceptionHandler) had
  no explicit @Order. In-process Spring tests scanned classes in
  one order; the packaged JAR scanned in another. In the JAR, the
  catch-all `@ExceptionHandler(Exception.class)` in
  GlobalExceptionHandler matched FIRST and turned every business
  exception (EmailAlreadyExists, UserNotFound, InvalidCredentials,
  etc.) into 500 instead of the correct 4xx.

  Fix:
  - DomainExceptionHandler → @Order(1)
  - GlobalExceptionHandler → @Order(Ordered.LOWEST_PRECEDENCE)

  Convention added to 03-conventions.md: every @RestControllerAdvice
  must carry an explicit @Order. New advices sit between 1 and
  LOWEST_PRECEDENCE.

  This is the most important kind of finding from end-to-end
  delivery testing: a bug that ALL automated tests missed because
  they exercise a different classpath order than production. The
  Newman/Docker-Compose flow caught it on the first real run.

- M11: docker-compose.yml required quoting around the
  ${JWT_SECRET:?...} interpolation because the message contains a
  colon (": generate with"). Without quotes, YAML parser treats
  the colon as a key/value separator. Fixed in the same commit.

- M11: pgdata volume must be cleared (docker compose down -v)
  between test runs of the Postman collection. Each run inserts
  the seed admin and customer; rerunning without volume reset
  yields 409 on the seed steps and cascades to subsequent failures.
  Documented in README.