package com.fiap.rms.infrastructure.adapter.in.web;

import com.fiap.rms.application.port.in.ChangePasswordUseCase;
import com.fiap.rms.application.port.in.DeleteUserUseCase;
import com.fiap.rms.application.port.in.FindUserByIdUseCase;
import com.fiap.rms.application.port.in.RegisterUserUseCase;
import com.fiap.rms.application.port.in.SearchUsersByNameUseCase;
import com.fiap.rms.application.port.in.UpdateUserUseCase;
import com.fiap.rms.domain.model.User;
import com.fiap.rms.infrastructure.adapter.in.web.dto.ChangePasswordRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserUseCase registerUser;
    private final FindUserByIdUseCase findUserById;
    private final SearchUsersByNameUseCase searchUsersByName;
    private final UpdateUserUseCase updateUser;
    private final ChangePasswordUseCase changePassword;
    private final DeleteUserUseCase deleteUser;
    private final UserWebMapper mapper;

    public UserController(RegisterUserUseCase registerUser,
                          FindUserByIdUseCase findUserById,
                          SearchUsersByNameUseCase searchUsersByName,
                          UpdateUserUseCase updateUser,
                          ChangePasswordUseCase changePassword,
                          DeleteUserUseCase deleteUser,
                          UserWebMapper mapper) {
        this.registerUser = registerUser;
        this.findUserById = findUserById;
        this.searchUsersByName = searchUsersByName;
        this.updateUser = updateUser;
        this.changePassword = changePassword;
        this.deleteUser = deleteUser;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = registerUser.register(mapper.toCommand(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(findUserById.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> searchByName(
            @RequestParam(required = true) String name) {
        List<UserResponse> results = searchUsersByName.searchByName(name).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateUserRequest request) {
        User user = updateUser.update(id, mapper.toCommand(request));
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        changePassword.changePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUser.delete(id);
        return ResponseEntity.noContent().build();
    }
}
