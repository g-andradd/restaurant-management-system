package com.fiap.rms.application.usecase;

import com.fiap.rms.application.port.in.SearchUsersByNameUseCase;
import com.fiap.rms.application.port.out.UserRepositoryPort;
import com.fiap.rms.domain.model.User;

import java.util.List;

public class SearchUsersByNameService implements SearchUsersByNameUseCase {

    private final UserRepositoryPort userRepository;

    public SearchUsersByNameService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> searchByName(String term) {
        return userRepository.findByNameContainingIgnoreCase(term);
    }
}
