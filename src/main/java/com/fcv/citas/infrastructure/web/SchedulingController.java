package com.fcv.citas.infrastructure.web;

import com.fcv.citas.application.appointments.SchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SchedulingController {
    private final SchedulingService scheduling;
    public SchedulingController(SchedulingService scheduling) { this.scheduling = scheduling; }
    @PostMapping("/admin/professionals") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
    public Id createProfessional(@Valid @RequestBody Professional request) { return new Id(scheduling.createProfessional(request.userId(), request.code(), request.license(), request.specialties(), request.locations())); }
    @PostMapping("/professionals/me/availability-blocks") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('PROFESSIONAL')")
    public Id block(@AuthenticationPrincipal String email, @Valid @RequestBody Block request) { return new Id(scheduling.createBlock(email, request.locationId(), request.date(), request.start(), request.end())); }
    @GetMapping("/availability") @PreAuthorize("hasAnyRole('USER','ADMIN','PROFESSIONAL')")
    public List<LocalDateTime> availability(@RequestParam long professionalId, @RequestParam long specialtyId, @RequestParam long locationId, @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date) { return scheduling.availability(professionalId, specialtyId, locationId, date); }
    @GetMapping("/catalogs/specialties") @PreAuthorize("isAuthenticated()") public List<Map<String,Object>> specialties() { return scheduling.specialties(); }
    @GetMapping("/catalogs/locations") @PreAuthorize("isAuthenticated()") public List<Map<String,Object>> locations() { return scheduling.locations(); }
    @GetMapping("/catalogs/professionals") @PreAuthorize("isAuthenticated()") public List<Map<String,Object>> professionals(@RequestParam long specialtyId, @RequestParam long locationId) { return scheduling.professionals(specialtyId, locationId); }
    @GetMapping("/admin/appointments/requests") @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> requestedAppointments() { return scheduling.requestedAppointments(); }
    @PostMapping("/appointments") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('USER')")
    public SchedulingService.AppointmentResult reserve(@AuthenticationPrincipal String email, @Valid @RequestBody Appointment request) { return scheduling.reserve(email, request.professionalId(), request.specialtyId(), request.locationId(), request.start()); }
    @PatchMapping("/admin/appointments/{id}/decision") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')")
    public void decide(@AuthenticationPrincipal String email, @PathVariable long id, @RequestBody Decision request) { scheduling.decide(email, id, request.approve(), request.reason()); }
    record Id(long id) { }
    record Professional(@NotNull Long userId, @NotBlank String code, @NotBlank String license, @NotEmpty List<Long> specialties, @NotEmpty List<Long> locations) { }
    record Block(@NotNull Long locationId, @NotNull LocalDate date, @NotNull LocalTime start, @NotNull LocalTime end) { }
    record Appointment(@NotNull Long professionalId, @NotNull Long specialtyId, @NotNull Long locationId, @NotNull LocalDateTime start) { }
    record Decision(boolean approve, String reason) { }
}
