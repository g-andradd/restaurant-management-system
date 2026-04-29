package com.fiap.rms.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 60) String login,
        @NotNull @Valid AddressRequest address
) {}
