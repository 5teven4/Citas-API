package com.fcv.citas.application.auth;

import java.time.Instant;

public record AuthTokensResponse(String accessToken, Instant accessExpiresAt, String refreshToken, Instant refreshExpiresAt) { }
