# M01 — Project Skeleton

## Goal
Bootstrap a Spring Boot 3.3.5 / Java 21 / Maven project with the package
structure, profiles and base configuration defined in the technical
specs. The application must boot empty (no endpoints, no entities) and
respond on `/actuator/health`.

## Scope
- `pom.xml` with all dependencies declared in `02-stack.md`.
- Package structure under `com.fiap.rms` matching
  `01-architecture.md`.
- `application.yml` with shared config and three profile files:
  `application-dev.yml`, `application-hom.yml`, `application-prod.yml`.
- `TechChallengeApplication` main class.
- `.gitignore` for Java/Maven.

Out of scope: ArchUnit, error handling, any domain code (those come
in M02 and M03).

## Inputs / Outputs
No HTTP API yet. Only `/actuator/health` returning 200.

## Rules
- Java 21, Maven 4.0+, Spring Boot 3.3.x.
- `dev` profile: H2 in-memory, Flyway disabled (no migrations yet),
  H2 console enabled at `/h2-console`.
- `hom` and `prod` profiles: PostgreSQL connection placeholders via
  environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- `prod`: Swagger and H2 console disabled, log level INFO.
- All dependencies pinned via Spring Boot BOM — no loose versions.

## Required dependencies (pom.xml)
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- spring-boot-starter-actuator
- com.h2database:h2 (runtime)
- org.postgresql:postgresql (runtime)
- org.flywaydb:flyway-core
- org.springframework.security:spring-security-crypto
- org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0
- org.projectlombok:lombok (provided)
- spring-boot-starter-test (test)
- com.tngtech.archunit:archunit-junit5:1.3.0 (test)
- org.testcontainers:postgresql (test)
- org.testcontainers:junit-jupiter (test)

## Package structure to create (empty packages with package-info.java)
com.fiap.rms
├── domain
│   ├── model
│   └── exception
├── application
│   ├── port
│   │   ├── in
│   │   └── out
│   └── usecase
├── infrastructure
│   ├── adapter
│   │   ├── in
│   │   │   └── web
│   │   └── out
│   │       ├── persistence
│   │       └── security
│   └── config
└── shared
└── exception

## Tests
- `TechChallengeApplicationTests`: context loads with `dev` profile.

## Definition of done
- [ ] `mvn clean verify` is green.
- [ ] `mvn spring-boot:run` boots and `/actuator/health` returns
  `{"status":"UP"}`.
- [ ] All packages exist with package-info.java.
- [ ] Commit: `feat(M01): project skeleton`.