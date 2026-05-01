package com.fiap.rms.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @Schema(description = "Nome da rua ou avenida", example = "Rua das Flores")
        @NotBlank String street,

        @Schema(description = "Número do imóvel", example = "42")
        @NotBlank String number,

        @Schema(description = "Cidade", example = "São Paulo")
        @NotBlank String city,

        @Schema(description = "CEP no formato NNNNN-NNN", example = "01310-100")
        @NotBlank String zipCode
) {}
