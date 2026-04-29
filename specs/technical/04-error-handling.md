# Error Handling

## Standard
All error responses follow RFC 7807 (Problem Details for HTTP APIs)
using Spring's built-in `ProblemDetail`.

## Language note
ProblemDetail `title` and `detail` fields are **end-user facing** and
therefore written in **Portuguese**, per the project language
convention. Field names, types and `instance` paths stay in English.

## Handler split
Exception-to-HTTP translation is split across two
`@RestControllerAdvice` classes, separated by the originating layer
of the handled exception. This split exists because ArchUnit Rule 4
forbids `shared..` from depending on any project layer (including
`domain..`), so a single advice in `shared.exception` cannot import
domain exceptions.

### `shared.exception.GlobalExceptionHandler`
Handles framework-level exceptions and exceptions defined in
`shared.exception` itself.

| Exception                           | Status | type (URI suffix)     |
|-------------------------------------|--------|-----------------------|
| MethodArgumentNotValidException     | 400    | /errors/validation    |
| ConstraintViolationException        | 400    | /errors/validation    |
| NotFoundException                   | 404    | /errors/not-found     |
| Exception (catch-all)               | 500    | /errors/internal      |

### `infrastructure.adapter.in.web.DomainExceptionHandler`
Handles every exception declared in `domain.exception`. New domain
exceptions are wired here, NOT in `GlobalExceptionHandler`.

| Exception                           | Status | type (URI suffix)        |
|-------------------------------------|--------|--------------------------|
| EmailAlreadyExistsException         | 409    | /errors/email-conflict   |
| UserNotFoundException               | 404    | /errors/user-not-found   |
| InvalidCredentialsException         | 401    | /errors/unauthorized     |
| InvalidUserDataException            | 400    | /errors/invalid-user     |
| InvalidAddressException             | 400    | /errors/invalid-address  |

(Some exceptions in the table above are defined in later modules;
the corresponding handler entry is added when the module ships.)

## Response shape (example)
```json
{
  "type": "https://api.techchallenge.com/errors/email-conflict",
  "title": "E-mail já cadastrado",
  "status": 409,
  "detail": "Já existe um usuário com o e-mail informado.",
  "instance": "/api/v1/users",
  "timestamp": "2026-04-26T12:34:56Z"
}
```

`instance` is populated by accepting `HttpServletRequest` as a
parameter on each handler method (NOT via `RequestContextHolder`)
and calling `request.getRequestURI()`.

`timestamp` is `Instant.now().toString()` set as a ProblemDetail
extension property. Sub-second precision is acceptable; tests should
assert format/presence, not an exact value.

## Validation errors (extended)
For 400 responses from validation, include an `errors` array as a
ProblemDetail extension. Each entry has `field` and `message`. The
`field` is taken from `FieldError.getField()` for
`MethodArgumentNotValidException` and from
`violation.getPropertyPath().toString()` for
`ConstraintViolationException`.

```json
{
  "type": "https://api.techchallenge.com/errors/validation",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos estão inválidos.",
  "instance": "/api/v1/users",
  "timestamp": "2026-04-26T12:34:56Z",
  "errors": [
    { "field": "email", "message": "deve ser um e-mail válido" }
  ]
}
```

## Catch-all (500) rules
- The `Exception` handler logs at ERROR level with the full stack
  trace passed as the second argument to the logger
  (`log.error("...", ex)`).
- The response `detail` is the fixed Portuguese string
  `"Erro interno. Tente novamente mais tarde."` — `ex.getMessage()`
  is NEVER interpolated into the response body.
- Stack traces NEVER appear in the response body. Tests assert this
  by checking the body does not contain `"\tat "` (tab + "at " is
  the JVM's stack frame format).

## Domain exceptions
Live in `domain.exception` and extend a base `DomainException`. They
carry ONLY a message — no HTTP status, no error codes, no field
names. Translation to HTTP happens exclusively in
`DomainExceptionHandler`.