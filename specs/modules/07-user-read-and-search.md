# M07 — User Read & Search

## Goal
Add the read operations: fetch by id and search by name (the latter
required by FR05).

## Scope
- `FindUserByIdUseCase` and `SearchUsersByNameUseCase` interfaces.
- Their `*Service` implementations.
- `UserNotFoundException` in `domain.exception`.
- New endpoints on `UserController`.
- New handler entry in `GlobalExceptionHandler`.

## Use case contracts
```java
public interface FindUserByIdUseCase {
    User findById(UUID id);   // throws UserNotFoundException
}

public interface SearchUsersByNameUseCase {
    List<User> searchByName(String term);   // never throws on no match
}
```

## REST contract
- `GET /api/v1/users/{id}`
    - 200 with `UserResponse`.
    - 404 with ProblemDetail `type=/errors/user-not-found` if missing.

- `GET /api/v1/users?name={term}`
    - `name` is required (`@RequestParam(required = true)`).
    - 200 with `List<UserResponse>`. Empty list when no match
      (NEVER 404).
    - Case-insensitive partial match against the user's name.

## Rules
- Search uses `userRepository.findByNameContainingIgnoreCase(term)`
  defined in M05.
- The list endpoint stays simple in phase 1 — no pagination,
  no sorting. (Out of scope, possible improvement for fase 2.)

## Tests
- Unit tests for both services (Mockito).
- `UserControllerWebMvcTest` extended:
    - 200 on `GET /users/{id}` for existing user.
    - 404 on `GET /users/{id}` for missing user.
    - 200 + empty list on `GET /users?name=zzz`.
    - 200 + populated list on `GET /users?name=mar` returning users
      with names "Maria" and "Marcos".
- Integration test verifying case-insensitive search hits both
  "Maria" and "MARIA".

## Definition of done
- [ ] All tests green.
- [ ] No 404 on empty search.
- [ ] Commit: `feat(M07): user read and search endpoints`.