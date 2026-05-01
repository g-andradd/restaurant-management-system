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
        @Schema(description = "Nome completo do usuário", example = "Maria da Silva")
        @NotBlank @Size(max = 150) String name,

        @Schema(description = "Endereço de e-mail único", example = "maria@exemplo.com.br")
        @NotBlank @Email String email,

        @Schema(description = "Login único para autenticação", example = "mariasilva")
        @NotBlank @Size(min = 3, max = 60) String login,

        @Schema(description = "Senha — mínimo 8 caracteres, ao menos uma maiúscula e um dígito",
                example = "Senha@123")
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                 message = "must contain at least one uppercase letter and one digit")
        String password,

        @Schema(description = "Papel do usuário no sistema", example = "CUSTOMER")
        @NotNull Role role,

        @Schema(description = "Endereço de entrega/contato do usuário")
        @NotNull @Valid AddressRequest address
) {}
