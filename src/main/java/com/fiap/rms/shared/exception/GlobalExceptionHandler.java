package com.fiap.rms.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_BASE = "https://api.techchallenge.com";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handle(MethodArgumentNotValidException ex,
                                                HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.of("field", f.getField(), "message", String.valueOf(f.getDefaultMessage())))
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
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(v -> Map.of("field", v.getPropertyPath().toString(),
                                 "message", v.getMessage()))
                .toList();

        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST,
                TYPE_BASE + "/errors/validation",
                "Dados inválidos",
                "Um ou mais campos estão inválidos.",
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
