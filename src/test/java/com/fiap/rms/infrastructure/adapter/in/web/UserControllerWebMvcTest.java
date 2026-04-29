package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.domain.exception.EmailAlreadyExistsException;
import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import com.fiap.rms.infrastructure.adapter.in.web.dto.AddressRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserWebMapper.class)
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUser;

    private static RegisterUserRequest validRequest() {
        return new RegisterUserRequest(
                "Maria Silva",
                "maria@example.com",
                "mariasilva",
                "Senha@123",
                Role.CUSTOMER,
                new AddressRequest("Rua A", "100", "São Paulo", "01000-000")
        );
    }

    private static User stubUser() {
        return User.create(
                "Maria Silva", "maria@example.com", "mariasilva",
                "$2a$12$hashed", Role.CUSTOMER,
                new Address("Rua A", "100", "São Paulo", "01000-000")
        );
    }

    @Test
    void register_validRequest_returns201WithLocation() throws Exception {
        User user = stubUser();
        when(registerUser.register(any())).thenReturn(user);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        containsString("/api/v1/users/" + user.getId())))
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value("maria@example.com"))
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                // response must never contain any password field
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$").value(not(containsString("password"))));
    }

    @Test
    void register_missingRequiredField_returns400() throws Exception {
        // name is blank — validation must fail
        RegisterUserRequest bad = new RegisterUserRequest(
                "",
                "x@example.com",
                "login1",
                "Senha@123",
                Role.CUSTOMER,
                new AddressRequest("Rua A", "1", "SP", "01000-000")
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        containsString("/errors/validation")))
                .andExpect(jsonPath("$.errors[0].field").exists());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(registerUser.register(any()))
                .thenThrow(new EmailAlreadyExistsException("maria@example.com"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        containsString("/errors/email-conflict")))
                .andExpect(jsonPath("$.status").value(409));
    }
}
