package com.fiap.rms.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.rms.application.port.in.ChangePasswordUseCase;
import com.fiap.rms.application.port.out.JwtTokenProviderPort;
import com.fiap.rms.application.port.in.DeleteUserUseCase;
import com.fiap.rms.application.port.in.FindUserByIdUseCase;
import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.application.port.in.SearchUsersByNameUseCase;
import com.fiap.rms.application.port.in.UpdateUserUseCase;
import com.fiap.rms.domain.exception.EmailAlreadyExistsException;
import com.fiap.rms.domain.exception.UserNotFoundException;
import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import com.fiap.rms.infrastructure.adapter.in.web.dto.AddressRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.ChangePasswordRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.fiap.rms.application.usecase.UpdateUserCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fiap.rms.infrastructure.adapter.in.web.security.RestAuthenticationEntryPoint;
import com.fiap.rms.infrastructure.adapter.in.web.security.RestAccessDeniedHandler;
import com.fiap.rms.infrastructure.config.SecurityConfig;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({UserWebMapper.class, SecurityConfig.class,
        RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@WithMockUser
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // JwtAuthenticationFilter is a Filter bean loaded by @WebMvcTest; its port must be mocked
    @MockBean
    private JwtTokenProviderPort jwtTokenProvider;

    @MockBean
    private RegisterUserUseCase registerUser;

    @MockBean
    private FindUserByIdUseCase findUserById;

    @MockBean
    private SearchUsersByNameUseCase searchUsersByName;

    @MockBean
    private UpdateUserUseCase updateUser;

    @MockBean
    private ChangePasswordUseCase changePassword;

    @MockBean
    private DeleteUserUseCase deleteUser;

    private static final Address ADDRESS = new Address("Rua A", "100", "São Paulo", "01000-000");

    private static RegisterUserRequest validRegisterRequest() {
        return new RegisterUserRequest(
                "Maria Silva", "maria@example.com", "mariasilva",
                "Senha@123", Role.CUSTOMER,
                new AddressRequest("Rua A", "100", "São Paulo", "01000-000"));
    }

    private static UpdateUserRequest validUpdateRequest() {
        return new UpdateUserRequest(
                "Maria Souza", "maria@example.com", "mariasouza",
                new AddressRequest("Rua B", "200", "Rio de Janeiro", "20000-000"));
    }

    private static User stubUser() {
        return User.create(
                "Maria Silva", "maria@example.com", "mariasilva",
                "$2a$12$hashed", Role.CUSTOMER, ADDRESS);
    }

    // ── POST /api/v1/users ──────────────────────────────────────────────────

    @Test
    void register_validRequest_returns201WithLocation() throws Exception {
        User user = stubUser();
        when(registerUser.register(any())).thenReturn(user);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest())))
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
                "", "x@example.com", "login1", "Senha@123", Role.CUSTOMER,
                new AddressRequest("Rua A", "1", "SP", "01000-000"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(containsString("/errors/validation")))
                .andExpect(jsonPath("$.errors[0].field").exists());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(registerUser.register(any()))
                .thenThrow(new EmailAlreadyExistsException("maria@example.com"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(containsString("/errors/email-conflict")))
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
                .andExpect(jsonPath("$.type").value(containsString("/errors/user-not-found")))
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
                .andExpect(jsonPath("$.type").value(containsString("/errors/validation")))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("parâmetro obrigatório ausente"));
    }

    // ── PUT /api/v1/users/{id} ──────────────────────────────────────────────

    @Test
    void update_validRequest_returns200WithUserResponse() throws Exception {
        User user = stubUser();
        when(updateUser.update(eq(user.getId()), any())).thenReturn(user);

        mockMvc.perform(put("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()));
    }

    @Test
    void update_missingRequiredField_returns400() throws Exception {
        UpdateUserRequest bad = new UpdateUserRequest(
                "", "x@example.com", "login1",
                new AddressRequest("Rua A", "1", "SP", "01000-000"));

        mockMvc.perform(put("/api/v1/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(containsString("/errors/validation")));
    }

    @Test
    void update_missingUser_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateUser.update(eq(id), any()))
                .thenThrow(new UserNotFoundException(id.toString()));

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(containsString("/errors/user-not-found")));
    }

    @Test
    void update_emailCollision_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateUser.update(eq(id), any()))
                .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(containsString("/errors/email-conflict")));
    }

    @Test
    void update_extraPasswordFieldInBody_isIgnoredAndUseCaseCalledWithCommand() throws Exception {
        User user = stubUser();
        when(updateUser.update(eq(user.getId()), any())).thenReturn(user);

        String bodyWithExtraField = """
                {"name":"Maria Silva","email":"maria@example.com","login":"mariasilva",
                 "password":"ShouldBeIgnored@1",
                 "address":{"street":"Rua A","number":"100","city":"São Paulo","zipCode":"01000-000"}}
                """;

        mockMvc.perform(put("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithExtraField))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateUserCommand> captor = ArgumentCaptor.forClass(UpdateUserCommand.class);
        verify(updateUser).update(eq(user.getId()), captor.capture());
        UpdateUserCommand cmd = captor.getValue();
        assertThat(cmd.name()).isEqualTo("Maria Silva");
        // Structural proof: UpdateUserCommand has no password() component.
        // The record definition is the constraint — no runtime assertion needed.
    }

    // ── PATCH /api/v1/users/{id}/password ──────────────────────────────────

    @Test
    void changePassword_validRequest_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(changePassword).changePassword(eq(id), any());

        mockMvc.perform(patch("/api/v1/users/{id}/password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("NovaSenha@1"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_weakPassword_returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/users/{id}/password", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("weak"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(containsString("/errors/validation")));
    }

    @Test
    void changePassword_missingUser_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new UserNotFoundException(id.toString()))
                .when(changePassword).changePassword(eq(id), any());

        mockMvc.perform(patch("/api/v1/users/{id}/password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("NovaSenha@1"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(containsString("/errors/user-not-found")));
    }

    // ── DELETE /api/v1/users/{id} ───────────────────────────────────────────

    @Test
    void delete_existingUser_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteUser).delete(id);

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_missingUser_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new UserNotFoundException(id.toString()))
                .when(deleteUser).delete(id);

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(containsString("/errors/user-not-found")));
    }
}
