package com.fiap.rms.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Login do usuário", example = "mariasilva")
        @NotBlank String login,
        @Schema(description = "Senha do usuário", example = "Senha@123")
        @NotBlank String password
) {}
