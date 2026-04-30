package com.fiap.rms.infrastructure.adapter.in.web.dto;

import com.fiap.rms.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record LoginResponse(
        @Schema(description = "Indica autenticação bem-sucedida", example = "true")
        boolean authenticated,
        @Schema(description = "ID do usuário autenticado")
        UUID userId,
        @Schema(description = "Perfil do usuário")
        Role role,
        @Schema(description = "JWT Bearer token")
        String token,
        @Schema(description = "Expiração em segundos", example = "3600")
        long expiresIn
) {}
