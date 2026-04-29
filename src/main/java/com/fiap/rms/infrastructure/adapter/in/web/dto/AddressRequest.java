package com.fiap.rms.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String street,
        @NotBlank String number,
        @NotBlank String city,
        @NotBlank String zipCode
) {}
