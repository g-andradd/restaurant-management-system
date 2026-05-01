# M09b — Spring Security Filter & Endpoint Protection

## Goal
Wire Spring Security into the application as a stateless JWT filter
that protects user-management endpoints, while keeping the
authentication and registration endpoints public.

## Scope
- `SecurityConfig` in `infrastructure.config`.
- `JwtAuthenticationFilter` in
  `infrastructure.adapter.in.web.security`.
- `AuthenticatedUser` principal value object.
- `RestAuthenticationEntryPoint` returning ProblemDetail for 401.
- `RestAccessDeniedHandler` returning ProblemDetail for 403.

Out of scope: role-based authorization rules (kept coarse in
phase 1 — any authenticated user passes).

## SecurityConfig
- `@EnableWebSecurity`.
- CSRF disabled (stateless API).
- Session policy: `STATELESS`.
- CORS: permissive in `dev`, configurable via property in `hom`/`prod`.
- Authorization rules:
    - `POST   /api/v1/auth/login`     → permitAll
    - `POST   /api/v1/users`          → permitAll  (registration)
    - `GET    /actuator/health`       → permitAll
    - `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` → permitAll
    - `/h2-console/**`                → permitAll (dev profile only)
    - everything else                 → authenticated
- `JwtAuthenticationFilter` registered before
  `UsernamePasswordAuthenticationFilter`.
- `RestAuthenticationEntryPoint` for 401, `RestAccessDeniedHandler`
  for 403 — both returning ProblemDetail per `04-error-handling.md`.

## JwtAuthenticationFilter behavior
1. Read header `Authorization`.
2. If missing or not starting with `Bearer `, continue the chain
   without setting authentication.
3. Otherwise, strip the prefix and call
   `jwtTokenProvider.parseToken(raw)`.
4. If empty Optional → continue without auth (entry point will
   eventually return 401 if the endpoint requires it).
5. If present → build a `UsernamePasswordAuthenticationToken` with
   `AuthenticatedUser(userId, role)` as principal and authorities
   `["ROLE_" + role]`. Set into `SecurityContextHolder`.

The filter MUST NOT throw on bad tokens — it just doesn't
authenticate, letting the entry point produce a clean 401.

## AuthenticatedUser
```java
public record AuthenticatedUser(UUID userId, Role role) {}
```
Stored as the principal so controllers can inject it via
`@AuthenticationPrincipal` if/when needed in later phases.

## ProblemDetail responses
- 401 (unauthenticated request to a protected endpoint):
  `type=/errors/unauthorized`, `title="Não autenticado"`,
  `detail="Token ausente, inválido ou expirado."`.
- 403 (authenticated but not allowed — reserved for future):
  `type=/errors/forbidden`, `title="Acesso negado"`,
  `detail="Sem permissão para acessar este recurso."`.

## Tests
- `SecurityConfigTest` (`@SpringBootTest`, light context):
    - Public endpoints reachable without token.
    - `GET /api/v1/users/{id}` without token returns 401 ProblemDetail.
    - `GET /api/v1/users/{id}` with valid token returns 200 (or 404
      for missing user — point being it gets past security).
    - `GET /api/v1/users/{id}` with expired token returns 401.
    - `GET /api/v1/users/{id}` with malformed token returns 401.
- `EndToEndSecuredFlowIT` (Testcontainers):
    - Register user → login → use returned token to GET the user →
      success. Without the token → 401.

## Definition of done
- [ ] All tests green.
- [ ] All previous module tests still green (this module must NOT
  break M06–M08 — slice tests for those endpoints need to be
  updated to send a token or use `@WithMockUser` /
  `SecurityMockMvcRequestPostProcessors.jwt()`).
- [ ] No 500s from Spring Security exceptions — all errors are
  clean ProblemDetails.
- [ ] Commit: `feat(M09b): spring security jwt filter`.