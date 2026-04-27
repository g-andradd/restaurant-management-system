# M04 — User Domain

## Goal
Model the User aggregate as pure Java — zero framework dependencies.
This is the heart of the system; everything else adapts to it.

## Scope
- `User` entity (`domain.model.User`).
- `Address` value object (`domain.model.Address`).
- `Role` enum (`domain.model.Role`).
- Domain exceptions for invariant violations.

Out of scope: persistence, REST, validation annotations from Jakarta
(those belong in adapters, not in the domain).

## Role enum
Values: `RESTAURANT_OWNER`, `CUSTOMER`, `ADMIN`.

## Address value object
- Fields: `street` (String), `number` (String), `city` (String),
  `zipCode` (String).
- Java record. Immutable.
- Compact constructor validates: all fields non-null and non-blank.
  Violation throws `InvalidAddressException`.

## User entity
- Fields:
    - `id` (UUID) — generated in domain via `UUID.randomUUID()` if null.
    - `name` (String) — required, non-blank, max 150 chars.
    - `email` (String) — required, must match a basic email regex.
    - `login` (String) — required, non-blank, 3–60 chars.
    - `passwordHash` (String) — required, non-blank. The domain stores
      the hash, never the plain password. Hashing is the adapter's job.
    - `role` (Role) — required.
    - `address` (Address) — required.
    - `createdAt` (Instant) — set on creation.
    - `updatedAt` (Instant) — set on creation, refreshed on every change.

- NOT a record (it is mutable through controlled methods).
- Public no-arg constructor is forbidden. Use a static factory
  `User.create(...)` for new users and a constructor `User.rehydrate(...)`
  used by persistence to rebuild from the database.

## Behaviors (methods on User)
- `updateProfile(String name, String email, String login, Address address)`
  — updates the four fields and refreshes `updatedAt`.
  Validates the same rules as creation. Does NOT touch password.
- `changePassword(String newPasswordHash)`
  — replaces `passwordHash`, refreshes `updatedAt`.
  The hash is provided by the adapter; the domain only stores it.
- `matches(String passwordHash)` — equality check used by auth.

## Domain exceptions
All extend `DomainException`. All in `domain.exception`:
- `InvalidUserDataException` — generic invariant violation.
- `InvalidAddressException` — address invariant violation.

## Rules
- `User` has no setters. State changes only through behavior methods.
- `equals` / `hashCode` based on `id` only.
- `toString` MUST NOT include `passwordHash`.

## Tests (unit, no Spring)
- `UserTest`:
    - `create` with valid data sets all fields and timestamps.
    - `create` with blank name throws `InvalidUserDataException`.
    - `create` with invalid email throws `InvalidUserDataException`.
    - `updateProfile` refreshes `updatedAt` and changes the four fields.
    - `updateProfile` does not change `passwordHash`.
    - `changePassword` refreshes `updatedAt` and replaces hash.
    - `toString` does not contain the password hash.
- `AddressTest`:
    - Valid creation succeeds.
    - Blank street throws `InvalidAddressException`.

## Definition of done
- [ ] All domain tests green, no Spring context loaded.
- [ ] Architecture tests green (no framework imports leaked into domain).
- [ ] Commit: `feat(M04): user domain model`.