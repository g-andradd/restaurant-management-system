package com.fiap.rms.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tech Challenge — User Management API")
                        .version("1.0.0")
                        .description("Shared restaurant management backend — FIAP Tech Challenge Phase 1")
                        .contact(new Contact()
                                .name("FIAP Pós-Graduação em Arquitetura Java")
                                .email("tech-challenge@fiap.com.br")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desenvolvimento local"),
                        new Server().url("https://hom.techchallenge.example.com")
                                .description("Homologação")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSchemas("ProblemDetail", problemDetailSchema())
                        .addSchemas("ValidationProblemDetail", validationProblemDetailSchema()))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Schema<?> problemDetailSchema() {
        return new ObjectSchema()
                .description("Resposta de erro conforme RFC 7807")
                .addProperty("type", new StringSchema()
                        .example("https://api.techchallenge.com/errors/not-found"))
                .addProperty("title", new StringSchema()
                        .example("Não encontrado"))
                .addProperty("status", new IntegerSchema()
                        .example(404))
                .addProperty("detail", new StringSchema()
                        .example("Usuário não encontrado."))
                .addProperty("instance", new StringSchema()
                        .example("/api/v1/users/3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .addProperty("timestamp", new StringSchema()
                        .format("date-time")
                        .example("2026-04-30T12:00:00Z"));
    }

    private Schema<?> validationProblemDetailSchema() {
        Schema<?> fieldError = new ObjectSchema()
                .addProperty("field", new StringSchema().example("name"))
                .addProperty("message", new StringSchema().example("não deve estar em branco"));
        return new ObjectSchema()
                .description("Erro de validação de bean — RFC 7807 estendido com array de erros")
                .addProperty("type", new StringSchema()
                        .example("https://api.techchallenge.com/errors/validation"))
                .addProperty("title", new StringSchema()
                        .example("Erro de validação"))
                .addProperty("status", new IntegerSchema()
                        .example(400))
                .addProperty("errors", new ArraySchema().items(fieldError))
                .addProperty("timestamp", new StringSchema()
                        .format("date-time")
                        .example("2026-04-30T12:00:00Z"));
    }
}
