package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.in.DeleteUserUseCase;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.exception.UserNotFoundException;

import java.util.UUID;

public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort userRepository;

    public DeleteUserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void delete(UUID id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException(id.toString());
        }
        userRepository.deleteById(id);
    }
}
