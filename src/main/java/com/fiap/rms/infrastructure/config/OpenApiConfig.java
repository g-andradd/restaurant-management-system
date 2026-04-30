package com.fiap.rms.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customOpenAPI() {
        Schema<?> problemDetail = new ObjectSchema()
                .addProperty("type", new Schema<String>().example("https://api.techchallenge.com/errors/unauthorized"))
                .addProperty("title", new Schema<String>().example("Não autenticado"))
                .addProperty("status", new Schema<Integer>().example(401))
                .addProperty("detail", new Schema<String>().example("Token ausente, inválido ou expirado."))
                .addProperty("instance", new Schema<String>().example("/api/v1/users/123"))
                .addProperty("timestamp", new Schema<String>().example("2026-04-26T12:34:56Z"));

        Schema<?> validationProblemDetail = new ObjectSchema()
                .allOf(List.of(new Schema<>().$ref("#/components/schemas/ProblemDetail")))
                .addProperty("errors", new ArraySchema().items(
                        new ObjectSchema()
                                .addProperty("field", new Schema<String>().example("email"))
                                .addProperty("message", new Schema<String>().example("deve ser um e-mail válido"))));

        return new OpenAPI()
                .info(new Info()
                        .title("Tech Challenge — User Management API")
                        .version("1.0.0")
                        .description("API para gestão de usuários com autenticação JWT e endpoints protegidos.")
                        .contact(new Contact().name("FIAP Student").url("https://www.fiap.com.br/")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://{hom-host}").description("Homologation placeholder")))
                .components(new Components()
                        .addSchemas("ProblemDetail", problemDetail)
                        .addSchemas("ValidationProblemDetail", validationProblemDetail)
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
