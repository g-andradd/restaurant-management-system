# M10 — OpenAPI / Swagger

## Goal
Make the API self-documented via Swagger UI, with request/response
examples for both success and error paths on every endpoint.
Required by NFR05 and explicitly graded in the challenge.

## Scope
- `OpenApiConfig` class in `infrastructure.config`.
- Annotations on every controller and DTO to surface examples,
  descriptions and the ProblemDetail schema.
- Swagger UI enabled in `dev` and `hom`, disabled in `prod`.

## OpenApiConfig
- `@Bean OpenAPI` with:
    - `info`: title "Tech Challenge — User Management API",
      version "1.0.0", description (1–2 lines), contact (your name +
      course).
    - `servers`: `http://localhost:8080` (dev),
      placeholder for hom.
    - A reusable `components.schemas.ProblemDetail` matching
      `04-error-handling.md`, plus a reusable
      `components.schemas.ValidationProblemDetail` extending it with
      the `errors` array.

## JWT Bearer scheme (added in M10 because depends on M09b)
The OpenAPI document MUST declare a Bearer security scheme:

```java
.components(new Components()
    .addSecuritySchemes("bearerAuth",
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")))
.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
```

Public endpoints (`POST /auth/login`, `POST /users`) explicitly
override with empty security via `@SecurityRequirements({})` on the
controller method, so Swagger UI shows them as not requiring a token.

The Swagger UI "Authorize" button must accept a Bearer token and
apply it to subsequent calls.

## Per-endpoint requirements
For every operation, document:
- `@Operation(summary, description)` with one-sentence summary.
- `@ApiResponses` covering the success status AND every error status
  the endpoint can return (per `04-error-handling.md`).
- `@ExampleObject` payloads attached to:
    - The request body (one realistic example).
    - The 2xx response body.
    - Each 4xx response body, referencing the ProblemDetail schema.

Endpoints to cover:
- `POST   /api/v1/users`            → 201, 400, 409
- `GET    /api/v1/users/{id}`       → 200, 404
- `GET    /api/v1/users?name=...`   → 200
- `PUT    /api/v1/users/{id}`       → 200, 400, 404, 409
- `PATCH  /api/v1/users/{id}/password` → 204, 400, 404
- `DELETE /api/v1/users/{id}`       → 204, 404
- `POST   /api/v1/auth/login`       → 200, 400, 401

## Configuration
- `springdoc.api-docs.path=/v3/api-docs`
- `springdoc.swagger-ui.path=/swagger-ui.html`
- In `application-prod.yml`:
```yaml
  springdoc:
    api-docs:
      enabled: false
    swagger-ui:
      enabled: false
```

## Rules
- DTOs use `@Schema(description=...)` on every field.
- `password` and `passwordHash` are NEVER part of any response
  schema example.
- Examples must use realistic Brazilian data (names, addresses,
  CEP format).

## Tests
- `OpenApiSmokeTest` (`@SpringBootTest`, profile `dev`):
    - `GET /v3/api-docs` returns 200 and a JSON document where
      `paths` contains every endpoint listed above.
    - The document includes a `components.schemas.ProblemDetail`.

## Definition of done
- [ ] `mvn spring-boot:run` then visit `/swagger-ui.html` shows all
  seven endpoints with examples and error responses.
- [ ] Smoke test green.
- [ ] No password field anywhere in the OpenAPI document.
- [ ] Commit: `feat(M10): OpenAPI/Swagger documentation`.