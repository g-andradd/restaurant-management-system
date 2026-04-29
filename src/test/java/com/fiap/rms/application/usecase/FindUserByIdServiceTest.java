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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserByIdServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private FindUserByIdService service;

    private static final Address ADDRESS = new Address("Rua A", "1", "SP", "01000-000");

    @Test
    void findById_existingUser_returnsUser() {
        User user = User.create("Maria Silva", "maria@example.com", "mariasilva",
                "$2a$12$hash", Role.CUSTOMER, ADDRESS);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = service.findById(user.getId());

        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo("maria@example.com");
    }

    @Test
    void findById_missingUser_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(UserNotFoundException.class);
    }
}
