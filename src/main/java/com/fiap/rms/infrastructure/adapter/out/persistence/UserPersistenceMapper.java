package com.fiap.rms.infrastructure.adapter.out.persistence;

import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.User;
import org.springframework.stereotype.Component;

@Component
class UserPersistenceMapper {

    UserJpaEntity toJpa(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .login(user.getLogin())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .street(user.getAddress().street())
                .number(user.getAddress().number())
                .city(user.getAddress().city())
                .zipCode(user.getAddress().zipCode())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    User toDomain(UserJpaEntity e) {
        return User.rehydrate(
                e.getId(),
                e.getName(),
                e.getEmail(),
                e.getLogin(),
                e.getPasswordHash(),
                e.getRole(),
                new Address(e.getStreet(), e.getNumber(), e.getCity(), e.getZipCode()),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
