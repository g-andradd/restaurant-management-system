package com.fiap.rms.infrastructure.adapter.in.web;

import com.fiap.rms.application.port.in.AuthenticateUserUseCase;
import com.fiap.rms.application.usecase.AuthenticationResult;
import com.fiap.rms.infrastructure.adapter.in.web.dto.LoginRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.LoginResponse;
import com.fiap.rms.infrastructure.config.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Emissão de tokens JWT")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUser;
    private final JwtProperties jwtProperties;

    public AuthController(AuthenticateUserUseCase authenticateUser,
                          JwtProperties jwtProperties) {
        this.authenticateUser = authenticateUser;
        this.jwtProperties = jwtProperties;
    }

    @Operation(
            summary = "Autenticar usuário",
            description = "Valida login e senha e retorna um token JWT para uso nas demais chamadas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticação bem-sucedida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "Sucesso",
                                    value = """
                                            {
                                              "authenticated": true,
                                              "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                              "role": "CUSTOMER",
                                              "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzZmE4NWY2NC01NzE3LTQ1NjItYjNmYy0yYzk2M2Y2NmFmYTYifQ.abc",
                                              "expiresIn": 3600
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos (campo obrigatório ausente)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Login em branco",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/validation",
                                              "title": "Erro de validação",
                                              "status": 400,
                                              "errors": [{"field": "login", "message": "não deve estar em branco"}],
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Senha errada",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/unauthorized",
                                              "title": "Não autenticado",
                                              "status": 401,
                                              "detail": "Credenciais inválidas.",
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            )
    })
    @SecurityRequirements({})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = authenticateUser.authenticate(
                request.login(), request.password());

        LoginResponse response = new LoginResponse(
                result.authenticated(),
                result.userId(),
                result.role(),
                result.token(),
                jwtProperties.expirationSeconds());

        return ResponseEntity.ok(response);
    }
}
