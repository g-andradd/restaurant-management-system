package com.fiap.rms.application.usecase;

import com.fiap.rms.domain.model.Role;

import java.time.Instant;
import java.util.UUID;

public record TokenPayload(UUID userId, Role role, Instant expiresAt) {}
