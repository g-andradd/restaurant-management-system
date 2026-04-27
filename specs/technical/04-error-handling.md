# Error Handling

## Standard
All error responses follow RFC 7807 (Problem Details for HTTP APIs)
using Spring's built-in `ProblemDetail`.

## Language note
ProblemDetail `title` and `detail` fields are **end-user facing** and
therefore written in **Portuguese**, per the project language
convention. Field names, types and `instance` paths stay in English.

## Global handler
A single `@RestControllerAdvice` named `GlobalExceptionHandler` lives
in `shared.exception`. It maps:

| Exception                           | Status | type (URI suffix)     |
|-------------------------------------|--------|-----------------------|
| MethodArgumentNotValidException     | 400    | /errors/validation    |
| ConstraintViolationException        | 400    | /errors/validation    |
| EmailAlreadyExistsException         | 409    | /errors/email-conflict|
| UserNotFoundException               | 404    | /errors/user-not-found|
| InvalidCredentialsException         | 401    | /errors/unauthorized  |
| Exception (catch-all)               | 500    | /errors/internal      |

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

## Validation errors (extended)
For 400 responses from validation, include an `errors` array as a
ProblemDetail extension:

```json
{
  "type": "https://api.techchallenge.com/errors/validation",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos estão inválidos.",
  "instance": "/api/v1/users",
  "errors": [
    { "field": "email", "message": "deve ser um e-mail válido" }
  ]
}
```

## Domain exceptions
Live in `domain.exception` and extend a base `DomainException`. They
NEVER carry HTTP status — translation to HTTP happens in the handler.