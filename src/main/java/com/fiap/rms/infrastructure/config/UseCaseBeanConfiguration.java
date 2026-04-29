package com.fiap.rms.infrastructure.config;

import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.application.usecase.RegisterUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseBeanConfiguration {

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepository,
                                            PasswordEncoderPort passwordEncoder) {
        return new RegisterUserService(userRepository, passwordEncoder);
    }
}
