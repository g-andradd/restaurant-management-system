package com.fiap.rms.infrastructure.adapter.in.web.security;

import com.fiap.rms.domain.model.Role;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, Role role) {}
