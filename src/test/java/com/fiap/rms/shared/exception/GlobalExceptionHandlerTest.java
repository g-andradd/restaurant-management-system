package com.fiap.rms.shared.exception;

import com.fiap.rms.application.port.out.JwtTokenProviderPort;
import com.fiap.rms.infrastructure.adapter.in.web.ErrorProbeController;
import com.fiap.rms.infrastructure.adapter.in.web.security.RestAccessDeniedHandler;
import com.fiap.rms.infrastructure.adapter.in.web.security.RestAuthenticationEntryPoint;
import com.fiap.rms.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ErrorProbeController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@ActiveProfiles("dev")
@WithMockUser
class GlobalExceptionHandlerTest {

    // JwtAuthenticationFilter is a Filter bean loaded by @WebMvcTest; its port must be mocked
    @MockBean
    private JwtTokenProviderPort jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validationError_returns400WithErrorsArray() throws Exception {
        mockMvc.perform(post("/probe/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://api.techchallenge.com/errors/validation"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void notFound_returns404WithProblemDetail() throws Exception {
        mockMvc.perform(get("/probe/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://api.techchallenge.com/errors/not-found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void internalError_returns500WithGenericMessageAndNoStackTrace() throws Exception {
        String body = mockMvc.perform(get("/probe/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://api.techchallenge.com/errors/internal"))
                .andExpect(jsonPath("$.detail").value("Erro interno. Tente novamente mais tarde."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("RuntimeException");
        assertThat(body).doesNotContain("\tat ");
    }
}
