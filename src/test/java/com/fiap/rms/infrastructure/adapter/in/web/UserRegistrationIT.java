package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.infrastructure.adapter.in.web.dto.AddressRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class UserRegistrationIT {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.h2.console.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    void postUser_endToEnd_persists201AndBcryptHash() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "Maria Silva",
                "maria@integration.com",
                "mariasilva",
                "Senha@123",
                Role.CUSTOMER,
                new AddressRequest("Rua A", "100", "São Paulo", "01000-000")
        );

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();

        // Response body must not contain the word "password"
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("password");

        // Verify the DB row has the correct email and a BCrypt hash
        entityManager.flush();
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery("SELECT email, password_hash FROM users WHERE email = :email")
                .setParameter("email", "maria@integration.com")
                .getResultList();

        assertThat(rows).hasSize(1);
        String dbEmail       = (String) rows.get(0)[0];
        String dbPasswordHash = (String) rows.get(0)[1];
        assertThat(dbEmail).isEqualTo("maria@integration.com");
        assertThat(dbPasswordHash)
                .startsWith("$2a$")
                .isNotEqualTo("Senha@123");

        // Also verify the Location header points to the created resource
        String location = result.getResponse().getHeader("Location");
        assertThat(location).contains("/api/v1/users/");

        // Parse the response and verify id matches location
        JsonNode json = objectMapper.readTree(responseBody);
        String id = json.get("id").asText();
        assertThat(location).endsWith(id);
    }
}
