package com.fcv.citas.infrastructure.web;

import com.fcv.citas.application.auth.PasswordRecoveryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class PasswordRecoveryController {
    private final PasswordRecoveryService recovery;

    public PasswordRecoveryController(PasswordRecoveryService recovery) { this.recovery = recovery; }

    @PostMapping("/password-recovery")
    public Map<String, Object> request(@Valid @RequestBody RecoveryRequest request) { return recovery.request(request.email()); }

    @PostMapping("/password-reset")
    public Map<String, String> reset(@Valid @RequestBody ResetPassword request) {
        recovery.reset(request.token(), request.newPassword());
        return Map.of("message", "La contraseña fue actualizada; inicia sesión de nuevo.");
    }

    record RecoveryRequest(@NotBlank @Email String email) { }
    record ResetPassword(@NotBlank String token, @NotBlank @Size(min = 8, max = 72) String newPassword) { }
}