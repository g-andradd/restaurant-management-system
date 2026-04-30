package com.fiap.rms.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class OpenApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode apiDocs;

    @BeforeEach
    void setUp() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        apiDocs = objectMapper.readTree(body);
    }

    @Test
    void containsExpectedPathsAndReusableSchemas() {
        assertThat(apiDocs.at("/paths/~1api~1v1~1users").isMissingNode()).isFalse();
        assertThat(apiDocs.at("/paths/~1api~1v1~1users~1{id}").isMissingNode()).isFalse();
        assertThat(apiDocs.at("/paths/~1api~1v1~1users~1{id}~1password").isMissingNode()).isFalse();
        assertThat(apiDocs.at("/paths/~1api~1v1~1auth~1login").isMissingNode()).isFalse();
        assertThat(apiDocs.at("/components/schemas/ProblemDetail").isMissingNode()).isFalse();
        assertThat(apiDocs.at("/components/schemas/ValidationProblemDetail").isMissingNode()).isFalse();
    }

    @Test
    void noPasswordHashAnywhere() {
        assertThat(apiDocs.toString()).doesNotContain("\"passwordHash\"");
    }

    @Test
    void responseSchemasHaveNoPasswordField() {
        JsonNode userResp = apiDocs.at("/components/schemas/UserResponse/properties");
        assertThat(userResp.isMissingNode()).isFalse();
        assertThat(userResp.has("password")).isFalse();

        JsonNode loginResp = apiDocs.at("/components/schemas/LoginResponse/properties");
        assertThat(loginResp.isMissingNode()).isFalse();
        assertThat(loginResp.has("password")).isFalse();
    }
}
