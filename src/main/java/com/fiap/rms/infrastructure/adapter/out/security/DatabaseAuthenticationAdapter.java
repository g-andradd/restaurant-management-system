package com.fiap.rms.infrastructure.adapter.out.security;

import com.fiap.rms.application.port.out.AuthenticationStrategyPort;
import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.application.usecase.AuthenticationResult;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAuthenticationAdapter implements AuthenticationStrategyPort {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public DatabaseAuthenticationAdapter(UserRepositoryPort userRepository,
                                         PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthenticationResult authenticate(String login, String rawPassword) {
        return userRepository.findByLogin(login)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
                .map(user -> AuthenticationResult.success(user.getId(), user.getRole(), null))
                .orElse(AuthenticationResult.failure());
    }
}
