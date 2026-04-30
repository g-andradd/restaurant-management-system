package com.fiap.rms.infrastructure.adapter.out.security;

import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.application.usecase.AuthenticationResult;
import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseAuthenticationAdapterTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private DatabaseAuthenticationAdapter adapter;

    private static final Address ADDRESS = new Address("Rua A", "1", "SP", "01000-000");

    private static User stubUser() {
        return User.create("Ana Doe", "ana@example.com", "anadoe",
                "$2a$12$hashed", Role.CUSTOMER, ADDRESS);
    }

    @Test
    void authenticate_unknownLogin_returnsFailureWithoutCallingPasswordEncoder() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        AuthenticationResult result = adapter.authenticate("ghost", "anything");

        assertThat(result).isEqualTo(AuthenticationResult.failure());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_wrongPassword_returnsFailure() {
        User user = stubUser();
        when(userRepository.findByLogin("anadoe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$12$hashed")).thenReturn(false);

        AuthenticationResult result = adapter.authenticate("anadoe", "wrong");

        assertThat(result).isEqualTo(AuthenticationResult.failure());
    }

    @Test
    void authenticate_unknownLoginAndWrongPassword_returnStructurallyIdenticalFailure() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());
        User user = stubUser();
        when(userRepository.findByLogin("anadoe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "$2a$12$hashed")).thenReturn(false);

        AuthenticationResult unknownResult = adapter.authenticate("ghost", "irrelevant");
        AuthenticationResult wrongPwResult = adapter.authenticate("anadoe", "bad");

        // No distinguishable information leakage between the two failure paths
        assertThat(unknownResult).isEqualTo(AuthenticationResult.failure());
        assertThat(wrongPwResult).isEqualTo(AuthenticationResult.failure());
        assertThat(unknownResult).isEqualTo(wrongPwResult);
    }

    @Test
    void authenticate_correctCredentials_returnsSuccessWithNullToken() {
        User user = stubUser();
        when(userRepository.findByLogin("anadoe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha@123", "$2a$12$hashed")).thenReturn(true);

        AuthenticationResult result = adapter.authenticate("anadoe", "Senha@123");

        assertThat(result.authenticated()).isTrue();
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.role()).isEqualTo(Role.CUSTOMER);
        // Token issuance is the use case's responsibility — adapter always returns null here
        assertThat(result.token()).isNull();
    }
}
