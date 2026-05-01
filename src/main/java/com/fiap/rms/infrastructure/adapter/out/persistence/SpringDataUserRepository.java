package com.fiap.rms.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByLogin(String login);

    List<UserJpaEntity> findByNameContainingIgnoreCase(String name);

    boolean existsByEmail(String email);
}
