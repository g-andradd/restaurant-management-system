# M01 — Project Skeleton

## Goal
Bootstrap a Spring Boot 3.3.5 / Java 21 / Maven project with the package
structure, profiles and base configuration defined in the technical
specs. The application must boot empty (no endpoints, no entities) and
respond on `/actuator/health`.

## Scope
- `pom.xml` with all dependencies and plugins declared in `02-stack.md`.
- Package structure under `com.fiap.rms` matching
  `01-architecture.md`.
- `application.yml` with shared config and three profile files:
  `application-dev.yml`, `application-hom.yml`, `application-prod.yml`.
- `RmsApplication` main class.
- `.gitignore` for Java/Maven.

Out of scope: ArchUnit, error handling, any domain code (those come
in M02 and M03).

## Inputs / Outputs
No HTTP API yet. Only `/actuator/health` returning 200.

## Rules
- Java 21, Maven 3.9+, Spring Boot 3.3.x.
- `dev` profile: H2 in-memory in PostgreSQL compatibility mode,
  Flyway disabled (no migrations yet — M05 turns it on),
  H2 console enabled at `/h2-console`.
- `hom` and `prod` profiles: PostgreSQL connection placeholders via
  environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
  These profiles will not be functional until M05 ships the first
  Flyway migration.
- `prod`: Swagger and H2 console disabled, log level INFO.
- All dependencies pinned via Spring Boot BOM — no loose versions
  except where the BOM does not manage the artifact (e.g. springdoc,
  archunit, testcontainers override).

## Required dependencies (pom.xml)
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- spring-boot-starter-actuator
- com.h2database:h2 (runtime)
- org.postgresql:postgresql (runtime)
- org.flywaydb:flyway-core
- org.flywaydb:flyway-database-postgresql
  (Flyway 10+ split PostgreSQL support into a separate module;
  flyway-core alone cannot connect to PostgreSQL 16+.)
- org.springframework.security:spring-security-crypto
- org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0
- org.projectlombok:lombok (provided)
- spring-boot-starter-test (test)
- com.tngtech.archunit:archunit-junit5:1.3.0 (test)
- org.testcontainers:postgresql:1.20.4 (test) — explicit override
  of the version managed by the Spring Boot BOM (which ships
  Testcontainers 1.19.x). 1.20.4 has the docker-java client
  required for Docker Desktop 4.28+.
- org.testcontainers:junit-jupiter:1.20.4 (test)

## Required build plugins (pom.xml)
- spring-boot-maven-plugin (managed by parent)
- maven-compiler-plugin with Lombok annotation processor path
  (already in scope; just keep the existing config)
- maven-surefire-plugin (managed by parent — runs `*Test` classes
  in the `test` phase). No extra config needed in M01.
- maven-failsafe-plugin
  Required so `*IT` classes (introduced in M05) run during
  `mvn verify`. The Spring Boot Starter Parent does NOT bind
  Failsafe by default — it must be declared explicitly with goals
  `integration-test` and `verify`.
  Configure with `<argLine>-Dapi.version=1.44</argLine>` to work
  around the Docker Desktop 4.28+ proxy rejecting docker-java's
  default API version. The argLine has no effect on environments
  without Docker Desktop, so it's safe everywhere.

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
- `RmsApplicationTests`: context loads with `dev` profile
  (`@ActiveProfiles("dev")` explicit).

## Definition of done
- [ ] `mvn clean verify` is green.
- [ ] `mvn spring-boot:run` boots and `/actuator/health` returns
  `{"status":"UP"}`.
- [ ] All packages exist with package-info.java.
- [ ] Commit: `feat(M01): project skeleton`.