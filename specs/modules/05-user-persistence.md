# M05 — User Persistence

## Goal
Bind the `User` domain model to PostgreSQL/H2 via JPA, behind a port
so the domain stays unaware of persistence. Provide the first Flyway
migration.

## Scope
- `UserRepositoryPort` interface in `application.port.out`.
- `UserJpaEntity` in `infrastructure.adapter.out.persistence`.
- `SpringDataUserRepository` (extends `JpaRepository`) — internal,
  package-private if possible.
- `JpaUserRepositoryAdapter` implementing `UserRepositoryPort`.
- `UserPersistenceMapper` (domain ↔ JPA entity).
- Flyway migration `V1__create_users_table.sql`.

## UserRepositoryPort contract
```java
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByLogin(String login);
    List<User> findByNameContainingIgnoreCase(String term);
    boolean existsByEmail(String email);
    void deleteById(UUID id);
}
```
The port returns and accepts only domain types. JPA entities never
leak out of the adapter.

## JPA entity (UserJpaEntity)
- Table `users`, all columns snake_case.
- `id UUID PRIMARY KEY`.
- `name VARCHAR(150) NOT NULL`.
- `email VARCHAR(255) NOT NULL UNIQUE`.
- `login VARCHAR(60) NOT NULL UNIQUE`.
- `password_hash VARCHAR(255) NOT NULL`.
- `role VARCHAR(30) NOT NULL` — stored as enum string.
- `street VARCHAR(150) NOT NULL`.
- `number VARCHAR(20) NOT NULL`.
- `city VARCHAR(100) NOT NULL`.
- `zip_code VARCHAR(20) NOT NULL`.
- `created_at TIMESTAMP NOT NULL`.
- `updated_at TIMESTAMP NOT NULL`.
- Indexes:
    - Unique index on `email`.
    - Unique index on `login`.
    - Index on `LOWER(name)` for the case-insensitive search.

## Flyway migration
- Path: `src/main/resources/db/migration/V1__create_users_table.sql`.
- ANSI SQL compatible with both H2 (PostgreSQL mode) and PostgreSQL.
- Enable Flyway in all profiles from now on (M01 had it disabled).

## Mapper rules
- `UserPersistenceMapper.toJpa(User)` → `UserJpaEntity`.
- `UserPersistenceMapper.toDomain(UserJpaEntity)` → `User` via
  `User.rehydrate(...)`.
- Mapper is a `@Component` in the persistence package.

## Tests
- `JpaUserRepositoryAdapterIT` (integration, Testcontainers postgres):
    - `save` then `findById` returns the same domain User.
    - `existsByEmail` returns true after save.
    - `findByNameContainingIgnoreCase("john")` finds "Johnny" and "JOHN".
    - `deleteById` removes the row.
- `UserPersistenceMapperTest` (unit):
    - Round-trip domain → JPA → domain preserves all fields.

## Definition of done
- [ ] Integration tests green against a real PostgreSQL container.
- [ ] No JPA imports in `application` or `domain` packages
  (ArchUnit confirms).
- [ ] Commit: `feat(M05): user persistence adapter`.
