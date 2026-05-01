package com.fiap.rms.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Schema(description = "Nome completo do usuário", example = "Maria da Silva Souza")
        @NotBlank @Size(max = 150) String name,

        @Schema(description = "Endereço de e-mail único", example = "maria.souza@exemplo.com.br")
        @NotBlank @Email String email,

        @Schema(description = "Login único para autenticação", example = "mariasouza")
        @NotBlank @Size(min = 3, max = 60) String login,

        @Schema(description = "Novo endereço de entrega/contato do usuário")
        @NotNull @Valid AddressRequest address
) {}
