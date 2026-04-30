package com.fiap.rms.application.port.out;

import com.fiap.rms.application.usecase.TokenPayload;
import com.fiap.rms.domain.model.Role;

import java.util.Optional;
import java.util.UUID;

public interface JwtTokenProviderPort {

    String generateToken(UUID userId, Role role);

    Optional<TokenPayload> parseToken(String token);
}
