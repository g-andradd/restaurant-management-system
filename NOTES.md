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