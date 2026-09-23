package com.fcv.citas.application.auth;

import com.fcv.citas.infrastructure.persistence.*;
import com.fcv.citas.infrastructure.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, RoleRepository roles, RefreshTokenRepository refreshTokens,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.roles = roles;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisteredUserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String documentType = request.documentType().trim().toUpperCase(Locale.ROOT);
        String documentNumber = request.documentNumber().trim();
        if (users.existsByEmail(email)) throw new DuplicateResourceException("El email ya está registrado");
        if (users.existsByDocumentTypeAndDocumentNumber(documentType, documentNumber)) {
            throw new DuplicateResourceException("El documento ya está registrado");
        }
        RoleEntity userRole = roles.findByCode("USER")
                .orElseThrow(() -> new IllegalStateException("Falta el rol USER en la base de datos"));
        UserEntity user = new UserEntity(request.firstName().trim(), request.lastName().trim(), documentType,
                documentNumber, email, normalizePhone(request.phone()), passwordEncoder.encode(request.password()));
        user.addRole(userRole);
        UserEntity saved = users.save(user);
        return new RegisteredUserResponse(saved.getId(), saved.getEmail(), roleCodes(saved));
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest request) {
        UserEntity user = users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .filter(UserEntity::isActive)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return issueTokens(user);
    }

    @Transactional
    public AuthTokensResponse refresh(RefreshRequest request) {
        Long tokenUserId;
        try { tokenUserId = jwtService.refreshUserId(request.refreshToken()); }
        catch (JwtException exception) { throw new InvalidRefreshTokenException(); }
        RefreshTokenEntity stored = refreshTokens.findByTokenHashAndRevokedAtIsNull(hash(request.refreshToken()))
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .filter(token -> token.getUser().getId().equals(tokenUserId))
                .orElseThrow(InvalidRefreshTokenException::new);
        stored.revoke();
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokens.findByTokenHashAndRevokedAtIsNull(hash(request.refreshToken())).ifPresent(RefreshTokenEntity::revoke);
    }

    private AuthTokensResponse issueTokens(UserEntity user) {
        JwtService.GeneratedToken access = jwtService.issueAccess(user.getId(), user.getEmail(), roleCodes(user));
        JwtService.GeneratedToken refresh = jwtService.issueRefresh(user.getId());
        refreshTokens.save(new RefreshTokenEntity(user, hash(refresh.value()), refresh.expiresAt()));
        return new AuthTokensResponse(access.value(), access.expiresAt(), refresh.value(), refresh.expiresAt());
    }

    private List<String> roleCodes(UserEntity user) {
        return user.getRoles().stream().map(RoleEntity::getCode).sorted().toList();
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }

    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 no disponible", exception); }
    }
}
