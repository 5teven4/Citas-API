package com.fcv.citas.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessMinutes;
    private final long refreshDays;

    public JwtService(@Value("${app.security.jwt.access-secret}") String accessSecret,
                      @Value("${app.security.jwt.refresh-secret}") String refreshSecret,
                      @Value("${app.security.jwt.access-minutes}") long accessMinutes,
                      @Value("${app.security.jwt.refresh-days}") long refreshDays) {
        this.accessKey = keyFrom(accessSecret);
        this.refreshKey = keyFrom(refreshSecret);
        this.accessMinutes = accessMinutes;
        this.refreshDays = refreshDays;
    }

    public GeneratedToken issueAccess(Long userId, String email, Collection<String> roles) {
        Instant expiresAt = Instant.now().plus(accessMinutes, ChronoUnit.MINUTES);
        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .claim("token_type", "access")
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt))
                .signWith(accessKey)
                .compact();
        return new GeneratedToken(token, expiresAt);
    }

    public GeneratedToken issueRefresh(Long userId) {
        Instant expiresAt = Instant.now().plus(refreshDays, ChronoUnit.DAYS);
        String token = Jwts.builder()
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .claim("token_type", "refresh")
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt))
                .signWith(refreshKey)
                .compact();
        return new GeneratedToken(token, expiresAt);
    }

    public Long accessUserId(String token) { return userId(parse(token, accessKey, "access")); }
    public Long refreshUserId(String token) { return userId(parse(token, refreshKey, "refresh")); }

    private Claims parse(String token, SecretKey key, String expectedType) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (!expectedType.equals(claims.get("token_type", String.class))) {
            throw new UnsupportedJwtException("Tipo de token no permitido");
        }
        return claims;
    }

    private Long userId(Claims claims) {
        try { return Long.valueOf(claims.getSubject()); }
        catch (NumberFormatException exception) { throw new UnsupportedJwtException("Subject JWT inválido"); }
    }

    private SecretKey keyFrom(String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("El secreto JWT debe tener al menos 32 caracteres UTF-8");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record GeneratedToken(String value, Instant expiresAt) { }
}
