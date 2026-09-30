package com.fcv.citas.application.catalogs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CatalogAdminService {
    private final JdbcTemplate jdbc;

    public CatalogAdminService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> eps() { return jdbc.queryForList("SELECT id,code,name,is_active AS active FROM eps ORDER BY name"); }

    @Transactional
    public long createEps(String code, String name) {
        jdbc.update("INSERT INTO eps(code,name) VALUES (?,?)", normalize(code), normalize(name));
        return jdbc.queryForObject("SELECT id FROM eps WHERE code=?", Long.class, normalize(code));
    }

    public void updateEps(long id, String code, String name) {
        jdbc.update("UPDATE eps SET code=?,name=? WHERE id=?", normalize(code), normalize(name), id);
    }

    public void activateEps(long id, boolean active) { jdbc.update("UPDATE eps SET is_active=? WHERE id=?", active, id); }

    public List<Map<String, Object>> plans(long epsId) {
        return jdbc.queryForList("SELECT p.id,p.eps_id AS epsId,p.regime_id AS regimeId,r.code AS regimeCode,p.code,p.name,p.is_active AS active " +
                "FROM eps_plans p JOIN insurance_regimes r ON r.id=p.regime_id WHERE p.eps_id=? ORDER BY p.name", epsId);
    }

    public List<Map<String, Object>> regimes() { return jdbc.queryForList("SELECT id,code,name FROM insurance_regimes ORDER BY name"); }

    @Transactional
    public long createPlan(long epsId, long regimeId, String code, String name) {
        jdbc.update("INSERT INTO eps_plans(eps_id,regime_id,code,name) VALUES (?,?,?,?)", epsId, regimeId, normalize(code), normalize(name));
        return jdbc.queryForObject("SELECT id FROM eps_plans WHERE eps_id=? AND regime_id=? AND code=?", Long.class, epsId, regimeId, normalize(code));
    }

    public void updatePlan(long id, long regimeId, String code, String name) {
        jdbc.update("UPDATE eps_plans SET regime_id=?,code=?,name=? WHERE id=?", regimeId, normalize(code), normalize(name), id);
    }

    public void activatePlan(long id, boolean active) {
        jdbc.update("UPDATE eps_plans SET is_active=? WHERE id=? AND (?=FALSE OR EXISTS (SELECT 1 FROM eps e WHERE e.id=eps_plans.eps_id AND e.is_active=TRUE))", active, id, active);
    }

    public List<Map<String, Object>> specialties() {
        return jdbc.queryForList("SELECT id,code,name,appointment_duration_minutes AS durationMinutes,is_general AS general,requires_admin_approval AS requiresAdminApproval,is_active AS active FROM specialties ORDER BY name");
    }

    @Transactional
    public long createSpecialty(String code, String name, int duration, boolean general, boolean requiresApproval) {
        validateSpecialty(duration, general, requiresApproval);
        jdbc.update("INSERT INTO specialties(code,name,appointment_duration_minutes,is_general,requires_admin_approval) VALUES (?,?,?,?,?)",
                normalize(code), normalize(name), duration, general, requiresApproval);
        return jdbc.queryForObject("SELECT id FROM specialties WHERE code=?", Long.class, normalize(code));
    }

    public void updateSpecialty(long id, String code, String name, int duration, boolean general, boolean requiresApproval) {
        validateSpecialty(duration, general, requiresApproval);
        jdbc.update("UPDATE specialties SET code=?,name=?,appointment_duration_minutes=?,is_general=?,requires_admin_approval=? WHERE id=?",
                normalize(code), normalize(name), duration, general, requiresApproval, id);
    }

    public void activateSpecialty(long id, boolean active) { jdbc.update("UPDATE specialties SET is_active=? WHERE id=?", active, id); }

    private void validateSpecialty(int duration, boolean general, boolean requiresApproval) {
        if (duration != 30 && duration != 60) throw new IllegalArgumentException("La duración debe ser de 30 o 60 minutos");
        if (general && requiresApproval) throw new IllegalArgumentException("Medicina general no requiere aprobación administrativa");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("El código y el nombre son obligatorios");
        return value.trim();
    }
}