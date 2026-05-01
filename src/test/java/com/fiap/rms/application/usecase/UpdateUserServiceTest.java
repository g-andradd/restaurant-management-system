package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.exception.EmailAlreadyExistsException;
import com.fiap.rms.domain.exception.LoginAlreadyExistsException;
import com.fiap.rms.domain.exception.UserNotFoundException;
import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private UpdateUserService service;

    private static final Address ADDRESS = new Address("Rua A", "1", "SP", "01000-000");
    private static final Address NEW_ADDRESS = new Address("Rua B", "2", "RJ", "20000-000");

    private static User existingUser() {
        return User.create("Maria Silva", "maria@example.com", "mariasilva",
                "$2a$12$hash", Role.CUSTOMER, ADDRESS);
    }

    @Test
    void update_happyPath_updatesAndReturnsUser() {
        User user = existingUser();
        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Souza", "maria@example.com", "mariasouza", NEW_ADDRESS);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.update(user.getId(), command);

        assertThat(result.getName()).isEqualTo("Maria Souza");
        assertThat(result.getLogin()).isEqualTo("mariasouza");
        verify(userRepository).save(user);
    }

    @Test
    void update_missingUser_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        UpdateUserCommand command = new UpdateUserCommand(
                "X", "x@example.com", "xuser", ADDRESS);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, command))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_emailNotChanged_doesNotCallExistsByEmail() {
        User user = existingUser();
        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Nova", "maria@example.com", "marianew", NEW_ADDRESS);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.update(user.getId(), command);

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, times(1)).existsByLogin("marianew");
    }

    @Test
    void update_emailChangedAndFree_callsExistsByEmailAndProceeds() {
        User user = existingUser();
        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Silva", "new@example.com", "mariasilva", ADDRESS);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.update(user.getId(), command);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).existsByEmail("new@example.com");
    }

    @Test
    void update_emailChangedAndTaken_throwsEmailAlreadyExistsException() {
        User user = existingUser();
        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Silva", "taken@example.com", "mariasilva", ADDRESS);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(user.getId(), command))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_loginNotChanged_doesNotCallExistsByLogin() {
        User user = existingUser();
        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Nova", "new@example.com", "mariasilva", NEW_ADDRESS);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.update(user.getId(), command);

        verify(userRepository, never()).existsByLogin(any());
    }

    @Test
    void update_loginChangedAndTaken_throwsLoginAlreadyExistsException() {
        User user = existingUser();
        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Silva", "maria@example.com", "takenlogin", ADDRESS);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByLogin("takenlogin")).thenReturn(true);

        assertThatThrownBy(() -> service.update(user.getId(), command))
                .isInstanceOf(LoginAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}
