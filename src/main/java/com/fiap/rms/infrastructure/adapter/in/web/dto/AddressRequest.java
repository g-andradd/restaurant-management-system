package com.fiap.rms.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @Schema(description = "Logradouro", example = "Rua das Flores")
        @NotBlank String street,
        @Schema(description = "Número", example = "123")
        @NotBlank String number,
        @Schema(description = "Cidade", example = "São Paulo")
        @NotBlank String city,
        @Schema(description = "CEP", example = "01000-000")
        @NotBlank String zipCode
) {}
