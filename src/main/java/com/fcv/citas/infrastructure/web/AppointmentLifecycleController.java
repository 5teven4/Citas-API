package com.fcv.citas.infrastructure.web;

import com.fcv.citas.application.appointments.AppointmentLifecycleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AppointmentLifecycleController {
    private final AppointmentLifecycleService lifecycle;

    public AppointmentLifecycleController(AppointmentLifecycleService lifecycle) { this.lifecycle = lifecycle; }

    @GetMapping("/appointments/me")
    @PreAuthorize("hasRole('USER')")
    public List<Map<String, Object>> myAppointments(@AuthenticationPrincipal String email,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return lifecycle.myAppointments(email, status, from, to);
    }

    @PostMapping("/appointments/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void cancel(@AuthenticationPrincipal String email, @PathVariable long id) { lifecycle.cancel(email, id); }

    @PostMapping("/appointments/{id}/reschedule-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public Id requestReschedule(@AuthenticationPrincipal String email, @PathVariable long id, @Valid @RequestBody Reschedule request) {
        return new Id(lifecycle.requestReschedule(email, id, request.locationId(), request.start()));
    }

    @GetMapping("/appointments/me/{id}/history")
    @PreAuthorize("hasRole('USER')")
    public List<Map<String, Object>> history(@AuthenticationPrincipal String email, @PathVariable long id) { return lifecycle.myHistory(email, id); }

    @GetMapping("/professionals/me/appointments")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public List<Map<String, Object>> agenda(@AuthenticationPrincipal String email,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                            @RequestParam(required = false) Long locationId) {
        return lifecycle.professionalAgenda(email, from, to, locationId);
    }

    @PatchMapping("/professionals/me/appointments/{id}/completion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public void close(@AuthenticationPrincipal String email, @PathVariable long id, @Valid @RequestBody Completion request) {
        lifecycle.closeAppointment(email, id, request.status());
    }

    @GetMapping("/admin/reschedule-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> pendingReschedules() { return lifecycle.pendingReschedules(); }

    @PatchMapping("/admin/reschedule-requests/{id}/decision")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void decideReschedule(@AuthenticationPrincipal String email, @PathVariable long id, @Valid @RequestBody Decision request) {
        lifecycle.decideReschedule(email, id, request.approve(), request.reason());
    }

    record Id(long id) { }
    record Reschedule(@NotNull Long locationId, @NotNull LocalDateTime start) { }
    record Completion(@NotBlank String status) { }
    record Decision(boolean approve, String reason) { }
}