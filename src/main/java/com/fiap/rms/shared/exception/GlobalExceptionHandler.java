package com.fiap.rms.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Catch-all handler for framework exceptions.
 * Must run AFTER all domain-specific @ControllerAdvice beans (DomainExceptionHandler).
 * The @Order(LOWEST_PRECEDENCE) guarantees this regardless of classpath scan order in the packaged JAR.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_BASE = "https://api.techchallenge.com";

    private record ValidationError(String field, String message) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handle(MethodArgumentNotValidException ex,
                                                HttpServletRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ValidationError(f.getField(), String.valueOf(f.getDefaultMessage())))
                .toList();

        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST,
                TYPE_BASE + "/errors/validation",
                "Dados inválidos",
                "Um ou mais campos estão inválidos.",
                request);
        problem.setProperty("errors", errors);

        return response(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handle(ConstraintViolationException ex,
                                                HttpServletRequest request) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(v -> new ValidationError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST,
                TYPE_BASE + "/errors/validation",
                "Dados inválidos",
                "Um ou mais campos estão inválidos.",
                request);
        problem.setProperty("errors", errors);

        return response(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handle(MissingServletRequestParameterException ex,
                                                HttpServletRequest request) {
        List<ValidationError> errors = List.of(
                new ValidationError(ex.getParameterName(), "parâmetro obrigatório ausente"));

        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST,
                TYPE_BASE + "/errors/validation",
                "Parâmetro obrigatório ausente",
                "Um parâmetro obrigatório está ausente.",
                request);
        problem.setProperty("errors", errors);

        return response(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handle(NotFoundException ex,
                                                HttpServletRequest request) {
        ProblemDetail problem = buildProblem(HttpStatus.NOT_FOUND,
                TYPE_BASE + "/errors/not-found",
                "Recurso não encontrado",
                ex.getMessage(),
                request);

        return response(HttpStatus.NOT_FOUND, problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handle(DataIntegrityViolationException ex,
                                                HttpServletRequest request) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        String normalized = message == null ? "" : message.toLowerCase();

        if (normalized.contains("idx_users_email") || normalized.contains("(email)")) {
            ProblemDetail problem = buildProblem(HttpStatus.CONFLICT,
                    TYPE_BASE + "/errors/email-conflict",
                    "E-mail já cadastrado",
                    "Já existe um usuário com o e-mail informado.",
                    request);
            return response(HttpStatus.CONFLICT, problem);
        }
        if (normalized.contains("idx_users_login") || normalized.contains("(login)")) {
            ProblemDetail problem = buildProblem(HttpStatus.CONFLICT,
                    TYPE_BASE + "/errors/login-conflict",
                    "Login já cadastrado",
                    "Já existe um usuário com o login informado.",
                    request);
            return response(HttpStatus.CONFLICT, problem);
        }

        ProblemDetail genericConflict = buildProblem(HttpStatus.CONFLICT,
                TYPE_BASE + "/errors/conflict",
                "Conflito de integridade",
                "Violação de integridade de dados.",
                request);
        return response(HttpStatus.CONFLICT, genericConflict);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handle(Exception ex,
                                                HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);

        ProblemDetail problem = buildProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                TYPE_BASE + "/errors/internal",
                "Erro interno",
                "Erro interno. Tente novamente mais tarde.",
                request);

        return response(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    }

    private static ProblemDetail buildProblem(HttpStatus status, String type,
                                              String title, String detail,
                                              HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    private static ResponseEntity<ProblemDetail> response(HttpStatus status, ProblemDetail body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
