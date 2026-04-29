package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.exception.UserNotFoundException;
import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private ChangePasswordService service;

    private static final Address ADDRESS = new Address("Rua A", "1", "SP", "01000-000");

    private static User existingUser() {
        return User.create("Maria Silva", "maria@example.com", "mariasilva",
                "$2a$12$oldhash", Role.CUSTOMER, ADDRESS);
    }

    @Test
    void changePassword_happyPath_encodesAndSaves() {
        User user = existingUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass@1")).thenReturn("$2a$12$newhash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.changePassword(user.getId(), "NewPass@1");

        verify(passwordEncoder).encode("NewPass@1");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_missingUser_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword(id, "NewPass@1"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_hashPassedToDomainIsEncoderOutput() {
        User user = existingUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Plain@1")).thenReturn("$2a$12$bcryptNewhash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.changePassword(user.getId(), "Plain@1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash())
                .isEqualTo("$2a$12$bcryptNewhash")
                .isNotEqualTo("Plain@1");
    }
}
