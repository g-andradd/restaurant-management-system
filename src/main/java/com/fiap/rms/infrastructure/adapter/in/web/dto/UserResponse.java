package com.fiap.rms.infrastructure.adapter.in.web.dto;

import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String login,
        Role role,
        Address address,
        Instant createdAt,
        Instant updatedAt
) {}
