package com.fiap.rms.infrastructure.config;

import com.fiap.rms.application.port.in.ChangePasswordUseCase;
import com.fiap.rms.application.port.in.DeleteUserUseCase;
import com.fiap.rms.application.port.in.FindUserByIdUseCase;
import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.application.port.in.SearchUsersByNameUseCase;
import com.fiap.rms.application.port.in.UpdateUserUseCase;
import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.application.usecase.ChangePasswordService;
import com.fiap.rms.application.usecase.DeleteUserService;
import com.fiap.rms.application.usecase.FindUserByIdService;
import com.fiap.rms.application.usecase.RegisterUserService;
import com.fiap.rms.application.usecase.SearchUsersByNameService;
import com.fiap.rms.application.usecase.UpdateUserService;
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

    @Bean
    UpdateUserUseCase updateUserUseCase(UserRepositoryPort userRepository) {
        return new UpdateUserService(userRepository);
    }

    @Bean
    ChangePasswordUseCase changePasswordUseCase(UserRepositoryPort userRepository,
                                                PasswordEncoderPort passwordEncoder) {
        return new ChangePasswordService(userRepository, passwordEncoder);
    }

    @Bean
    DeleteUserUseCase deleteUserUseCase(UserRepositoryPort userRepository) {
        return new DeleteUserService(userRepository);
    }
}
