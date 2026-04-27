# M09 — Authentication

## Goal
Implement login validation (FR06) using a pluggable Strategy Pattern,
as defined in ADR-002. Phase 1 ships the database-backed strategy.
Future phases (JWT, OAuth2) plug in as new adapters with zero changes
to the use case.

## Scope
- `AuthenticationStrategyPort` interface in `application.port.out`.
- `AuthenticationResult` value object (success/failure + optional
  user data, no token in phase 1).
- `AuthenticateUserUseCase` interface in `application.port.in`.
- `AuthenticateUserService` in `application.usecase`.
- `DatabaseAuthenticationAdapter` in
  `infrastructure.adapter.out.security`.
- `InvalidCredentialsException` in `domain.exception`.
- `AuthController` in `infrastructure.adapter.in.web`.
- `LoginRequest`, `LoginResponse` DTOs.
- New handler entry in `GlobalExceptionHandler`.

## AuthenticationStrategyPort contract
```java
public interface AuthenticationStrategyPort {
    AuthenticationResult authenticate(String login, String rawPassword);
}
```
Multiple beans of this type may exist in the future. The
`AuthenticateUserService` depends on the port, not on a concrete
adapter — Spring will inject the single bean available in phase 1.
When phase 2 adds JWT, mark the desired adapter with `@Primary` or
inject a list and pick by config — both options are documented in
the ADR but NOT implemented here.

## AuthenticationResult value object
```java
public record AuthenticationResult(
    boolean authenticated,
    UUID userId,    // null when authenticated == false
    Role role       // null when authenticated == false
) {
    public static AuthenticationResult success(UUID id, Role role) { ... }
    public static AuthenticationResult failure() { ... }
}
```
Lives in `application.usecase` (not in domain — it is an
application-level concept, not a business invariant).

## AuthenticateUserUseCase contract
```java
public interface AuthenticateUserUseCase {
    AuthenticationResult authenticate(String login, String rawPassword);
}
```
The service:
1. Delegates entirely to the injected `AuthenticationStrategyPort`.
2. If the result is failure, throws `InvalidCredentialsException`.
3. If success, returns the result to the controller.

## DatabaseAuthenticationAdapter behavior
1. `userRepository.findByLogin(login)` — if empty, return failure.
2. `passwordEncoder.matches(rawPassword, user.passwordHash)` — if
   false, return failure.
3. Otherwise return success with the user's id and role.

CRITICAL: the adapter MUST return the **same** failure result
whether the user does not exist or the password is wrong. No timing
attack mitigation is required for phase 1, but the response shape
must not leak which check failed.

## REST contract
- `POST /api/v1/auth/login`
- Request body (`LoginRequest`):
```json
  { "login": "maria", "password": "Senha@123" }
```
- Validation: both fields `@NotBlank`.
- Success response (`LoginResponse`, 200):
```json
  {
    "authenticated": true,
    "userId": "f1a2b3c4-...",
    "role": "CUSTOMER"
  }
```
- Failure response (401, ProblemDetail
  `type=/errors/unauthorized`):
```json
  {
    "type": "https://api.techchallenge.com/errors/unauthorized",
    "title": "Credenciais inválidas",
    "status": 401,
    "detail": "Login ou senha incorretos.",
    "instance": "/api/v1/auth/login"
  }
```
- Validation failure (missing field): 400 with the standard
  validation ProblemDetail.

## Rules
- The endpoint NEVER reveals whether the login exists.
- `passwordHash` from the User domain object is read by the adapter
  only — it never crosses into the use case or the controller.
- No JWT, no session, no cookie in phase 1. The response only states
  whether the credentials are valid plus minimal user identity.

## Tests
- `AuthenticateUserServiceTest` (unit, Mockito):
    - Strategy returns success → service returns the same result.
    - Strategy returns failure → service throws
      `InvalidCredentialsException`.
- `DatabaseAuthenticationAdapterTest` (unit):
    - Unknown login → failure.
    - Known login + wrong password → failure.
    - Known login + correct password → success with id and role.
- `AuthControllerWebMvcTest`:
    - 200 with success body on valid credentials.
    - 401 ProblemDetail on wrong password.
    - 401 ProblemDetail on unknown login (same body shape as above).
    - 400 on missing field.
- `AuthenticationIT` (Testcontainers): register a user via M06, then
  authenticate via M09, asserting 200; then attempt with wrong
  password and assert 401.

## Definition of done
- [ ] All tests green.
- [ ] Failure response is byte-identical for unknown login vs
  wrong password.
- [ ] Architecture tests still green (auth adapter only depends on
  ports, not on JPA repository directly — it goes through
  `UserRepositoryPort`).
- [ ] Commit: `feat(M09): pluggable authentication`.