package com.fiap.rms.application.port.out;

import com.fiap.rms.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByLogin(String login);

    List<User> findByNameContainingIgnoreCase(String term);

    boolean existsByEmail(String email);

    void deleteById(UUID id);
}
