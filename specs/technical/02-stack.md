# Stack

## Runtime
- Java 21 (LTS)
- Maven 3.9+

## Framework
- Spring Boot 3.3.x
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-validation
  - spring-boot-starter-actuator

## Database
- H2 (runtime, scope `dev` profile, PostgreSQL compatibility mode)
- PostgreSQL Driver (runtime, scope `hom`/`prod` profiles)
- Flyway for migrations (versioned schema, same SQL works on H2 in
  PostgreSQL compatibility mode and on PostgreSQL)
  - flyway-core
  - flyway-database-postgresql (required for Postgres 16+ support
    since Flyway 10 split DB support into per-database modules)

## Security (M01 scope)
- spring-security-crypto (BCrypt only — no full Spring Security
  setup yet)

## Security (added in M09 — NOT in M01)
- spring-boot-starter-security
- io.jsonwebtoken:jjwt-api:0.12.6
- io.jsonwebtoken:jjwt-impl:0.12.6 (runtime)
- io.jsonwebtoken:jjwt-jackson:0.12.6 (runtime)

## Documentation
- springdoc-openapi-starter-webmvc-ui 2.6.x

## Testing
- spring-boot-starter-test (JUnit 5, Mockito, AssertJ)
- ArchUnit 1.3.x (architecture tests)
- Testcontainers 1.20.x (postgres, integration tests against real DB)
  — explicit version override of the Spring Boot BOM's 1.19.x. 1.20+
  ships an updated docker-java client that works with Docker Desktop
  4.28+. Below 1.20 the `*IT` classes will fail with HTTP 400 against
  Docker Desktop's proxy.

## Build plugins
- spring-boot-maven-plugin (parent-managed)
- maven-compiler-plugin with Lombok annotation processor path
- maven-surefire-plugin (parent-managed) — runs `*Test` classes in
  the `test` phase
- maven-failsafe-plugin — runs `*IT` classes in the `verify` phase.
  Must be declared explicitly; the Spring Boot Starter Parent does
  not bind it by default. Recommended `argLine`:
  `-Dapi.version=1.44`
  to set the docker-java API version explicitly for compatibility
  with Docker Desktop 4.28+.

## Build helpers
- Lombok (only in infrastructure layer — never in domain)
- MapStruct optional (manual mappers are fine for this scope)

## Container
- Eclipse Temurin 21 JRE (slim) base image
- Docker Compose orchestrating app + postgres