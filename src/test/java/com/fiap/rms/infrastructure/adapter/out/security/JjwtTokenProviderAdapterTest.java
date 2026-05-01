package com.fiap.rms.infrastructure.adapter.out.security;

import com.fiap.rms.application.usecase.TokenPayload;
import com.fiap.rms.domain.model.Role;
import com.fiap.rms.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JjwtTokenProviderAdapterTest {

    private static final String SECRET = "test-secret-must-be-at-least-32-bytes-long-aaaa";
    private static final String OTHER_SECRET = "other-secret-must-be-at-least-32-bytes-long-bbbb";

    private JjwtTokenProviderAdapter adapter() {
        return new JjwtTokenProviderAdapter(new JwtProperties(SECRET, 3600));
    }

    private SecretKey buildKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void roundTrip_generateThenParse_returnsCorrectClaims() {
        UUID userId = UUID.randomUUID();
        Role role = Role.RESTAURANT_OWNER;

        String token = adapter().generateToken(userId, role);
        Optional<TokenPayload> payload = adapter().parseToken(token);

        assertThat(payload).isPresent();
        assertThat(payload.get().userId()).isEqualTo(userId);
        assertThat(payload.get().role()).isEqualTo(role);
        assertThat(payload.get().expiresAt()).isInTheFuture();
    }

    @Test
    void parseToken_expiredToken_returnsEmpty() {
        // Build an expired JWT directly so we don't need to advance the clock
        String expiredToken = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("role", Role.CUSTOMER.name())
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(buildKey(SECRET), Jwts.SIG.HS256)
                .compact();

        Optional<TokenPayload> result = adapter().parseToken(expiredToken);

        assertThat(result).isEmpty();
    }

    @Test
    void parseToken_tokenSignedWithDifferentSecret_returnsEmpty() {
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("role", Role.CUSTOMER.name())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(buildKey(OTHER_SECRET), Jwts.SIG.HS256)
                .compact();

        Optional<TokenPayload> result = adapter().parseToken(token);

        assertThat(result).isEmpty();
    }

    @Test
    void parseToken_malformedToken_returnsEmpty() {
        Optional<TokenPayload> result = adapter().parseToken("not-a-jwt");

        assertThat(result).isEmpty();
    }

    @Test
    void parseToken_emptyToken_returnsEmpty() {
        Optional<TokenPayload> result = adapter().parseToken("");

        assertThat(result).isEmpty();
    }

    @Test
    void parseToken_nullToken_returnsEmpty() {
        Optional<TokenPayload> result = adapter().parseToken(null);

        assertThat(result).isEmpty();
    }
}
