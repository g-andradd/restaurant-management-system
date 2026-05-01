package com.fiap.rms.infrastructure.adapter.out.security;

import com.fiap.rms.application.port.out.JwtTokenProviderPort;
import com.fiap.rms.application.usecase.TokenPayload;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class JjwtTokenProviderAdapter implements JwtTokenProviderPort {

    private final JwtProperties jwtProperties;

    public JjwtTokenProviderAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generateToken(UUID userId, Role role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.expirationSeconds());

        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(buildKey(), Jwts.SIG.HS256)
                .compact();

        log.debug("Token issued for user {}", userId);
        return token;
    }

    @Override
    public Optional<TokenPayload> parseToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(buildKey())
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            UUID userId  = UUID.fromString(claims.getSubject());
            Role role    = Role.valueOf(claims.get("role", String.class));
            Instant exp  = claims.getExpiration().toInstant();

            return Optional.of(new TokenPayload(userId, role, exp));

        } catch (ExpiredJwtException | MalformedJwtException | SignatureException
                 | UnsupportedJwtException | IllegalArgumentException e) {
            log.debug("Token parsing failed: {}", e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private SecretKey buildKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
