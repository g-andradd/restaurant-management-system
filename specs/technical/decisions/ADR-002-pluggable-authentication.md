# ADR-002 — Pluggable Authentication

## Status
Accepted — 2026-04-26

## Context
Phase 1 requires a simple authentication endpoint (login + password
against database). Future phases (and the optional challenge) may
add JWT, OAuth2, or Spring Security. We don't want to rewrite the
auth flow each time.

## Decision
Define an `AuthenticationStrategyPort` in `application.port.out` with
method `authenticate(login, password) → AuthenticationResult`.
Phase 1 ships a single adapter `DatabaseAuthenticationAdapter` that
queries the user repository and verifies the password with BCrypt.

Future strategies (JWT issuer, OAuth2 client, etc.) become new
adapters implementing the same port. The use case
`AuthenticateUserService` doesn't change.

## Consequences
+ Strategy Pattern at the port level — textbook clean architecture.
+ Adding JWT in fase 2 = new adapter + new config bean, nothing else.
  − Slight over-engineering for fase 1 alone, justified by fase 2+ scope.