package com.fiap.rms.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Schema(description = "Nova senha — mínimo 8 caracteres, ao menos uma maiúscula e um dígito",
                example = "NovaSenha@1")
        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                 message = "must contain at least one uppercase letter and one digit")
        String newPassword
) {}
