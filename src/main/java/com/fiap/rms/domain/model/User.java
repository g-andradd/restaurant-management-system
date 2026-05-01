package com.fiap.rms.domain.model;

import com.fiap.rms.domain.exception.InvalidUserDataException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int NAME_MAX_LENGTH = 150;
    private static final int LOGIN_MIN_LENGTH = 3;
    private static final int LOGIN_MAX_LENGTH = 60;

    private final UUID id;
    private String name;
    private String email;
    private String login;
    private String passwordHash;
    private final Role role;
    private Address address;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(UUID id, String name, String email, String login,
                 String passwordHash, Role role, Address address,
                 Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User create(String name, String email, String login,
                              String passwordHash, Role role, Address address) {
        validateName(name);
        validateEmail(email);
        validateLogin(login);
        validatePasswordHash(passwordHash);
        requireNonNull(role, "role");
        requireNonNull(address, "address");

        Instant now = Instant.now();
        return new User(UUID.randomUUID(), name, email, login,
                passwordHash, role, address, now, now);
    }

    public static User rehydrate(UUID id, String name, String email, String login,
                                 String passwordHash, Role role, Address address,
                                 Instant createdAt, Instant updatedAt) {
        requireNonNull(id, "id");
        validateName(name);
        validateEmail(email);
        validateLogin(login);
        validatePasswordHash(passwordHash);
        requireNonNull(role, "role");
        requireNonNull(address, "address");
        requireNonNull(createdAt, "createdAt");
        requireNonNull(updatedAt, "updatedAt");

        return new User(id, name, email, login, passwordHash,
                role, address, createdAt, updatedAt);
    }

    public void updateProfile(String name, String email, String login, Address address) {
        validateName(name);
        validateEmail(email);
        validateLogin(login);
        requireNonNull(address, "address");

        this.name = name;
        this.email = email;
        this.login = login;
        this.address = address;
        this.updatedAt = Instant.now();
    }

    public void changePassword(String newPasswordHash) {
        validatePasswordHash(newPasswordHash);
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public boolean matches(String passwordHash) {
        return this.passwordHash.equals(passwordHash);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Address getAddress() { return address; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", login='" + login + '\''
                + ", passwordHash='***'"
                + ", role=" + role
                + ", address=" + address
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + '}';
    }

    // ── Private validation helpers ─────────────────────────────────────────

    private static void requireNonNull(Object value, String field) {
        if (value == null) {
            throw new InvalidUserDataException(field + " must not be null");
        }
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException(field + " must not be blank");
        }
    }

    private static void validateName(String name) {
        requireNonBlank(name, "name");
        if (name.length() > NAME_MAX_LENGTH) {
            throw new InvalidUserDataException(
                    "name must not exceed " + NAME_MAX_LENGTH + " characters");
        }
    }

    private static void validateEmail(String email) {
        requireNonBlank(email, "email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidUserDataException("email is not valid: " + email);
        }
    }

    private static void validateLogin(String login) {
        requireNonBlank(login, "login");
        if (login.length() < LOGIN_MIN_LENGTH || login.length() > LOGIN_MAX_LENGTH) {
            throw new InvalidUserDataException(
                    "login must be between " + LOGIN_MIN_LENGTH
                    + " and " + LOGIN_MAX_LENGTH + " characters");
        }
    }

    private static void validatePasswordHash(String hash) {
        requireNonBlank(hash, "passwordHash");
    }
}
