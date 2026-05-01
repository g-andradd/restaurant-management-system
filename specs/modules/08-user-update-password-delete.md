# M08 — User Update, Password Change & Delete

## Goal
Complete the user CRUD with the three remaining operations. These
are split across distinct endpoints per the challenge requirements
(FR02, FR03, FR04).

## Scope
- `UpdateUserUseCase`, `ChangePasswordUseCase`, `DeleteUserUseCase`
  interfaces and services.
- New DTOs: `UpdateUserRequest`, `ChangePasswordRequest`.
- Three new endpoints on `UserController`.
- All wired through `GlobalExceptionHandler`.

## Use case contracts
```java
public interface UpdateUserUseCase {
    User update(UUID id, UpdateUserCommand command);
}

public interface ChangePasswordUseCase {
    void changePassword(UUID id, String newPlainPassword);
}

public interface DeleteUserUseCase {
    void delete(UUID id);
}
```
- `UpdateUserCommand` carries name, email, login, address. NO password,
  NO role (role changes are out of scope for phase 1).
- All three throw `UserNotFoundException` if id is missing.
- `update` checks email uniqueness if the email actually changed; on
  collision throws `EmailAlreadyExistsException`.
- `update` checks login uniqueness if the login actually changed; on
  collision throws `LoginAlreadyExistsException`.
- `changePassword` hashes via `PasswordEncoderPort` before delegating
  to `User.changePassword(hash)`.

## REST contract
- `PUT /api/v1/users/{id}`
    - Body: `UpdateUserRequest` (name, email, login, address — same
      validation rules as registration, no password field at all).
    - 200 with updated `UserResponse`.
    - 404 if user not found.
    - 409 if email or login collides with another user.
    - 400 on validation errors.

- `PATCH /api/v1/users/{id}/password`
    - Body: `ChangePasswordRequest { newPassword }`.
    - Validation: same password rules as registration.
    - 204 No Content on success.
    - 404 if user not found.
    - 400 on validation errors.

- `DELETE /api/v1/users/{id}`
    - 204 No Content on success.
    - 404 if user not found.

## Rules
- `updatedAt` MUST be refreshed on update and on password change.
  Verify with assertions on Instant comparisons.
- The update endpoint MUST reject any attempt to send `password` in
  the body. Achieved by simply NOT having that field on the request
  record — Jackson will ignore extra fields by default. Add a unit
  test asserting that even if a client posts `password`, it is
  ignored and the persisted hash stays unchanged.

## Tests
- Unit tests for all three services (Mockito), including the
  email-collision and login-collision branches on update.
- `UserControllerWebMvcTest` extended:
    - 200 on valid `PUT`.
    - 404 on `PUT` of missing user.
    - 409 on `PUT` causing email collision.
    - 409 on `PUT` causing login collision.
    - 204 on valid `PATCH password`.
    - 400 on `PATCH password` with weak password.
    - 204 on valid `DELETE`.
    - 404 on `DELETE` of missing user.
- Integration test (Testcontainers): full update + password change +
  delete cycle, asserting `updatedAt` advances on each change.
- Test asserting that posting `{ "password": "..." }` to `PUT` does
  NOT change the password hash in the database.

## Definition of done
- [ ] All tests green.
- [ ] `updatedAt` strictly advances on update and password change.
- [ ] No regression on previous modules.
- [ ] Commit: `feat(M08): update, password change and delete`.
