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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
// constructor omitted
    private final RegisterUserUseCase registerUser; private final FindUserByIdUseCase findUserById; private final SearchUsersByNameUseCase searchUsersByName; private final UpdateUserUseCase updateUser; private final ChangePasswordUseCase changePassword; private final DeleteUserUseCase deleteUser; private final UserWebMapper mapper;
    public UserController(RegisterUserUseCase registerUser, FindUserByIdUseCase findUserById, SearchUsersByNameUseCase searchUsersByName, UpdateUserUseCase updateUser, ChangePasswordUseCase changePassword, DeleteUserUseCase deleteUser, UserWebMapper mapper) {this.registerUser=registerUser;this.findUserById=findUserById;this.searchUsersByName=searchUsersByName;this.updateUser=updateUser;this.changePassword=changePassword;this.deleteUser=deleteUser;this.mapper=mapper;}

    @SecurityRequirements({})
    @Operation(summary = "Registrar usuário", description = "Cria um novo usuário no sistema.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"))),
            @ApiResponse(responseCode = "409", description = "E-mail já existe", content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetail")))})
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = registerUser.register(mapper.toCommand(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(user));
    }

    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados de um usuário pelo identificador.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetail")))})
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) { return ResponseEntity.ok(mapper.toResponse(findUserById.findById(id))); }

    @Operation(summary = "Pesquisar usuários por nome", description = "Busca usuários por trecho do nome (case-insensitive).")
    @ApiResponse(responseCode = "200", description = "Pesquisa realizada")
    @GetMapping
    public ResponseEntity<List<UserResponse>> searchByName(@RequestParam(required = true) String name) {
        List<UserResponse> results = searchUsersByName.searchByName(name).stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Atualizar usuário", description = "Atualiza dados cadastrais do usuário, exceto senha.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetail"))),
            @ApiResponse(responseCode = "409", description = "E-mail já existe", content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetail")))})
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        User user = updateUser.update(id, mapper.toCommand(request));
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Senha inválida", content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetail")))})
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id, @Valid @RequestBody ChangePasswordRequest request) { changePassword.changePassword(id, request.newPassword()); return ResponseEntity.noContent().build(); }

    @Operation(summary = "Excluir usuário", description = "Remove um usuário pelo identificador.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Usuário removido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetail")))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { deleteUser.delete(id); return ResponseEntity.noContent().build(); }
}
