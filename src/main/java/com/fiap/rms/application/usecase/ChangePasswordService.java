package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.in.ChangePasswordUseCase;
import com.fiap.rms.application.port.out.PasswordEncoderPort;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.exception.UserNotFoundException;
import com.fiap.rms.domain.model.User;

import java.util.UUID;

public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public ChangePasswordService(UserRepositoryPort userRepository,
                                 PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void changePassword(UUID id, String newPlainPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));

        String hash = passwordEncoder.encode(newPlainPassword);
        user.changePassword(hash);
        userRepository.save(user);
    }
}
