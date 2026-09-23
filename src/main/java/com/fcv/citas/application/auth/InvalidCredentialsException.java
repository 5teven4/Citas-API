package com.fcv.citas.application.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Credenciales inválidas"); }
}
