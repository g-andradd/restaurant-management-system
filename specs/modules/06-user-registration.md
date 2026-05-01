# M06 — User Registration

## Goal
First end-to-end feature: register a user via REST. Wires together
domain, application, persistence and web layers. After this module
the system has a working happy path.

## Scope
- `RegisterUserUseCase` interface in `application.port.in`.
- `RegisterUserService` implementing it in `application.usecase`.
- `PasswordEncoderPort` interface in `application.port.out`.
- `BCryptPasswordEncoderAdapter` in
  `infrastructure.adapter.out.security`.
- `EmailAlreadyExistsException` in `domain.exception`.
- `UserController` in `infrastructure.adapter.in.web`.
- `RegisterUserRequest` and `UserResponse` records (DTOs).
- `UserWebMapper` for DTO ↔ domain.
- Wiring of the new exception in `GlobalExceptionHandler` (extend M03).

## RegisterUserUseCase contract
```java
public interface RegisterUserUseCase {
    User register(RegisterUserCommand command);
}
```
- `RegisterUserCommand` is a domain-side record carrying name, email,
  login, plain password, role and Address. Lives in `application.usecase`.
- The use case:
    1. Checks `userRepository.existsByEmail(email)` — if true, throws
       `EmailAlreadyExistsException`.
    2. Checks `userRepository.existsByLogin(login)` — if true, throws
       `LoginAlreadyExistsException`.
    2. Hashes the password via `PasswordEncoderPort.encode(plain)`.
    3. Creates the User via `User.create(...)`.
    4. Persists via `userRepository.save(...)`.
    5. Returns the persisted User.

## PasswordEncoderPort contract
```java
public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
```
The BCrypt adapter wraps `BCryptPasswordEncoder` (cost 12) from
`spring-security-crypto`.

## REST contract
- `POST /api/v1/users`
- Request body (`RegisterUserRequest`):
```json
  {
    "name": "Maria Silva",
    "email": "maria@example.com",
    "login": "maria",
    "password": "Senha@123",
    "role": "CUSTOMER",
    "address": {
      "street": "Rua A",
      "number": "100",
      "city": "São Paulo",
      "zipCode": "01000-000"
    }
  }
```
- Validation (Jakarta Bean Validation on the DTO):
    - `name`: `@NotBlank`, `@Size(max=150)`.
    - `email`: `@NotBlank`, `@Email`.
    - `login`: `@NotBlank`, `@Size(min=3, max=60)`.
    - `password`: `@NotBlank`, `@Size(min=8, max=72)`,
      `@Pattern` requiring at least 1 uppercase and 1 digit.
    - `role`: `@NotNull`.
    - `address.*`: each `@NotBlank`.
- Success: `201 Created`, `Location: /api/v1/users/{id}`,
  body `UserResponse` (no password).
- Conflict (duplicate email): `409` with ProblemDetail
  `type=/errors/email-conflict`.
- Conflict (duplicate login): `409` with ProblemDetail
  `type=/errors/login-conflict`.
- Validation failure: `400` with ProblemDetail `type=/errors/validation`
  and `errors` array.

## UserResponse fields
`id, name, email, login, role, address, createdAt, updatedAt`.
Never includes any password field.

## Tests
- `RegisterUserServiceTest` (unit, Mockito):
    - Happy path: encodes password, calls `save`, returns User.
    - Throws `EmailAlreadyExistsException` when email exists.
    - Plain password is never persisted (verify the saved User has
      the hashed value, not the plain one).
- `UserControllerWebMvcTest` (`@WebMvcTest`):
    - 201 on valid registration with Location header.
    - 400 on missing required field.
    - 409 on duplicate email.
- `UserRegistrationIT` (Testcontainers, full slice):
    - End-to-end: HTTP POST persists a row in postgres and returns 201.

## Definition of done
- [ ] All tests green.
- [ ] `passwordHash` in DB starts with `$2a$` (BCrypt prefix).
- [ ] Response body never contains `password` or `passwordHash`.
- [ ] Commit: `feat(M06): user registration endpoint`.
