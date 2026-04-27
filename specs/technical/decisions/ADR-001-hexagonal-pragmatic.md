# ADR-001 — Pragmatic Hexagonal Architecture

## Status
Accepted — 2026-04-26

## Context
Tech Challenge requires modular, testable code following SOLID and
good Spring Boot practices. Solo developer, 8-day deadline. Future
phases will add orders, menu, reviews — meaning new use cases plugged
into the same backend.

## Decision
Adopt Hexagonal Architecture in a pragmatic, single-Maven-module form:
- Strict package separation (domain / application / infrastructure).
- Ports and adapters with explicit interfaces.
- Architectural rules enforced by ArchUnit tests at build time.

## Alternatives considered
- **Multi-module Maven**: pure hexagonal, fully isolated layers.
  Rejected: setup overhead too high for the deadline.
- **Classic layered (controller/service/repository)**: simpler but
  couples business logic to Spring/JPA, harder to extend in fase 2+.

## Consequences
+ Use cases unit-testable without Spring.
+ Swapping adapters (auth, persistence) doesn't touch business logic.
+ ArchUnit catches accidental coupling early.
  − One more layer than typical Spring tutorials show — small learning
  cost paid up front.