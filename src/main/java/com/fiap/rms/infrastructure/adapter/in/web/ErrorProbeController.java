package com.fiap.rms.infrastructure.adapter.in.web;

import com.fiap.rms.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/probe")
public class ErrorProbeController {

    @PostMapping("/validation")
    void validation(@Valid @RequestBody ProbeRequest body) {
    }

    @GetMapping("/not-found")
    void notFound() {
        throw new NotFoundException("recurso de teste não encontrado");
    }

    @GetMapping("/internal-error")
    void internalError() {
        throw new RuntimeException("erro inesperado de teste");
    }

    record ProbeRequest(@NotBlank String name) {
    }
}
