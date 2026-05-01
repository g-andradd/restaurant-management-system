# Acceptance Criteria

Scenarios that **must work** for the challenge to be considered
delivered. The Postman collection must cover all of them.

## User registration
- [ ] Valid registration returns 201 with the created user (no password).
- [ ] Registration with an existing email returns 409 (Conflict).
- [ ] Registration with an existing login returns 409 (Conflict).
- [ ] Registration with a missing required field returns 400 with a
  ProblemDetail pointing to the invalid field.
- [ ] Registration with an invalid user role returns 400.

## Update user data (separate from password endpoint)
- [ ] Valid update returns 200 with the updated user.
- [ ] Update refreshes `updatedAt`.
- [ ] Update of a non-existing user returns 404.
- [ ] Update with a login already used by another user returns 409.
- [ ] Attempting to include a password in this payload is ignored
  or rejected.

## Change password (dedicated endpoint)
- [ ] Valid change returns 204 (No Content).
- [ ] Change refreshes `updatedAt`.
- [ ] Change on a non-existing user returns 404.
- [ ] Change with an invalid new password returns 400.

## Delete user
- [ ] Valid delete returns 204.
- [ ] Delete of a non-existing user returns 404.

## Search by name
- [ ] Search returns the list of users whose name contains the term.
- [ ] Search with no match returns an empty list (200, not 404).
- [ ] Search is case-insensitive.

## Login validation
- [ ] Valid login + password returns 200 with success indication.
- [ ] Invalid login or password returns 401 (Unauthorized).
- [ ] Non-existing login returns 401 (same error — do not leak
  whether the user exists).

## Authentication & Authorization
- [ ] Successful login returns 200 with a JWT in the `token` field.
- [ ] JWT contains claims `sub` (user id) and `role`.
- [ ] JWT expires after the configured TTL (default 1h).
- [ ] Protected endpoints without `Authorization` header return 401
  ProblemDetail.
- [ ] Protected endpoints with an expired token return 401.
- [ ] Protected endpoints with a malformed token return 401.
- [ ] Public endpoints (`POST /auth/login`, `POST /users`) work
  without a token.
- [ ] Swagger UI's "Authorize" button accepts a Bearer token and
  authenticates subsequent calls.

## Operational
- [ ] `docker compose up` starts app + PostgreSQL and the API responds.
- [ ] Swagger reachable at `/swagger-ui.html`.
- [ ] The repository's Postman collection runs every scenario above.
