package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.in.FindUserByIdUseCase;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.exception.UserNotFoundException;
import com.fiap.rms.domain.model.User;

import java.util.UUID;

public class FindUserByIdService implements FindUserByIdUseCase {

    private final UserRepositoryPort userRepository;

    public FindUserByIdService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }
}
