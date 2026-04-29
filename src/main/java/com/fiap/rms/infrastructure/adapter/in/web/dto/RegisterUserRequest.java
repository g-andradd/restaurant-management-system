package com.fiap.rms.infrastructure.adapter.in.web.dto;

import com.fiap.rms.domain.model.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 60) String login,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                 message = "must contain at least one uppercase letter and one digit")
        String password,
        @NotNull Role role,
        @NotNull @Valid AddressRequest address
) {}
