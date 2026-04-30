package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.in.AuthenticateUserUseCase;
import com.fiap.rms.application.port.out.AuthenticationStrategyPort;
import com.fiap.rms.application.port.out.JwtTokenProviderPort;
import com.fiap.rms.domain.exception.InvalidCredentialsException;

public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final AuthenticationStrategyPort authenticationStrategy;
    private final JwtTokenProviderPort jwtTokenProvider;

    public AuthenticateUserService(AuthenticationStrategyPort authenticationStrategy,
                                   JwtTokenProviderPort jwtTokenProvider) {
        this.authenticationStrategy = authenticationStrategy;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthenticationResult authenticate(String login, String rawPassword) {
        AuthenticationResult result = authenticationStrategy.authenticate(login, rawPassword);

        if (!result.authenticated()) {
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenProvider.generateToken(result.userId(), result.role());
        return AuthenticationResult.success(result.userId(), result.role(), token);
    }
}
