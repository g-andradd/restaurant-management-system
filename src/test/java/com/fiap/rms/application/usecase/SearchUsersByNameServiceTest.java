package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchUsersByNameServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private SearchUsersByNameService service;

    private static final Address ADDRESS = new Address("Rua A", "1", "SP", "01000-000");

    @Test
    void searchByName_withMatches_returnsList() {
        User maria = User.create("Maria Silva", "maria@example.com", "mariasilva",
                "$2a$12$hash", Role.CUSTOMER, ADDRESS);
        User marcos = User.create("Marcos Lima", "marcos@example.com", "marcoslima",
                "$2a$12$hash", Role.CUSTOMER, ADDRESS);
        when(userRepository.findByNameContainingIgnoreCase("mar")).thenReturn(List.of(maria, marcos));

        List<User> result = service.searchByName("mar");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getName)
                .containsExactlyInAnyOrder("Maria Silva", "Marcos Lima");
    }

    @Test
    void searchByName_noMatches_returnsEmptyList() {
        when(userRepository.findByNameContainingIgnoreCase("zzz")).thenReturn(List.of());

        List<User> result = service.searchByName("zzz");

        assertThat(result).isEmpty();
    }
}
