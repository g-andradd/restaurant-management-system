package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.infrastructure.adapter.in.web.dto.AddressRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AuthenticationIT {

    private static final String TEST_SECRET =
            "test-secret-must-be-at-least-32-bytes-long-aaaa";

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
        registry.add("security.jwt.secret", () -> TEST_SECRET);
        registry.add("security.jwt.expiration-seconds", () -> "3600");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLogin_endToEnd_returnsValidJwtWithCorrectClaims() throws Exception {
        // 1. Register a user
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "Ana Token",
                "ana@authtest.com",
                "anatoken",
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
        String registeredId = registeredUser.get("id").asText();

        // 2. Login with the same credentials
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"anatoken\",\"password\":\"Senha@123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginResponse = objectMapper.readTree(
                loginResult.getResponse().getContentAsString());

        assertThat(loginResponse.get("authenticated").asBoolean()).isTrue();
        assertThat(loginResponse.get("userId").asText()).isEqualTo(registeredId);
        assertThat(loginResponse.get("role").asText()).isEqualTo("CUSTOMER");
        assertThat(loginResponse.get("expiresIn").asLong()).isEqualTo(3600);

        String jwt = loginResponse.get("token").asText();
        assertThat(jwt).isNotBlank();

        // 3. Verify JWT claims directly with JJWT — proves the token is well-formed and signed
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt);
        Claims claims = jws.getPayload();

        assertThat(claims.getSubject()).isEqualTo(registeredId);
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.getExpiration()).isInTheFuture();
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        // Register first
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "Bob Wrong",
                "bob@authtest.com",
                "bobwrong",
                "Senha@123",
                Role.CUSTOMER,
                new AddressRequest("Rua B", "200", "Rio de Janeiro", "20000-000")
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"bobwrong\",\"password\":\"WrongPass@999\"}"))
                .andExpect(status().isUnauthorized());
    }
}
