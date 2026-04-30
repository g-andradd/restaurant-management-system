package com.fiap.rms.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtSecretValidator {

    private static final String DEFAULT_SECRET =
            "change-me-in-prod-this-must-be-32-bytes-min";

    private final JwtProperties jwtProperties;
    private final Environment env;

    public JwtSecretValidator(JwtProperties jwtProperties, Environment env) {
        this.jwtProperties = jwtProperties;
        this.env = env;
    }

    @PostConstruct
    void validate() {
        String secret = jwtProperties.secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret must be at least 32 bytes (UTF-8 encoded)");
        }
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
        if (isProd && DEFAULT_SECRET.equals(secret)) {
            throw new IllegalStateException(
                    "security.jwt.secret must not be the default placeholder in the prod profile");
        }
    }
}
