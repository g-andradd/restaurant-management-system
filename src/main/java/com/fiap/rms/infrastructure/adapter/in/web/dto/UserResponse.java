package com.fiap.rms.infrastructure.adapter.in.web.dto;

import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        @Schema(description = "Identificador do usuário")
        UUID id,
        @Schema(description = "Nome completo")
        String name,
        @Schema(description = "E-mail")
        String email,
        @Schema(description = "Login")
        String login,
        @Schema(description = "Perfil")
        Role role,
        @Schema(description = "Endereço")
        Address address,
        @Schema(description = "Data/hora de criação")
        Instant createdAt,
        @Schema(description = "Data/hora da última atualização")
        Instant updatedAt
) {}
