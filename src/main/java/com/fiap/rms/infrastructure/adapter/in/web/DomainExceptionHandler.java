package com.fiap.rms.infrastructure.adapter.in.web;

import com.fiap.rms.domain.exception.EmailAlreadyExistsException;
import com.fiap.rms.domain.exception.InvalidCredentialsException;
import com.fiap.rms.domain.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class DomainExceptionHandler {

    private static final String TYPE_BASE = "https://api.techchallenge.com";

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handle(EmailAlreadyExistsException ex,
                                                HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT,
                TYPE_BASE + "/errors/email-conflict",
                "E-mail já cadastrado",
                "Já existe um usuário com o e-mail informado.",
                request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handle(UserNotFoundException ex,
                                                HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND,
                TYPE_BASE + "/errors/user-not-found",
                "Usuário não encontrado",
                "Não existe usuário com o id informado.",
                request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handle(InvalidCredentialsException ex,
                                                HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED,
                TYPE_BASE + "/errors/unauthorized",
                "Não autenticado",
                "Credenciais inválidas.",
                request);
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String type,
                                                         String title, String detail,
                                                         HttpServletRequest request) {
        ProblemDetail body = ProblemDetail.forStatus(status);
        body.setType(URI.create(type));
        body.setTitle(title);
        body.setDetail(detail);
        body.setInstance(URI.create(request.getRequestURI()));
        body.setProperty("timestamp", Instant.now().toString());

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
