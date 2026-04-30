package com.fiap.rms.infrastructure.adapter.in.web.dto;

import com.fiap.rms.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @Schema(description = "Nome completo do usuário", example = "Maria Silva")
        @NotBlank @Size(max = 150) String name,
        @Schema(description = "E-mail único do usuário", example = "maria.silva@email.com")
        @NotBlank @Email String email,
        @Schema(description = "Login para autenticação", example = "mariasilva")
        @NotBlank @Size(min = 3, max = 60) String login,
        @Schema(description = "Senha em texto plano (mín. 8, com maiúscula e número)", example = "Senha@123")
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                 message = "must contain at least one uppercase letter and one digit")
        String password,
        @Schema(description = "Perfil do usuário", example = "CUSTOMER")
        @NotNull Role role,
        @Schema(description = "Endereço do usuário")
        @NotNull @Valid AddressRequest address
) {}
