package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.exception.EmailAlreadyExistsException;
import com.fiap.rms.domain.model.User;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public RegisterUserService(UserRepositoryPort userRepository,
                               PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        User user = User.create(
                command.name(),
                command.email(),
                command.login(),
                encodedPassword,
                command.role(),
                command.address()
        );

        return userRepository.save(user);
    }
}
