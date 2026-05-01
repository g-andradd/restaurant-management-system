package com.fiap.rms.application.usecase;

import com.fiap.rms.domain.model.Role;

import java.util.UUID;

public record AuthenticationResult(
        boolean authenticated,
        UUID userId,
        Role role,
        String token
) {

    public static AuthenticationResult success(UUID userId, Role role, String token) {
        return new AuthenticationResult(true, userId, role, token);
    }

    public static AuthenticationResult failure() {
        return new AuthenticationResult(false, null, null, null);
    }
}
