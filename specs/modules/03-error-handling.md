# M03 — Error Handling

## Goal
Implement the global error handling strategy from `04-error-handling.md`
so every error response in the application — from this point on —
follows RFC 7807 ProblemDetail.

## Scope
- `GlobalExceptionHandler` in `shared.exception` annotated with
  `@RestControllerAdvice`.
- Handlers for the exception types listed in `04-error-handling.md`.
- A small probe controller `ErrorProbeController` (profile `dev`
  only) that throws each exception type on demand, used by the
  acceptance tests below. Remove after M06 if you want; for now it
  stays so the handler is verifiable in isolation.

Out of scope: domain-specific exceptions like `EmailAlreadyExists`
(those are added in M06 alongside the use case that throws them).
For M03 the handler must work for: validation errors, generic
fallback (Exception), and a placeholder `NotFoundException` defined
inside `shared.exception` to be reused later.

## Behavior
- All responses are `application/problem+json`.
- `type` field uses the URI prefix `https://api.techchallenge.com`.
- `instance` is the request URI.
- `timestamp` is an ISO-8601 UTC string (extension field).
- For validation: include an `errors` array with `{field, message}`
  entries.
- The catch-all (`Exception`) returns 500 and logs the stack trace
  at ERROR level. The response `detail` is generic: "Erro interno.
  Tente novamente mais tarde." (Portuguese, per language convention).
- Never include stack traces in the response body.

## Tests
- `GlobalExceptionHandlerTest` (`@WebMvcTest(ErrorProbeController.class)`):
    - 400 validation: payload missing required field returns
      ProblemDetail with `errors[0].field` populated.
    - 404 not found: probe endpoint throwing `NotFoundException` returns
      correct ProblemDetail.
    - 500 fallback: probe endpoint throwing `RuntimeException` returns
      generic 500 with no stack trace in body.

## Definition of done
- [ ] All handler tests green.
- [ ] Architecture tests still green.
- [ ] Commit: `feat(M03): global error handling with ProblemDetail`.