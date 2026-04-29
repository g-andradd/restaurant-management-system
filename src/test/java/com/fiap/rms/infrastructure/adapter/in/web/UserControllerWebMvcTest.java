package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.rms.application.port.in.FindUserByIdUseCase;
import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.application.port.in.SearchUsersByNameUseCase;
import com.fiap.rms.domain.exception.EmailAlreadyExistsException;
import com.fiap.rms.domain.exception.UserNotFoundException;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockBean
    private FindUserByIdUseCase findUserById;

    @MockBean
    private SearchUsersByNameUseCase searchUsersByName;

    private static final Address ADDRESS = new Address("Rua A", "100", "São Paulo", "01000-000");

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
                "$2a$12$hashed", Role.CUSTOMER, ADDRESS
        );
    }

    // ── POST /api/v1/users ──────────────────────────────────────────────────

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
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$").value(not(containsString("password"))));
    }

    @Test
    void register_missingRequiredField_returns400() throws Exception {
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

    // ── GET /api/v1/users/{id} ──────────────────────────────────────────────

    @Test
    void findById_existingUser_returns200WithUserResponse() throws Exception {
        User user = stubUser();
        when(findUserById.findById(user.getId())).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value("maria@example.com"))
                .andExpect(jsonPath("$.name").value("Maria Silva"));
    }

    @Test
    void findById_missingUser_returns404WithProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(findUserById.findById(id)).thenThrow(new UserNotFoundException(id.toString()));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(
                        containsString("/errors/user-not-found")))
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── GET /api/v1/users?name=... ──────────────────────────────────────────

    @Test
    void searchByName_noMatches_returns200WithEmptyList() throws Exception {
        when(searchUsersByName.searchByName("zzz")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users").param("name", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchByName_withMatches_returns200WithList() throws Exception {
        User maria = User.create("Maria Silva", "maria@example.com", "mariasilva",
                "$2a$12$hash", Role.CUSTOMER, ADDRESS);
        User marcos = User.create("Marcos Lima", "marcos@example.com", "marcoslima",
                "$2a$12$hash", Role.CUSTOMER, ADDRESS);
        when(searchUsersByName.searchByName("mar")).thenReturn(List.of(maria, marcos));

        mockMvc.perform(get("/api/v1/users").param("name", "mar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchByName_missingNameParam_returns400WithProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        containsString("/errors/validation")))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("parâmetro obrigatório ausente"));
    }
}
