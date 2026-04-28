# Architecture

## Style
Pragmatic Hexagonal + light Clean Architecture, single Maven module
with strict package boundaries enforced by ArchUnit tests.

## Layers and packages

com.fiap.rms
├── domain
│   ├── model          ← Entities, Value Objects (pure Java, no framework)
│   └── exception      ← Domain-level business exceptions
├── application
│   ├── port
│   │   ├── in         ← Use case interfaces (driving ports)
│   │   └── out        ← Repository / external interfaces (driven ports)
│   └── usecase        ← Use case implementations
├── infrastructure
│   ├── adapter
│   │   ├── in
│   │   │   └── web    ← REST Controllers, DTOs, mappers
│   │   └── out
│   │       ├── persistence  ← JPA Entities, Repositories, mappers
│   │       └── security     ← Password encoder, auth strategies
│   └── config         ← Spring @Configuration classes
└── shared
└── exception      ← Cross-cutting exceptions, ProblemDetail handler

## Dependency rules (enforced by ArchUnit)
1. `domain` depends on NOTHING (no Spring, no Jakarta, no Lombok).
2. `application` depends only on `domain`.
3. `infrastructure` depends on `application` and `domain`.
4. `shared` may be used by all layers.
5. Controllers (`adapter.in.web`) NEVER call repositories directly —
   always through use case ports.
6. Use cases NEVER reference JPA entities or DTOs — only domain models.

## Driving ports (input)
Each use case is an interface in `application.port.in` with a single
method, named after the action: `RegisterUserUseCase`, `UpdateUserUseCase`,
`ChangePasswordUseCase`, `DeleteUserUseCase`, `FindUsersByNameUseCase`,
`AuthenticateUserUseCase`.

## Driven ports (output)
Repositories and external services are interfaces in `application.port.out`,
implemented by adapters in `infrastructure.adapter.out`. Examples:
`UserRepositoryPort`, `PasswordEncoderPort`, `AuthenticationStrategyPort`.

## Mappers
Each adapter has its own mapper to translate between domain and its
representation (DTO, JPA entity). Domain stays pure; never leaks out.
- `UserWebMapper` — DTO ↔ Domain
- `UserPersistenceMapper` — JPA Entity ↔ Domain

## Why this style
- Use cases isolated from frameworks → easy to unit test without Spring.
- Swapping authentication strategy = swap an adapter, zero domain change.
- Swapping H2 for Postgres = swap config, zero code change.
- Future fases (orders, reviews) plug in as new use cases without
  touching existing code.