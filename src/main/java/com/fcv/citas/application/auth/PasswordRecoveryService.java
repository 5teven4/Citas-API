package com.fcv.citas.application.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Service
public class PasswordRecoveryService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final boolean exposeDevelopmentToken;

    public PasswordRecoveryService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder,
                                   @Value("${app.security.password-reset.expose-token:false}") boolean exposeDevelopmentToken) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.exposeDevelopmentToken = exposeDevelopmentToken;
    }

    @Transactional
    public Map<String, Object> request(String email) {
        var users = jdbc.query("SELECT id FROM users WHERE email=? AND is_active=TRUE", (rs, row) -> rs.getLong(1), email.trim().toLowerCase());
        if (users.isEmpty()) return Map.of("message", "Si la cuenta existe, se generó una solicitud de recuperación.");
        String token = newToken();
        long userId = users.getFirst();
        jdbc.update("UPDATE password_reset_tokens SET used_at=CURRENT_TIMESTAMP WHERE user_id=? AND used_at IS NULL", userId);
        jdbc.update("INSERT INTO password_reset_tokens(user_id,token_hash,expires_at) VALUES (?,?,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 15 MINUTE))", userId, hash(token));
        if (exposeDevelopmentToken) return Map.of("message", "Token de recuperación generado para desarrollo.", "developmentToken", token);
        return Map.of("message", "Si la cuenta existe, se generó una solicitud de recuperación.");
    }

    @Transactional
    public void reset(String token, String newPassword) {
        String tokenHash = hash(token);
        Long userId = jdbc.query("SELECT user_id FROM password_reset_tokens WHERE token_hash=? AND used_at IS NULL AND expires_at>CURRENT_TIMESTAMP FOR UPDATE",
                rs -> rs.next() ? rs.getLong(1) : null, tokenHash);
        if (userId == null) throw new IllegalArgumentException("El token es inválido, vencido o ya fue utilizado");
        jdbc.update("UPDATE users SET password_hash=? WHERE id=?", passwordEncoder.encode(newPassword), userId);
        jdbc.update("UPDATE password_reset_tokens SET used_at=CURRENT_TIMESTAMP WHERE token_hash=?", tokenHash);
        jdbc.update("UPDATE refresh_tokens SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE user_id=?", userId);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }
}