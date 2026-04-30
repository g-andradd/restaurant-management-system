package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.out.AuthenticationStrategyPort;
import com.fiap.rms.application.port.out.JwtTokenProviderPort;
import com.fiap.rms.domain.exception.InvalidCredentialsException;
import com.fiap.rms.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    @Mock
    private AuthenticationStrategyPort authenticationStrategy;

    @Mock
    private JwtTokenProviderPort jwtTokenProvider;

    @InjectMocks
    private AuthenticateUserService service;

    @Test
    void authenticate_validCredentials_callsTokenProviderAndReturnsTokenInResult() {
        UUID userId = UUID.randomUUID();
        // Strategy returns success but without a token (token field is null — strategy's job is only to verify)
        when(authenticationStrategy.authenticate("mariasilva", "Senha@123"))
                .thenReturn(AuthenticationResult.success(userId, Role.CUSTOMER, null));
        when(jwtTokenProvider.generateToken(userId, Role.CUSTOMER))
                .thenReturn("fake-jwt");

        AuthenticationResult result = service.authenticate("mariasilva", "Senha@123");

        assertThat(result.authenticated()).isTrue();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.role()).isEqualTo(Role.CUSTOMER);
        assertThat(result.token()).isEqualTo("fake-jwt");
        verify(jwtTokenProvider).generateToken(userId, Role.CUSTOMER);
    }

    @Test
    void authenticate_invalidCredentials_throwsAndNeverCallsTokenProvider() {
        when(authenticationStrategy.authenticate("ghost", "bad"))
                .thenReturn(AuthenticationResult.failure());

        assertThatThrownBy(() -> service.authenticate("ghost", "bad"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }
}
