package com.fiap.rms.infrastructure.config;

import com.fiap.rms.application.port.in.FindUserByIdUseCase;
import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.application.port.in.SearchUsersByNameUseCase;
import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.application.usecase.FindUserByIdService;
import com.fiap.rms.application.usecase.RegisterUserService;
import com.fiap.rms.application.usecase.SearchUsersByNameService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseBeanConfiguration {

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepository,
                                            PasswordEncoderPort passwordEncoder) {
        return new RegisterUserService(userRepository, passwordEncoder);
    }

    @Bean
    FindUserByIdUseCase findUserByIdUseCase(UserRepositoryPort userRepository) {
        return new FindUserByIdService(userRepository);
    }

    @Bean
    SearchUsersByNameUseCase searchUsersByNameUseCase(UserRepositoryPort userRepository) {
        return new SearchUsersByNameService(userRepository);
    }
}
