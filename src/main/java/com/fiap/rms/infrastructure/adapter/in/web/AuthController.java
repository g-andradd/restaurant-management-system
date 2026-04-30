package com.fiap.rms.infrastructure.adapter.in.web;

import com.fiap.rms.application.port.in.AuthenticateUserUseCase;
import com.fiap.rms.application.usecase.AuthenticationResult;
import com.fiap.rms.infrastructure.adapter.in.web.dto.LoginRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.LoginResponse;
import com.fiap.rms.infrastructure.config.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticateUserUseCase authenticateUser;
    private final JwtProperties jwtProperties;
    public AuthController(AuthenticateUserUseCase authenticateUser, JwtProperties jwtProperties) { this.authenticateUser = authenticateUser; this.jwtProperties = jwtProperties; }

    @SecurityRequirements({})
    @Operation(summary = "Autenticar usuário", description = "Valida credenciais e retorna JWT Bearer token.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Login realizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetail")))})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = authenticateUser.authenticate(request.login(), request.password());
        LoginResponse response = new LoginResponse(result.authenticated(), result.userId(), result.role(), result.token(), jwtProperties.expirationSeconds());
        return ResponseEntity.ok(response);
    }
}
