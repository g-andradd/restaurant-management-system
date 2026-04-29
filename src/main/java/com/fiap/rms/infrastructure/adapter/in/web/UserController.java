package com.fiap.rms.infrastructure.adapter.in.web;

import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.domain.model.User;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserUseCase registerUser;
    private final UserWebMapper mapper;

    public UserController(RegisterUserUseCase registerUser, UserWebMapper mapper) {
        this.registerUser = registerUser;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = registerUser.register(mapper.toCommand(request));
        UserResponse body = mapper.toResponse(user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }
}
