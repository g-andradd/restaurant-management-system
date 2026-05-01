package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class OpenApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode apiDocs;

    @BeforeEach
    void fetchDocs() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        apiDocs = objectMapper.readTree(json);
    }

    @Test
    void allSevenEndpointsArePresent() {
        JsonNode paths = apiDocs.get("paths");
        assertThat(paths).isNotNull();

        // Users endpoints
        assertThat(paths.has("/api/v1/users")).isTrue();
        assertThat(paths.has("/api/v1/users/{id}")).isTrue();
        assertThat(paths.has("/api/v1/users/{id}/password")).isTrue();

        // Auth endpoint
        assertThat(paths.has("/api/v1/auth/login")).isTrue();

        // Verify all 7 operations exist — use get() not at() because the keys contain '/'
        // which is the JSON Pointer delimiter; get() treats the argument as a literal field name.
        assertThat(paths.get("/api/v1/users").has("post")).isTrue();
        assertThat(paths.get("/api/v1/users").has("get")).isTrue();
        assertThat(paths.get("/api/v1/users/{id}").has("get")).isTrue();
        assertThat(paths.get("/api/v1/users/{id}").has("put")).isTrue();
        assertThat(paths.get("/api/v1/users/{id}/password").has("patch")).isTrue();
        assertThat(paths.get("/api/v1/users/{id}").has("delete")).isTrue();
        assertThat(paths.get("/api/v1/auth/login").has("post")).isTrue();
    }

    @Test
    void problemDetailSchemaExists() {
        JsonNode schemas = apiDocs.at("/components/schemas");
        assertThat(schemas.isMissingNode()).isFalse();
        assertThat(schemas.has("ProblemDetail")).isTrue();
        assertThat(schemas.has("ValidationProblemDetail")).isTrue();
    }

    @Test
    void bearerSecuritySchemeIsDeclared() {
        JsonNode bearerAuth = apiDocs.at("/components/securitySchemes/bearerAuth");
        assertThat(bearerAuth.isMissingNode()).isFalse();
        assertThat(bearerAuth.get("scheme").asText()).isEqualTo("bearer");
        assertThat(bearerAuth.get("bearerFormat").asText()).isEqualTo("JWT");
    }

    @Test
    void noPasswordHashAnywhere() {
        // passwordHash is internal — must NEVER appear in any schema
        assertThat(apiDocs.toString()).doesNotContain("\"passwordHash\"");
    }

    @Test
    void responseSchemasHaveNoPasswordField() {
        // UserResponse and LoginResponse are response DTOs — no password field
        JsonNode userRespProps = apiDocs.at("/components/schemas/UserResponse/properties");
        assertThat(userRespProps.isMissingNode()).isFalse();
        assertThat(userRespProps.has("password")).isFalse();

        JsonNode loginRespProps = apiDocs.at("/components/schemas/LoginResponse/properties");
        assertThat(loginRespProps.isMissingNode()).isFalse();
        assertThat(loginRespProps.has("password")).isFalse();
    }

    @Test
    void loginEndpointHasEmptySecurityRequirement() {
        // POST /api/v1/auth/login must have an empty security array
        // (overridden by @SecurityRequirements({}) — not locked in Swagger UI)
        JsonNode security = apiDocs.at("/paths/~1api~1v1~1auth~1login/post/security");
        assertThat(security.isArray()).isTrue();
        assertThat(security.size()).isZero();
    }
}
