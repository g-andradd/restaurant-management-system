package com.fiap.rms.infrastructure.config;

import com.fiap.rms.application.port.out.JwtTokenProviderPort;
import com.fiap.rms.domain.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProviderPort jwtTokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    // ── Public endpoints — must NOT be blocked by the filter ───────────────

    @Test
    void postAuthLogin_withBlankBody_returns400_notBlockedByFilter() throws Exception {
        // Missing required fields → 400 from validation, not 401 from the security filter
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUsers_withBlankBody_returns400_notBlockedByFilter() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actuatorHealth_noToken_returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ── Protected endpoint — no token ──────────────────────────────────────

    @Test
    void getUser_noToken_returns401ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(containsString("/errors/unauthorized")))
                .andExpect(jsonPath("$.title").value("Não autenticado"))
                .andExpect(jsonPath("$.status").value(401));
    }

    // ── Protected endpoint — valid token ───────────────────────────────────

    @Test
    void getUser_validToken_passesSecurityAndReturns404() throws Exception {
        // User does not exist in the in-memory DB — security passes, use case returns 404
        String token = jwtTokenProvider.generateToken(UUID.randomUUID(), Role.CUSTOMER);

        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ── Protected endpoint — expired token ─────────────────────────────────

    @Test
    void getUser_expiredToken_returns401() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("role", Role.CUSTOMER.name())
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(containsString("/errors/unauthorized")));
    }

    // ── Protected endpoint — malformed token ───────────────────────────────

    @Test
    void getUser_malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(containsString("/errors/unauthorized")));
    }

    // ── Protected endpoint — non-Bearer scheme ─────────────────────────────

    @Test
    void getUser_nonBearerScheme_returns401() throws Exception {
        String basicCredentials = Base64.getEncoder()
                .encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID())
                        .header("Authorization", "Basic " + basicCredentials))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(containsString("/errors/unauthorized")));
    }
}
