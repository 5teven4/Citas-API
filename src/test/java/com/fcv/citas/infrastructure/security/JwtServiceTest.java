package com.fcv.citas.infrastructure.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private final JwtService service = new JwtService("a".repeat(32), "b".repeat(32), 15, 7);

    @Test
    void issuesAndValidatesAccessToken() {
        JwtService.GeneratedToken token = service.issueAccess(42L, "user@fcv.test", List.of("USER"));
        assertEquals(42L, service.accessUserId(token.value()));
        assertTrue(token.expiresAt().isAfter(java.time.Instant.now()));
    }

    @Test
    void rejectsRefreshTokenAtAccessBoundary() {
        JwtService.GeneratedToken token = service.issueRefresh(42L);
        assertThrows(JwtException.class, () -> service.accessUserId(token.value()));
    }
}
