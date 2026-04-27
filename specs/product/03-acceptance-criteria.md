# Acceptance Criteria

Scenarios that **must work** for the challenge to be considered
delivered. The Postman collection must cover all of them.

## User registration
- [ ] Valid registration returns 201 with the created user (no password).
- [ ] Registration with an existing email returns 409 (Conflict).
- [ ] Registration with a missing required field returns 400 with a
  ProblemDetail pointing to the invalid field.
- [ ] Registration with an invalid user role returns 400.

## Update user data (separate from password endpoint)
- [ ] Valid update returns 200 with the updated user.
- [ ] Update refreshes `updatedAt`.
- [ ] Update of a non-existing user returns 404.
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

## Operational
- [ ] `docker compose up` starts app + PostgreSQL and the API responds.
- [ ] Swagger reachable at `/swagger-ui.html`.
- [ ] The repository's Postman collection runs every scenario above.