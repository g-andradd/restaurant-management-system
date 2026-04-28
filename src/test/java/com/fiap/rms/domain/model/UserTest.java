package com.fiap.rms.domain.model;

import com.fiap.rms.domain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final Address VALID_ADDRESS =
            new Address("Rua das Flores", "100", "São Paulo", "01310-100");

    private static User validUser() {
        return User.create("João Silva", "joao@example.com", "joaosilva",
                "hashed_pw", Role.CUSTOMER, VALID_ADDRESS);
    }

    @Test
    void create_withValidData_setsAllFieldsAndTimestamps() {
        Instant before = Instant.now();
        User user = validUser();
        Instant after = Instant.now();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("João Silva");
        assertThat(user.getEmail()).isEqualTo("joao@example.com");
        assertThat(user.getLogin()).isEqualTo("joaosilva");
        assertThat(user.getPasswordHash()).isEqualTo("hashed_pw");
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.getAddress()).isEqualTo(VALID_ADDRESS);
        assertThat(user.getCreatedAt()).isBetween(before, after);
        assertThat(user.getUpdatedAt()).isBetween(before, after);
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void create_withBlankName_throwsInvalidUserDataException() {
        assertThatThrownBy(() ->
                User.create("   ", "joao@example.com", "joaosilva",
                        "hashed_pw", Role.CUSTOMER, VALID_ADDRESS))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @Test
    void create_withInvalidEmail_throwsInvalidUserDataException() {
        assertThatThrownBy(() ->
                User.create("João Silva", "not-an-email", "joaosilva",
                        "hashed_pw", Role.CUSTOMER, VALID_ADDRESS))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @Test
    void updateProfile_refreshesUpdatedAtAndChangesFourFields() throws InterruptedException {
        User user = validUser();
        Instant originalUpdatedAt = user.getUpdatedAt();
        Thread.sleep(2);

        Address newAddress = new Address("Av. Paulista", "1000", "São Paulo", "01310-200");
        user.updateProfile("Maria Souza", "maria@example.com", "mariasouza", newAddress);

        assertThat(user.getName()).isEqualTo("Maria Souza");
        assertThat(user.getEmail()).isEqualTo("maria@example.com");
        assertThat(user.getLogin()).isEqualTo("mariasouza");
        assertThat(user.getAddress()).isEqualTo(newAddress);
        assertThat(user.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void updateProfile_doesNotChangePasswordHash() {
        User user = validUser();
        String originalHash = user.getPasswordHash();

        user.updateProfile("Maria Souza", "maria@example.com", "mariasouza", VALID_ADDRESS);

        assertThat(user.getPasswordHash()).isEqualTo(originalHash);
    }

    @Test
    void changePassword_refreshesUpdatedAtAndReplacesHash() throws InterruptedException {
        User user = validUser();
        Instant originalUpdatedAt = user.getUpdatedAt();
        Thread.sleep(2);

        user.changePassword("new_hashed_pw");

        assertThat(user.getPasswordHash()).isEqualTo("new_hashed_pw");
        assertThat(user.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void toString_doesNotContainPasswordHash() {
        User user = validUser();

        assertThat(user.toString()).doesNotContain("hashed_pw");
    }

    @Test
    void rehydrate_withNullId_throwsInvalidUserDataException() {
        Instant now = Instant.now();

        assertThatThrownBy(() ->
                User.rehydrate(null, "João Silva", "joao@example.com", "joaosilva",
                        "hashed_pw", Role.CUSTOMER, VALID_ADDRESS, now, now))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @Test
    void equals_andHashCode_basedOnIdOnly() {
        UUID sharedId = UUID.randomUUID();
        Instant now = Instant.now();

        User user1 = User.rehydrate(sharedId, "User One", "one@example.com",
                "userone", "hash1", Role.CUSTOMER, VALID_ADDRESS, now, now);
        User user2 = User.rehydrate(sharedId, "User Two", "two@example.com",
                "usertwo", "hash2", Role.ADMIN, VALID_ADDRESS, now, now);
        User user3 = User.rehydrate(UUID.randomUUID(), "User One", "one@example.com",
                "userone", "hash1", Role.CUSTOMER, VALID_ADDRESS, now, now);

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1).isNotEqualTo(user3);
    }
}
