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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

@Tag(name = "Usuários", description = "Cadastro e gerenciamento de usuários")
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

    // ── POST /api/v1/users ──────────────────────────────────────────────────

    @Operation(
            summary = "Cadastrar usuário",
            description = "Cria um novo usuário no sistema. Não requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário criado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Criado",
                                    value = """
                                            {
                                              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                              "name": "Maria da Silva",
                                              "email": "maria@exemplo.com.br",
                                              "login": "mariasilva",
                                              "role": "CUSTOMER",
                                              "address": {
                                                "street": "Rua das Flores",
                                                "number": "42",
                                                "city": "São Paulo",
                                                "zipCode": "01310-100"
                                              },
                                              "createdAt": "2026-04-30T10:00:00Z",
                                              "updatedAt": "2026-04-30T10:00:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Nome em branco",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/validation",
                                              "title": "Erro de validação",
                                              "status": 400,
                                              "errors": [{"field": "name", "message": "não deve estar em branco"}],
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail ou login já cadastrado no sistema",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Login duplicado",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/login-conflict",
                                              "title": "Conflito de login",
                                              "status": 409,
                                              "detail": "Login 'mariasilva' já está em uso.",
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            )
    })
    @SecurityRequirements({})
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = registerUser.register(mapper.toCommand(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(user));
    }

    // ── GET /api/v1/users/{id} ──────────────────────────────────────────────

    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os dados de um usuário a partir do seu identificador único."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Encontrado",
                                    value = """
                                            {
                                              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                              "name": "Maria da Silva",
                                              "email": "maria@exemplo.com.br",
                                              "login": "mariasilva",
                                              "role": "CUSTOMER",
                                              "address": {
                                                "street": "Rua das Flores",
                                                "number": "42",
                                                "city": "São Paulo",
                                                "zipCode": "01310-100"
                                              },
                                              "createdAt": "2026-04-30T10:00:00Z",
                                              "updatedAt": "2026-04-30T10:00:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Não encontrado",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/user-not-found",
                                              "title": "Não encontrado",
                                              "status": 404,
                                              "detail": "Usuário não encontrado.",
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(
            @Parameter(description = "UUID do usuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(findUserById.findById(id)));
    }

    // ── GET /api/v1/users?name=... ──────────────────────────────────────────

    @Operation(
            summary = "Pesquisar usuários por nome",
            description = "Retorna a lista de usuários cujo nome contém o termo informado (case-insensitive)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários (pode ser vazia)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)),
                            examples = @ExampleObject(
                                    name = "Com resultados",
                                    value = """
                                            [
                                              {
                                                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                "name": "Maria da Silva",
                                                "email": "maria@exemplo.com.br",
                                                "login": "mariasilva",
                                                "role": "CUSTOMER",
                                                "address": {
                                                  "street": "Rua das Flores",
                                                  "number": "42",
                                                  "city": "São Paulo",
                                                  "zipCode": "01310-100"
                                                },
                                                "createdAt": "2026-04-30T10:00:00Z",
                                                "updatedAt": "2026-04-30T10:00:00Z"
                                              }
                                            ]"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetro 'name' ausente",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Parâmetro ausente",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/validation",
                                              "title": "Erro de validação",
                                              "status": 400,
                                              "errors": [{"field": "name", "message": "parâmetro obrigatório ausente"}],
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> searchByName(
            @Parameter(description = "Fragmento do nome a pesquisar", example = "Maria")
            @RequestParam(required = true) String name) {
        List<UserResponse> results = searchUsersByName.searchByName(name).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(results);
    }

    // ── PUT /api/v1/users/{id} ──────────────────────────────────────────────

    @Operation(
            summary = "Atualizar usuário",
            description = "Substitui os dados cadastrais de um usuário existente (exceto senha e role)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Atualizado",
                                    value = """
                                            {
                                              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                              "name": "Maria da Silva Souza",
                                              "email": "maria.souza@exemplo.com.br",
                                              "login": "mariasouza",
                                              "role": "CUSTOMER",
                                              "address": {
                                                "street": "Avenida Paulista",
                                                "number": "1000",
                                                "city": "São Paulo",
                                                "zipCode": "01310-100"
                                              },
                                              "createdAt": "2026-04-30T10:00:00Z",
                                              "updatedAt": "2026-04-30T15:00:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Nome em branco",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/validation",
                                              "title": "Erro de validação",
                                              "status": 400,
                                              "errors": [{"field": "name", "message": "não deve estar em branco"}],
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Não encontrado",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/user-not-found",
                                              "title": "Não encontrado",
                                              "status": 404,
                                              "detail": "Usuário não encontrado.",
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail ou login já cadastrado por outro usuário",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Login duplicado",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/login-conflict",
                                              "title": "Conflito de login",
                                              "status": 409,
                                              "detail": "Login 'mariasilva' já está em uso.",
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @Parameter(description = "UUID do usuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = updateUser.update(id, mapper.toCommand(request));
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    // ── PATCH /api/v1/users/{id}/password ──────────────────────────────────

    @Operation(
            summary = "Alterar senha",
            description = "Substitui a senha de um usuário. A nova senha deve ter ao menos 8 caracteres, "
                    + "uma letra maiúscula e um dígito."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Senha alterada com sucesso (sem corpo)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Senha não atende aos critérios mínimos",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Senha fraca",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/validation",
                                              "title": "Erro de validação",
                                              "status": 400,
                                              "errors": [{"field": "newPassword", "message": "must contain at least one uppercase letter and one digit"}],
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Não encontrado",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/user-not-found",
                                              "title": "Não encontrado",
                                              "status": 404,
                                              "detail": "Usuário não encontrado.",
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            )
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "UUID do usuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        changePassword.changePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    // ── DELETE /api/v1/users/{id} ───────────────────────────────────────────

    @Operation(
            summary = "Remover usuário",
            description = "Exclui permanentemente um usuário do sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário removido com sucesso (sem corpo)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(
                                    name = "Não encontrado",
                                    value = """
                                            {
                                              "type": "https://api.techchallenge.com/errors/user-not-found",
                                              "title": "Não encontrado",
                                              "status": 404,
                                              "detail": "Usuário não encontrado.",
                                              "timestamp": "2026-04-30T12:00:00Z"
                                            }"""
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID do usuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        deleteUser.delete(id);
        return ResponseEntity.noContent().build();
    }
}
