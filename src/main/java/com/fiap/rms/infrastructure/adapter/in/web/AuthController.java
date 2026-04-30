package com.fiap.rms.infrastructure.adapter.in.web;

import com.fiap.rms.application.port.in.AuthenticateUserUseCase;
import com.fiap.rms.application.usecase.AuthenticationResult;
import com.fiap.rms.infrastructure.adapter.in.web.dto.LoginRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.LoginResponse;
import com.fiap.rms.infrastructure.config.JwtProperties;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
