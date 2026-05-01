package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.out.UserRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private DeleteUserService service;

    private static final Address ADDRESS = new Address("Rua A", "1", "SP", "01000-000");

    @Test
    void delete_happyPath_deletesUser() {
        User user = User.create("Maria Silva", "maria@example.com", "mariasilva",
                "$2a$12$hash", Role.CUSTOMER, ADDRESS);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.delete(user.getId());

        verify(userRepository).deleteById(user.getId());
    }

    @Test
    void delete_missingUser_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
