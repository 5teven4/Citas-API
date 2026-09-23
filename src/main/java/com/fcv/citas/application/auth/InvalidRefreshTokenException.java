package com.fcv.citas.application.auth;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() { super("Refresh token inválido o revocado"); }
}
