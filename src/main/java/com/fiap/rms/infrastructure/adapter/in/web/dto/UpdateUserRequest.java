package com.fiap.rms.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Schema(description = "Nome completo atualizado", example = "Maria Souza")
        @NotBlank @Size(max = 150) String name,
        @Schema(description = "E-mail atualizado", example = "maria.souza@email.com")
        @NotBlank @Email String email,
        @Schema(description = "Login atualizado", example = "mariasouza")
        @NotBlank @Size(min = 3, max = 60) String login,
        @Schema(description = "Endereço atualizado")
        @NotNull @Valid AddressRequest address
) {}
