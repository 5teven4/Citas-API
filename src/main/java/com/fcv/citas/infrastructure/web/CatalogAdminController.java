package com.fcv.citas.infrastructure.web;

import com.fcv.citas.application.catalogs.CatalogAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/catalogs")
@PreAuthorize("hasRole('ADMIN')")
public class CatalogAdminController {
    private final CatalogAdminService catalogs;

    public CatalogAdminController(CatalogAdminService catalogs) { this.catalogs = catalogs; }

    @GetMapping("/eps") public List<Map<String, Object>> eps() { return catalogs.eps(); }
    @PostMapping("/eps") @ResponseStatus(HttpStatus.CREATED)
    public Id createEps(@Valid @RequestBody Eps request) { return new Id(catalogs.createEps(request.code(), request.name())); }
    @PutMapping("/eps/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEps(@PathVariable long id, @Valid @RequestBody Eps request) { catalogs.updateEps(id, request.code(), request.name()); }
    @PatchMapping("/eps/{id}/active") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateEps(@PathVariable long id, @RequestBody Active request) { catalogs.activateEps(id, request.active()); }

    @GetMapping("/regimes") public List<Map<String, Object>> regimes() { return catalogs.regimes(); }
    @GetMapping("/eps/{epsId}/plans") public List<Map<String, Object>> plans(@PathVariable long epsId) { return catalogs.plans(epsId); }
    @PostMapping("/eps/{epsId}/plans") @ResponseStatus(HttpStatus.CREATED)
    public Id createPlan(@PathVariable long epsId, @Valid @RequestBody Plan request) { return new Id(catalogs.createPlan(epsId, request.regimeId(), request.code(), request.name())); }
    @PutMapping("/plans/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePlan(@PathVariable long id, @Valid @RequestBody Plan request) { catalogs.updatePlan(id, request.regimeId(), request.code(), request.name()); }
    @PatchMapping("/plans/{id}/active") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activatePlan(@PathVariable long id, @RequestBody Active request) { catalogs.activatePlan(id, request.active()); }

    @GetMapping("/specialties") public List<Map<String, Object>> specialties() { return catalogs.specialties(); }
    @PostMapping("/specialties") @ResponseStatus(HttpStatus.CREATED)
    public Id createSpecialty(@Valid @RequestBody Specialty request) { return new Id(catalogs.createSpecialty(request.code(), request.name(), request.durationMinutes(), request.general(), request.requiresAdminApproval())); }
    @PutMapping("/specialties/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSpecialty(@PathVariable long id, @Valid @RequestBody Specialty request) { catalogs.updateSpecialty(id, request.code(), request.name(), request.durationMinutes(), request.general(), request.requiresAdminApproval()); }
    @PatchMapping("/specialties/{id}/active") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateSpecialty(@PathVariable long id, @RequestBody Active request) { catalogs.activateSpecialty(id, request.active()); }

    record Id(long id) { }
    record Eps(@NotBlank String code, @NotBlank String name) { }
    record Plan(@NotNull Long regimeId, @NotBlank String code, @NotBlank String name) { }
    record Specialty(@NotBlank String code, @NotBlank String name, @Min(30) @Max(60) int durationMinutes, boolean general, boolean requiresAdminApproval) { }
    record Active(boolean active) { }
}