package com.fiap.rms.infrastructure.adapter.in.web.dto;

import com.fiap.rms.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record LoginResponse(
        @Schema(description = "Indica se a autenticação foi bem-sucedida", example = "true")
        boolean authenticated,

        @Schema(description = "Identificador único do usuário autenticado",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID userId,

        @Schema(description = "Papel (role) do usuário no sistema", example = "CUSTOMER")
        Role role,

        @Schema(description = "Token JWT para uso nas requisições subsequentes",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzZmE4NWY2NC01NzE3LTQ1NjItYjNmYy0yYzk2M2Y2NmFmYTYifQ.abc")
        String token,

        @Schema(description = "Tempo de expiração do token em segundos", example = "3600")
        long expiresIn
) {}
