package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.infrastructure.adapter.in.web.dto.AddressRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.ChangePasswordRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class UserUpdateAndDeleteIT {

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

    @Test
    void updatePasswordDelete_fullCycle_updatedAtAdvancesAndDeleteReturns404() throws Exception {
        // ── Create ──────────────────────────────────────────────────────────
        RegisterUserRequest createReq = new RegisterUserRequest(
                "Maria Silva", "maria@cycle.com", "mariacycle",
                "Senha@123", Role.CUSTOMER,
                new AddressRequest("Rua A", "100", "São Paulo", "01000-000"));

        String createBody = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode createJson = objectMapper.readTree(createBody);
        UUID id = UUID.fromString(createJson.get("id").asText());
        Instant updatedAtAfterCreate = Instant.parse(createJson.get("updatedAt").asText());

        // ── Update profile ───────────────────────────────────────────────────
        Thread.sleep(50);

        UpdateUserRequest updateReq = new UpdateUserRequest(
                "Maria Souza", "maria@cycle.com", "mariacycle",
                new AddressRequest("Rua B", "200", "Rio de Janeiro", "20000-000"));

        String updateBody = mockMvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode updateJson = objectMapper.readTree(updateBody);
        assertThat(updateJson.get("name").asText()).isEqualTo("Maria Souza");
        Instant updatedAtAfterUpdate = Instant.parse(updateJson.get("updatedAt").asText());
        assertThat(updatedAtAfterUpdate).isAfter(updatedAtAfterCreate);

        // ── Change password ──────────────────────────────────────────────────
        Thread.sleep(50);

        // Read current hash before password change
        String beforePatchBody = mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Instant updatedAtBeforePatch = Instant.parse(
                objectMapper.readTree(beforePatchBody).get("updatedAt").asText());

        ChangePasswordRequest patchReq = new ChangePasswordRequest("NovaSenha@456");

        mockMvc.perform(patch("/api/v1/users/{id}/password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchReq)))
                .andExpect(status().isNoContent());

        String afterPatchBody = mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Instant updatedAtAfterPatch = Instant.parse(
                objectMapper.readTree(afterPatchBody).get("updatedAt").asText());
        assertThat(updatedAtAfterPatch).isAfter(updatedAtBeforePatch);

        // ── Delete ───────────────────────────────────────────────────────────
        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
