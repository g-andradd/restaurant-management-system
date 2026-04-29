# Conventions

## Code language
All code, comments, identifiers and commit messages in **English**.
User-facing messages (validation errors, ProblemDetail title/detail)
in **Portuguese**.

## Naming
- Domain entities: nouns, singular — `User`, `Address`.
- Use case interfaces: `<Action><Entity>UseCase` — `RegisterUserUseCase`.
- Use case implementations: `<Action><Entity>Service` — `RegisterUserService`.
- Use case input objects: `<Action><Entity>Command` — `RegisterUserCommand`.
- Ports out: `<Capability>Port` — `UserRepositoryPort`, `PasswordEncoderPort`.
- Adapters: `<Tech><Capability>Adapter` — `JpaUserRepositoryAdapter`,
  `BCryptPasswordEncoderAdapter`.
- DTOs: `<Action><Entity>Request`, `<Entity>Response`.
- JPA entities: `<Entity>JpaEntity`.

## REST
- Base path: `/api/v1`
- Resource: `/users`
- Verbs:
  - `POST   /api/v1/users`                   → create
  - `GET    /api/v1/users?name=...`          → search by name
  - `GET    /api/v1/users/{id}`              → find by id
  - `PUT    /api/v1/users/{id}`              → update profile data
  - `PATCH  /api/v1/users/{id}/password`     → change password
  - `DELETE /api/v1/users/{id}`              → delete
  - `POST   /api/v1/auth/login`              → validate credentials

## HTTP status codes
- 201 Created — successful creation, with `Location` header.
- 200 OK — successful read/update.
- 204 No Content — successful delete or password change.
- 400 Bad Request — validation failure.
- 401 Unauthorized — auth failure.
- 404 Not Found — resource not found.
- 409 Conflict — business conflict (duplicate email).

## DTOs
- Use Java records.
- Validate with Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`,
  `@Pattern`, `@NotNull`, `@Valid` for cascading).
- Nested DTOs (e.g. `AddressRequest` inside `RegisterUserRequest`) are
  validated with `@Valid` on the parent field.
- Never expose password fields in responses. Response records simply
  omit the field — there is no masking mechanism.

## Persistence
- IDs are UUID, generated in the domain (not by the database).
- All tables snake_case; all columns snake_case.
- Auditing fields: `created_at`, `updated_at` (the spec calls
  `updated_at` "data da última alteração").
- Migrations in `src/main/resources/db/migration/V{n}__{name}.sql`.

## Bean wiring across layers
The `application` layer is framework-free. Classes in
`application.usecase` and `application.port..` MUST NOT carry
`@Service`, `@Component`, `@Repository`, or any Spring stereotype —
this is enforced by ArchUnit Rule 2.

Use case implementations are registered as Spring beans through
explicit `@Bean` methods in `infrastructure.config.UseCaseBeanConfiguration`.
The bean's declared return type is the use case **interface** (from
`application.port.in`), not the implementation. This forces controllers
to depend on ports and keeps the application layer ArchUnit-compliant.

Example:
```java
// in infrastructure.config.UseCaseBeanConfiguration
@Bean
RegisterUserUseCase registerUserUseCase(
        UserRepositoryPort userRepository,
        PasswordEncoderPort passwordEncoder) {
    return new RegisterUserService(userRepository, passwordEncoder);
}
```

Adapters in `infrastructure.adapter..` (e.g. `JpaUserRepositoryAdapter`,
`BCryptPasswordEncoderAdapter`) DO use `@Component` directly — they
live in the infrastructure layer where Spring stereotypes are allowed.
The same is true for controllers (`@RestController`), advices
(`@RestControllerAdvice`), JPA entities (`@Entity`), and config
classes (`@Configuration`).

## Exception handling layout
Two `@RestControllerAdvice` classes coexist, separated by which
layer the handled exceptions live in:

- `shared.exception.GlobalExceptionHandler` — handles framework
  exceptions (`MethodArgumentNotValidException`,
  `ConstraintViolationException`, generic `Exception` catch-all)
  and exceptions defined in `shared.exception` itself
  (e.g. `NotFoundException`).
- `infrastructure.adapter.in.web.DomainExceptionHandler` — handles
  every exception defined in `domain.exception`. Adding a new
  domain exception means adding a new `@ExceptionHandler` here,
  NOT in `GlobalExceptionHandler` (which would import from `domain..`
  and violate ArchUnit Rule 4: shared cannot depend on any project
  layer).

Both advices coexist without ordering concerns as long as the
handled exception types are disjoint. See `04-error-handling.md`
for the full mapping table.

## Testing
- Unit tests for domain and use cases (no Spring context — plain
  JUnit 5 + AssertJ + Mockito).
- Slice tests for web layer (`@WebMvcTest(MyController.class)`).
- Integration tests with Testcontainers for repository and full
  HTTP-to-DB flow. Filename suffix `*IT` (run by Failsafe in the
  `verify` phase, NOT by Surefire).
- ArchUnit test class: `ArchitectureTest` enforcing dependency rules.
- Coverage target: ≥ 80% on `application` and `domain` packages.

### @WebMvcTest with @Component collaborators
A controller often depends on stateless `@Component` beans (mappers,
formatters). `@WebMvcTest` does NOT load these by default. The
correct way to make them available is `@Import(MyMapper.class)`,
NOT `@MockBean`. Mock only the use case interfaces and other
behavior-carrying collaborators. Stateless mappers should run as
real beans so the test exercises the actual mapping logic.

Example:
```java
@WebMvcTest(UserController.class)
@Import(UserWebMapper.class)
class UserControllerWebMvcTest {

    @Autowired MockMvc mockMvc;
    @MockBean RegisterUserUseCase registerUserUseCase; // mock behavior
    // UserWebMapper is the real bean — imported above
}
```

### Integration tests with Testcontainers
ITs override the datasource via `@DynamicPropertySource`, including
`spring.datasource.driver-class-name=org.postgresql.Driver` (the dev
profile sets the H2 driver, which would otherwise survive the URL
override). See `JpaUserRepositoryAdapterIT` and `UserRegistrationIT`
for the canonical pattern. Copy verbatim rather than refactoring
into a base class — keeping each IT self-contained makes failures
easier to diagnose.

## Logging
- SLF4J via Lombok `@Slf4j` (only in infrastructure).
- Never log passwords, tokens, or full request bodies.

## Profiles
- `dev`   → H2 in-memory (PostgreSQL compatibility mode), Flyway
  enabled, Swagger enabled, verbose logs.
- `hom`   → Postgres, Flyway enabled, Swagger enabled.
- `prod`  → Postgres, Flyway enabled, Swagger disabled, info logs.