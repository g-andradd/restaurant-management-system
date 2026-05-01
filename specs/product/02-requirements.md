# Requirements

## Functional Requirements (FR)

### FR01 — User registration
The system allows registering users of type **RESTAURANT_OWNER**,
**CUSTOMER** or **ADMIN**, with the mandatory fields: name, email,
login, password, address and role. The last-update timestamp is set
on creation.

### FR02 — Update user data
A dedicated endpoint allows updating name, email, login and address.
**Password cannot be changed via this endpoint.** Automatically
updates the last-update timestamp.

### FR03 — Change password
A **separate** endpoint allows changing the user's password.
Automatically updates the last-update timestamp.

### FR04 — Delete user
The system allows deleting a user by its identifier.

### FR05 — Search users by name
The system allows searching users whose name contains the given term
(case-insensitive, partial match).

### FR06 — Login validation
A service/endpoint receives login and password and returns whether
the combination is valid. Spring Security is not required — a simple
database lookup is sufficient.

### FR07 — Email uniqueness
Two users cannot share the same email. A duplicate attempt returns
a conflict error.

## Non-Functional Requirements (NFR)

### NFR01 — Stack
Spring Boot, Java 21, Maven, relational database.

### NFR02 — Containerization
Application dockerized via `Dockerfile` and orchestrated with
`docker-compose.yml` (app + PostgreSQL).

### NFR03 — API versioning
All exposed endpoints under the `/api/v1` prefix.

### NFR04 — Error handling
Error responses follow the **ProblemDetail (RFC 7807)** standard.

### NFR05 — Documentation
API documented with **Swagger / OpenAPI**, including request and
response examples for both success and error cases.

### NFR06 — Database
- Dev: in-memory H2.
- Hom / Prod: PostgreSQL in a Docker container.
- Switchable via `application-{profile}.yml`.

### NFR07 — Postman collection
The repository includes a Postman collection (JSON) covering every
scenario listed in the acceptance criteria.

### NFR08 — Code quality
Spring Boot best practices, SOLID principles, testable code, clear
layered organization.