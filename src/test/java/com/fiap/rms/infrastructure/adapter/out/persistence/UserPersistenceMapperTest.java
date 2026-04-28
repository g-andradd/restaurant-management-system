package com.fiap.rms.infrastructure.adapter.out.persistence;

import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceMapperTest {

    private final UserPersistenceMapper mapper = new UserPersistenceMapper();

    @Test
    void roundTrip_domainToJpaToDomain_preservesAllFields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T12:00:00Z");
        Address address = new Address("Rua das Flores", "42", "São Paulo", "01310-100");

        User original = User.rehydrate(id, "João Silva", "joao@example.com", "joaosilva",
                "bcrypt_hash", Role.CUSTOMER, address, createdAt, updatedAt);

        UserJpaEntity entity = mapper.toJpa(original);
        User rebuilt = mapper.toDomain(entity);

        assertThat(rebuilt.getId()).isEqualTo(id);
        assertThat(rebuilt.getName()).isEqualTo("João Silva");
        assertThat(rebuilt.getEmail()).isEqualTo("joao@example.com");
        assertThat(rebuilt.getLogin()).isEqualTo("joaosilva");
        assertThat(rebuilt.getPasswordHash()).isEqualTo("bcrypt_hash");
        assertThat(rebuilt.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(rebuilt.getAddress()).isEqualTo(address);
        assertThat(rebuilt.getCreatedAt()).isEqualTo(createdAt);
        assertThat(rebuilt.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
