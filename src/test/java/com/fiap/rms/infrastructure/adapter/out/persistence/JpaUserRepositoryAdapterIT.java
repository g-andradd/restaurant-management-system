package com.fiap.rms.infrastructure.adapter.out.persistence;

import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@Transactional
class JpaUserRepositoryAdapterIT {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Override the H2 driver set by the dev profile
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        // Disable H2 console auto-configuration (datasource is now PostgreSQL)
        registry.add("spring.h2.console.enabled", () -> "false");
    }

    @Autowired
    private JpaUserRepositoryAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_andFindById_roundTrip() {
        User saved = adapter.save(newUser("João Silva", "joao@ex.com", "joaosilva"));

        // Flush SQL to DB and clear first-level cache so findById hits the database
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        User user = found.get();
        assertThat(user.getId()).isEqualTo(saved.getId());
        assertThat(user.getName()).isEqualTo("João Silva");
        assertThat(user.getEmail()).isEqualTo("joao@ex.com");
        assertThat(user.getLogin()).isEqualTo("joaosilva");
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.getAddress().street()).isEqualTo("Rua A");
    }

    @Test
    void existsByEmail_returnsTrueAfterSaveAndFalseOtherwise() {
        adapter.save(newUser("Maria Oliveira", "maria@ex.com", "mariaoliveira"));
        entityManager.flush();

        assertThat(adapter.existsByEmail("maria@ex.com")).isTrue();
        assertThat(adapter.existsByEmail("nonexistent@ex.com")).isFalse();
    }

    @Test
    void findByNameContainingIgnoreCase_findsBothMixedCaseNames() {
        // Two distinct users with different casing — both match "john"
        adapter.save(newUser("Johnny Bravo", "johnny@ex.com", "johnnybr"));
        adapter.save(newUser("JOHN DOE", "johndoe@ex.com", "johndoe"));
        entityManager.flush();
        entityManager.clear();

        List<User> results = adapter.findByNameContainingIgnoreCase("john");

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Johnny Bravo", "JOHN DOE");
    }

    @Test
    void deleteById_removesTheUser() {
        User saved = adapter.save(newUser("To Delete", "delete@ex.com", "todelete"));
        entityManager.flush();
        entityManager.clear();

        adapter.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.findById(saved.getId())).isEmpty();
    }

    private static User newUser(String name, String email, String login) {
        return User.create(name, email, login, "hashed_pw", Role.CUSTOMER,
                new Address("Rua A", "1", "SP", "01000-000"));
    }
}
