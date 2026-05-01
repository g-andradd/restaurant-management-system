package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.in.UpdateUserUseCase;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.exception.EmailAlreadyExistsException;
import com.fiap.rms.domain.exception.LoginAlreadyExistsException;
import com.fiap.rms.domain.exception.UserNotFoundException;
import com.fiap.rms.domain.model.User;

import java.util.UUID;

public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepositoryPort userRepository;

    public UpdateUserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User update(UUID id, UpdateUserCommand command) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));

        if (!command.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(command.email())) {
                throw new EmailAlreadyExistsException(command.email());
            }
        }
        if (!command.login().equals(user.getLogin())) {
            if (userRepository.existsByLogin(command.login())) {
                throw new LoginAlreadyExistsException(command.login());
            }
        }

        user.updateProfile(command.name(), command.email(),
                command.login(), command.address());

        return userRepository.save(user);
    }
}
