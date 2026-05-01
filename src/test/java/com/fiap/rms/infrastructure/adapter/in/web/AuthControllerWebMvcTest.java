package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fiap.rms.application.port.in.AuthenticateUserUseCase;
import com.fiap.rms.application.port.out.JwtTokenProviderPort;
import com.fiap.rms.application.usecase.AuthenticationResult;
import com.fiap.rms.domain.exception.InvalidCredentialsException;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.infrastructure.adapter.in.web.security.RestAccessDeniedHandler;
import com.fiap.rms.infrastructure.adapter.in.web.security.RestAuthenticationEntryPoint;
import com.fiap.rms.infrastructure.config.JwtProperties;
import com.fiap.rms.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-must-be-at-least-32-bytes-long-aaaa",
        "security.jwt.expiration-seconds=3600"
})
@WithMockUser
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // JwtAuthenticationFilter is a Filter bean loaded by @WebMvcTest; its port must be mocked
    @MockBean
    private JwtTokenProviderPort jwtTokenProvider;

    @MockBean
    private AuthenticateUserUseCase authenticateUser;

    @Test
    void login_validCredentials_returns200WithFullLoginResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticateUser.authenticate("mariasilva", "Senha@123"))
                .thenReturn(AuthenticationResult.success(userId, Role.CUSTOMER, "fake-jwt"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"mariasilva\",\"password\":\"Senha@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token").value("fake-jwt"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void login_wrongPassword_returns401WithProblemDetail() throws Exception {
        when(authenticateUser.authenticate("mariasilva", "wrong"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"mariasilva\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(containsString("/errors/unauthorized")))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas."));
    }

    @Test
    void login_unknownLogin_returns401BodyIdenticalToWrongPassword() throws Exception {
        when(authenticateUser.authenticate("ghost", "any"))
                .thenThrow(new InvalidCredentialsException());
        when(authenticateUser.authenticate("mariasilva", "wrong"))
                .thenThrow(new InvalidCredentialsException());

        MvcResult unknownResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"ghost\",\"password\":\"any\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        MvcResult wrongPwResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"mariasilva\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String body1 = withoutTimestamp(unknownResult.getResponse().getContentAsString());
        String body2 = withoutTimestamp(wrongPwResult.getResponse().getContentAsString());
        assertThat(body1).isEqualTo(body2);
    }

    @Test
    void login_missingRequiredField_returns400WithValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"\",\"password\":\"Senha@123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(containsString("/errors/validation")))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private String withoutTimestamp(String json) throws Exception {
        ObjectNode node = (ObjectNode) objectMapper.readTree(json);
        node.remove("timestamp");
        return objectMapper.writeValueAsString(node);
    }
}
