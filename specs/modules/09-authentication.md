# M09 — Authentication (with JWT issuance)

## Goal
Implement login validation (FR06) using a pluggable Strategy Pattern
(ADR-002) and issue a JWT on success (ADR-004). The token will be
validated by the Spring Security filter introduced in M09b.

## Scope
- `AuthenticationStrategyPort` interface in `application.port.out`.
- `JwtTokenProviderPort` interface in `application.port.out`.
- `AuthenticationResult` value object.
- `AuthenticateUserUseCase` interface and `AuthenticateUserService`.
- `DatabaseAuthenticationAdapter` (credential check).
- `JjwtTokenProviderAdapter` (JWT issue + parse) in
  `infrastructure.adapter.out.security`.
- `InvalidCredentialsException` in `domain.exception`.
- `AuthController` in `infrastructure.adapter.in.web`.
- `LoginRequest`, `LoginResponse` DTOs.
- New handler entry in `GlobalExceptionHandler` (401 mapping).

Out of scope (delivered in M09b): the Spring Security filter chain,
endpoint protection rules, the `JwtAuthenticationFilter` itself.

## AuthenticationStrategyPort contract
```java
public interface AuthenticationStrategyPort {
    AuthenticationResult authenticate(String login, String rawPassword);
}
```
Phase 1 has a single adapter; future strategies plug in here.

## JwtTokenProviderPort contract
```java
public interface JwtTokenProviderPort {
    String generateToken(UUID userId, Role role);
    Optional<TokenPayload> parseToken(String token);
}

public record TokenPayload(UUID userId, Role role, Instant expiresAt) {}
```
The port lives in `application.port.out`. The use case depends on
this port — it does not know about JJWT or any signing library.
`TokenPayload` lives in `application.usecase` (application concern).

## AuthenticationResult value object
```java
public record AuthenticationResult(
    boolean authenticated,
    UUID userId,    // null on failure
    Role role,      // null on failure
    String token    // null on failure (set after successful auth)
) {
    public static AuthenticationResult success(UUID id, Role role, String token) { ... }
    public static AuthenticationResult failure() { ... }
}
```

## AuthenticateUserUseCase contract
```java
public interface AuthenticateUserUseCase {
    AuthenticationResult authenticate(String login, String rawPassword);
}
```
The service:
1. Calls `authenticationStrategy.authenticate(login, rawPassword)`.
2. If failure, throws `InvalidCredentialsException`.
3. If success, calls `jwtTokenProvider.generateToken(userId, role)`,
   wraps the response with the token and returns it.

## DatabaseAuthenticationAdapter behavior
Same as the previous M09 spec:
1. `userRepository.findByLogin(login)` — if empty, return failure.
2. `passwordEncoder.matches(rawPassword, user.passwordHash)` — if
   false, return failure.
3. Otherwise return success WITHOUT a token (token is added by the
   use case after the strategy returns).

CRITICAL: failure response is identical for unknown login and wrong
password (no user enumeration leak).

## JjwtTokenProviderAdapter behavior
- Algorithm: HS256.
- Secret: read from `JWT_SECRET` env var. Required to be at least
  32 bytes; configuration validation fails fast if shorter.
- Expiration: 1 hour (configurable via `JWT_EXPIRATION_SECONDS`,
  default 3600).
- Claims:
    - `sub` = user id (UUID, as string).
    - `role` = role name (String).
    - `iat` and `exp` standard claims.
- `parseToken` returns empty Optional for: invalid signature,
  expired token, malformed token. It does NOT throw.

## REST contract (updated)
- `POST /api/v1/auth/login`
- Request:
```json
  { "login": "maria", "password": "Senha@123" }
```
- Success (200):
```json
  {
    "authenticated": true,
    "userId": "f1a2b3c4-...",
    "role": "CUSTOMER",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 3600
  }
```
- Failure (401, ProblemDetail `type=/errors/unauthorized`): identical
  body for unknown login and wrong password.
- Validation failure (400): standard ProblemDetail.

## Configuration
- `application.yml`:
```yaml
  security:
    jwt:
      secret: ${JWT_SECRET:change-me-in-prod-this-must-be-32-bytes-min}
      expiration-seconds: ${JWT_EXPIRATION_SECONDS:3600}
```
- `prod` profile MUST refuse the default secret (validate at
  startup; throw if `secret` equals the placeholder).

## Tests
- `AuthenticateUserServiceTest` (unit, Mockito):
    - Strategy success → token generator called → result includes token.
    - Strategy failure → throws `InvalidCredentialsException`.
- `DatabaseAuthenticationAdapterTest`: same as before.
- `JjwtTokenProviderAdapterTest`:
    - Generated token is parseable and yields the same `userId`/`role`.
    - Expired token returns empty Optional from `parseToken`.
    - Token signed with a different secret returns empty Optional.
    - Malformed token returns empty Optional.
- `AuthControllerWebMvcTest`:
    - 200 with body containing `token` on valid credentials.
    - 401 ProblemDetail on wrong password.
    - 401 ProblemDetail on unknown login (byte-identical body).
    - 400 on missing field.
- `AuthenticationIT` (Testcontainers): register user → login →
  receive valid JWT → parse it and assert claims.

## Definition of done
- [ ] All tests green.
- [ ] Token never logged anywhere.
- [ ] Failure response is byte-identical for unknown login vs wrong
  password.
- [ ] `prod` profile fails to start with default JWT secret.
- [ ] Architecture tests still green.
- [ ] Commit: `feat(M09): authentication with JWT issuance`.