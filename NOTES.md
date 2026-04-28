# Project Notes

## 2026-04-27 — M01: Project Skeleton

- M01: stack confirmada Boot 3.3.5 + Java 21 + Maven 3.9+.
- M01: groupId `com.fiap.rms`, package raiz `com.fiap.rms`, classe principal `RmsApplication`.
- Pendência: corrigir "Maven 4.0+" para "Maven 3.9+" em `specs/technical/02-stack.md`.

## 2026-04-27 — M04: User Domain

- M04: email uniqueness is NOT a domain invariant — it will be enforced by the persistence schema
  (UNIQUE constraint, M05) and by the create-user use case checking the repository before insert
  (M06). The domain only validates email FORMAT.
- M04: Role enum includes ADMIN beyond the FIAP-required RESTAURANT_OWNER and CUSTOMER. ADMIN is
  intended for administrative endpoints to be added in M07/M08, gated by the authorization rules
  of M09b. The FIAP brief explicitly allows additional roles if useful.
- M04: 'data da última alteração' is modeled as Instant (UTC) rather than java.util.Date. Instant
  is the correct modern Java 21 choice — immutable, timezone-safe, ISO-8601 native.

## 2026-04-28 — M05: User Persistence

- M05: The comment "not functional until M02 brings migrations" in application-hom.yml and
  application-prod.yml was incorrect — the blocking module was M05 (user persistence + first
  Flyway migration), not M02 (architecture guardrails). Both comments have been removed.
- M05: Flyway is now enabled in all three profiles (dev, hom, prod). ddl-auto is set to
  validate in all profiles — Flyway owns the schema; Hibernate only validates it.
- M05: TIMESTAMP WITH TIME ZONE is used for created_at and updated_at in the users table.
  Plain TIMESTAMP was rejected because it silently discards timezone info and can corrupt
  round-trips when the DB server timezone differs from UTC.
