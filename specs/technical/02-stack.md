# Stack

## Runtime
- Java 21 (LTS)
- Maven 4.0+

## Framework
- Spring Boot 3.3.x
    - spring-boot-starter-web
    - spring-boot-starter-data-jpa
    - spring-boot-starter-validation
    - spring-boot-starter-actuator

## Database
- H2 (runtime, scope `dev` profile)
- PostgreSQL Driver (runtime, scope `hom`/`prod` profiles)
- Flyway for migrations (versioned schema, same SQL works on H2 in
  PostgreSQL compatibility mode)
 
## Security (M01 scope)
- spring-security-crypto (BCrypt only — no full Spring Security setup yet)

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
- Testcontainers (postgres, integration tests against real DB)

## Build helpers
- Lombok (only in infrastructure layer — never in domain)
- MapStruct optional (manual mappers are fine for this scope)

## Container
- Eclipse Temurin 21 JRE (slim) base image
- Docker Compose orchestrating app + postgres