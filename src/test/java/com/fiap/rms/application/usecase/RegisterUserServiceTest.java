package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.exception.EmailAlreadyExistsException;
import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private RegisterUserService service;

    private static final Address ADDRESS =
            new Address("Rua A", "1", "SP", "01000-000");

    @Test
    void register_happyPath_encodesPasswordAndSavesUser() {
        RegisterUserCommand command = new RegisterUserCommand(
                "João Silva", "joao@example.com", "joaosilva",
                "Senha@123", Role.CUSTOMER, ADDRESS);

        when(userRepository.existsByEmail("joao@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$12$hashedValue");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.register(command);

        assertThat(result.getEmail()).isEqualTo("joao@example.com");
        assertThat(result.getName()).isEqualTo("João Silva");
        verify(passwordEncoder).encode("Senha@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyExistsException() {
        RegisterUserCommand command = new RegisterUserCommand(
                "Maria", "dup@example.com", "maria",
                "Senha@123", Role.CUSTOMER, ADDRESS);

        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_plainPasswordNeverPersisted() {
        RegisterUserCommand command = new RegisterUserCommand(
                "Ana", "ana@example.com", "anadoe",
                "Plain@123", Role.CUSTOMER, ADDRESS);

        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Plain@123")).thenReturn("$2a$12$bcryptHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        service.register(command);

        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPasswordHash())
                .isNotEqualTo("Plain@123")
                .isEqualTo("$2a$12$bcryptHash");
    }
}
