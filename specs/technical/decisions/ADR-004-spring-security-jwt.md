# ADR-004 — Spring Security with JWT

## Status
Accepted — 2026-04-26

## Context
ADR-002 defined a pluggable authentication strategy expecting that
phase 2+ would add JWT. The author has decided to bring this forward
into phase 1 to capture the optional challenge points and to deliver
a more complete portfolio piece.

The challenge PDF lists "Spring Security with JWT" as an optional
extra, so this is a stretch goal, not a base requirement. Base login
validation still works exactly the same; we only add a token on top.

## Decision
- Adopt Spring Security with stateless JWT authentication.
- The login endpoint (`POST /api/v1/auth/login`) returns a JWT in the
  success response.
- All `/api/v1/users/**` endpoints require a valid Bearer JWT.
- The authentication endpoint and the registration endpoint
  (`POST /api/v1/users`) stay public — a customer or owner must be
  able to sign up before logging in.
- Authorization in phase 1 stays coarse: any authenticated user can
  hit any user-management endpoint. Role-based authorization is
  reserved for later phases (and the ADMIN role is already in place
  to support that).
- Token signing uses HMAC-SHA256 with a secret loaded from
  environment variable `JWT_SECRET`. Expiration: 1 hour. No refresh
  token in phase 1 (kept simple, can be added later).

## Why this fits the existing architecture
- The Strategy port `AuthenticationStrategyPort` is unchanged.
- The existing `DatabaseAuthenticationAdapter` is unchanged — it
  still validates credentials.
- A new `JwtTokenProviderPort` (out) is introduced, with a single
  adapter `JjwtTokenProviderAdapter`. Token issuance is a side
  capability of the authentication use case, not part of the
  credential check.
- Spring Security itself is configured in `infrastructure.config`
  and adds a single `JwtAuthenticationFilter`. The filter consults
  the same port to validate tokens — no use case knows Spring
  Security exists.

## Alternatives considered
- **Custom auth filter without Spring Security**: simpler code, but
  loses the standard `SecurityContext`, CSRF/CORS handling, and
  recruiter recognition. Rejected.
- **Sessions instead of JWT**: simpler, but stateful and not what
  the challenge mentions. Rejected.
- **Refresh tokens**: nice-to-have, out of scope for phase 1.

## Consequences
+ Production-grade authentication; great portfolio signal.
+ Strategy + Token-Provider ports keep domain and use cases clean.
+ Adding role-based authorization later is a Spring config change.
  − Slight build-time and runtime overhead from Spring Security.
  − One more dependency to learn for someone reading the code first
  time — mitigated by the focused configuration class.