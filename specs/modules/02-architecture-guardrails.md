# M02 — Architecture Guardrails

## Goal
Establish the architectural rules as executable tests so any future
violation breaks the build. Add the base domain exception that all
business exceptions will extend.

## Scope
- `ArchitectureTest` class enforcing the rules from `01-architecture.md`.
- `DomainException` base class in `domain.exception`.
- `shared.exception` placeholder package (real handler comes in M03).

Out of scope: any user domain code, any HTTP handler.

## Architecture rules (must be tests)
1. Classes in `domain..` may NOT depend on Spring, Jakarta, JPA,
   Lombok, or any infrastructure package.
2. Classes in `application..` may depend only on `domain..` and JDK.
3. Classes in `infrastructure..` may depend on `application..` and
   `domain..`.
4. Classes in `shared..` may be used by any layer.
5. Controllers (`infrastructure.adapter.in.web..`) may NOT directly
   depend on classes in `infrastructure.adapter.out..` (controllers
   reach the outside world only via use case ports).
6. Classes named `*UseCase` must be interfaces and live under
   `application.port.in`.
7. Classes named `*Port` must be interfaces and live under
   `application.port.out` or `application.port.in`.
8. Classes named `*Adapter` must live under `infrastructure.adapter..`.

## DomainException contract
- `package com.fiap.rms.domain.exception`
- `public abstract class DomainException extends RuntimeException`
- Constructor `(String message)`.
- No HTTP knowledge. No status codes.

## Tests
- `ArchitectureTest`: one `@Test` per rule above, using ArchUnit's
  `classes()` and `noClasses()` DSL.
- `DomainExceptionTest`: subclass it inline and assert message
  propagation.

## Definition of done
- [ ] All architecture tests are green.
- [ ] `DomainException` exists and is unit-tested.
- [ ] Build still green.
- [ ] Commit: `feat(M02): architecture guardrails`.