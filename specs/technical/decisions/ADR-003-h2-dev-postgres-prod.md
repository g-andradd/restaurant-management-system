# ADR-003 — H2 in dev, PostgreSQL in hom/prod

## Status
Accepted — 2026-04-26

## Context
Local development should be fast (no container required just to run
tests). Hom/prod environments must use the relational DB required
by the challenge.

## Decision
- Profile `dev`: H2 in-memory, PostgreSQL compatibility mode.
- Profile `hom` / `prod`: PostgreSQL via Docker Compose.
- Single Flyway migration set under `db/migration/` written in
  ANSI SQL compatible with both engines.

## Consequences
+ Fast feedback loop in development.
+ Same migrations run in all environments.
  − Must avoid Postgres-specific features in V1 migrations (jsonb,
  arrays). For phase 1's user model this is trivially satisfied.