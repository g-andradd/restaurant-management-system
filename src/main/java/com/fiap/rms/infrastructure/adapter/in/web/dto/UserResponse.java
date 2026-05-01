package com.fiap.rms.infrastructure.adapter.in.web.dto;

import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        @Schema(description = "Identificador único do usuário",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Nome completo do usuário", example = "Maria da Silva")
        String name,

        @Schema(description = "Endereço de e-mail", example = "maria@exemplo.com.br")
        String email,

        @Schema(description = "Login do usuário", example = "mariasilva")
        String login,

        @Schema(description = "Papel do usuário no sistema", example = "CUSTOMER")
        Role role,

        @Schema(description = "Endereço de entrega/contato do usuário")
        Address address,

        @Schema(description = "Data e hora de criação do registro (UTC)",
                example = "2026-04-30T10:00:00Z")
        Instant createdAt,

        @Schema(description = "Data e hora da última atualização do registro (UTC)",
                example = "2026-04-30T10:00:00Z")
        Instant updatedAt
) {}
