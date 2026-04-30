package com.fiap.rms.infrastructure.adapter.in.web.dto;

import com.fiap.rms.domain.model.Role;

import java.util.UUID;

public record LoginResponse(
        boolean authenticated,
        UUID userId,
        Role role,
        String token,
        long expiresIn
) {}
