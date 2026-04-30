package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.infrastructure.adapter.in.web.dto.AddressRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class EndToEndSecuredFlowIT {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.h2.console.enabled", () -> "false");
        registry.add("security.jwt.secret",
                () -> "test-secret-must-be-at-least-32-bytes-long-aaaa");
        registry.add("security.jwt.expiration-seconds", () -> "3600");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlow_registerLoginGetUser_securityEnforced() throws Exception {
        // ── 1. POST /api/v1/users — permitAll, no token needed ───────────────
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "Ana Secured",
                "ana@secured.com",
                "anasecured",
                "Senha@123",
                Role.CUSTOMER,
                new AddressRequest("Rua A", "100", "São Paulo", "01000-000")
        );

        MvcResult registerResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode registeredUser = objectMapper.readTree(
                registerResult.getResponse().getContentAsString());
        String userId = registeredUser.get("id").asText();

        // ── 2. GET /api/v1/users/{id} WITHOUT token — must return 401 ────────
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(containsString("/errors/unauthorized")))
                .andExpect(jsonPath("$.status").value(401));

        // ── 3. POST /api/v1/auth/login — capture token ───────────────────────
        String authHeader = loginAndGetToken("anasecured", "Senha@123");

        // ── 4. GET /api/v1/users/{id} WITH Bearer token — must return 200 ────
        mockMvc.perform(get("/api/v1/users/{id}", userId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("ana@secured.com"));
    }

    private String loginAndGetToken(String login, String password) throws Exception {
        String body = String.format(
                "{\"login\":\"%s\",\"password\":\"%s\"}", login, password);
        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();
        return "Bearer " + token;
    }
}
