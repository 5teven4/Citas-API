package com.fcv.citas.application.appointments;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SchedulingService {
    private final JdbcTemplate jdbc;
    public SchedulingService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public long createProfessional(long userId, String code, String license, List<Long> specialtyIds, List<Long> locationIds) {
        if (specialtyIds.isEmpty() || locationIds.isEmpty()) throw new IllegalArgumentException("El profesional requiere especialidad y sede");
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO professionals(user_id,professional_code,license_number) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, userId); statement.setString(2, code.trim()); statement.setString(3, license.trim()); return statement;
        }, holder);
        long professionalId = holder.getKey().longValue();
        jdbc.update("INSERT IGNORE INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE code='PROFESSIONAL'", userId);
        specialtyIds.forEach(id -> jdbc.update("INSERT INTO professional_specialties(professional_id,specialty_id,is_primary) VALUES (?,?,?)", professionalId, id, id.equals(specialtyIds.getFirst())));
        locationIds.forEach(id -> jdbc.update("INSERT INTO professional_locations(professional_id,location_id) VALUES (?,?)", professionalId, id));
        return professionalId;
    }

    @Transactional
    public long createBlock(String email, long locationId, LocalDate date, LocalTime start, LocalTime end) {
        if (!date.isAfter(LocalDate.now(ZoneId.of("America/Bogota")))) throw new IllegalArgumentException("El bloque debe ser futuro");
        if (!end.isAfter(start) || start.getMinute() % 30 != 0 || end.getMinute() % 30 != 0 || start.getSecond() != 0 || end.getSecond() != 0) throw new IllegalArgumentException("El bloque debe usar intervalos de 30 minutos");
        long professionalId = professionalId(email);
        if (count("SELECT COUNT(*) FROM professional_locations WHERE professional_id=? AND location_id=?", professionalId, locationId) == 0) throw new IllegalArgumentException("El profesional no está asignado a la sede");
        if (count("SELECT COUNT(*) FROM availability_blocks WHERE professional_id=? AND available_date=? AND is_active=TRUE AND start_time < ? AND end_time > ?", professionalId, date, end, start) > 0) throw new IllegalArgumentException("El bloque se solapa con disponibilidad existente");
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(connection -> { PreparedStatement s = connection.prepareStatement("INSERT INTO availability_blocks(professional_id,location_id,available_date,start_time,end_time) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            s.setLong(1, professionalId); s.setLong(2, locationId); s.setObject(3, date); s.setObject(4, start); s.setObject(5, end); return s; }, holder);
        long blockId = holder.getKey().longValue();
        for (LocalDateTime slot = LocalDateTime.of(date, start); slot.isBefore(LocalDateTime.of(date, end)); slot = slot.plusMinutes(30)) jdbc.update("INSERT INTO professional_slots(availability_block_id,start_at,end_at) VALUES (?,?,?)", blockId, slot, slot.plusMinutes(30));
        return blockId;
    }

    public List<LocalDateTime> availability(long professionalId, long specialtyId, long locationId, LocalDate date) {
        int duration = jdbc.queryForObject("SELECT appointment_duration_minutes FROM specialties WHERE id=? AND is_active=TRUE", Integer.class, specialtyId);
        if (count("SELECT COUNT(*) FROM professional_specialties WHERE professional_id=? AND specialty_id=?", professionalId, specialtyId) == 0 || count("SELECT COUNT(*) FROM professional_locations WHERE professional_id=? AND location_id=?", professionalId, locationId) == 0) return List.of();
        List<LocalDateTime> free = jdbc.query("SELECT ps.start_at FROM professional_slots ps JOIN availability_blocks b ON b.id=ps.availability_block_id WHERE b.professional_id=? AND b.location_id=? AND b.available_date=? AND b.is_active=TRUE AND NOT EXISTS (SELECT 1 FROM slot_reservations r WHERE r.professional_slot_id=ps.id) ORDER BY ps.start_at", (rs, row) -> rs.getObject(1, LocalDateTime.class), professionalId, locationId, date);
        return SlotRules.availableStarts(free, duration);
    }

    public List<Map<String, Object>> specialties() { return jdbc.queryForList("SELECT id,code,name,appointment_duration_minutes AS durationMinutes,is_general AS general FROM specialties WHERE is_active=TRUE ORDER BY name"); }
    public List<Map<String, Object>> locations() { return jdbc.queryForList("SELECT id,code,name FROM locations ORDER BY name"); }
    public List<Map<String, Object>> professionals(long specialtyId, long locationId) { return jdbc.queryForList("SELECT p.id, CONCAT(u.first_name,' ',u.last_name) AS name FROM professionals p JOIN users u ON u.id=p.user_id JOIN professional_specialties ps ON ps.professional_id=p.id JOIN professional_locations pl ON pl.professional_id=p.id WHERE p.is_active=TRUE AND ps.specialty_id=? AND pl.location_id=? ORDER BY name", specialtyId, locationId); }
    public List<Map<String, Object>> requestedAppointments() {
        return jdbc.queryForList("SELECT a.id, CONCAT(patient.first_name,' ',patient.last_name) AS patientName, CONCAT(professional_user.first_name,' ',professional_user.last_name) AS professionalName, s.name AS specialtyName, l.name AS locationName, DATE_FORMAT(a.scheduled_start_at,'%Y-%m-%dT%H:%i:%s') AS start, DATE_FORMAT(a.scheduled_end_at,'%Y-%m-%dT%H:%i:%s') AS end FROM appointments a JOIN users patient ON patient.id=a.patient_user_id JOIN professionals p ON p.id=a.professional_id JOIN users professional_user ON professional_user.id=p.user_id JOIN specialties s ON s.id=a.specialty_id JOIN locations l ON l.id=a.location_id JOIN appointment_statuses status ON status.id=a.status_id WHERE status.code='REQUESTED' ORDER BY a.scheduled_start_at");
    }

    @Transactional
    public AppointmentResult reserve(String email, long professionalId, long specialtyId, long locationId, LocalDateTime start) {
        Map<String,Object> specialty = jdbc.queryForMap("SELECT appointment_duration_minutes,is_general,requires_admin_approval FROM specialties WHERE id=? AND is_active=TRUE", specialtyId);
        int duration = ((Number) specialty.get("appointment_duration_minutes")).intValue();
        if (availability(professionalId, specialtyId, locationId, start.toLocalDate()).stream().noneMatch(start::equals)) throw new SlotRules.SlotAlreadyReservedException();
        long patientId = userId(email);
        String status = Boolean.TRUE.equals(specialty.get("requires_admin_approval")) ? "REQUESTED" : "APPROVED";
        long statusId = statusId(status);
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(connection -> { PreparedStatement s = connection.prepareStatement("INSERT INTO appointments(patient_user_id,professional_id,location_id,specialty_id,status_id,scheduled_start_at,scheduled_end_at,duration_minutes) VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            s.setLong(1, patientId); s.setLong(2, professionalId); s.setLong(3, locationId); s.setLong(4, specialtyId); s.setLong(5, statusId); s.setObject(6, start); s.setObject(7, start.plusMinutes(duration)); s.setInt(8, duration); return s; }, holder);
        long appointmentId = holder.getKey().longValue();
        List<Long> slotIds = jdbc.query("SELECT ps.id FROM professional_slots ps JOIN availability_blocks b ON b.id=ps.availability_block_id WHERE b.professional_id=? AND b.location_id=? AND ps.start_at>=? AND ps.start_at<? AND NOT EXISTS (SELECT 1 FROM slot_reservations r WHERE r.professional_slot_id=ps.id) ORDER BY ps.start_at", (rs,row) -> rs.getLong(1), professionalId, locationId, start, start.plusMinutes(duration));
        if (slotIds.size() != SlotRules.requiredSlots(duration)) throw new SlotRules.SlotAlreadyReservedException();
        try { slotIds.forEach(slotId -> jdbc.update("INSERT INTO slot_reservations(professional_slot_id,appointment_id) VALUES (?,?)", slotId, appointmentId)); }
        catch (DataIntegrityViolationException exception) { throw new SlotRules.SlotAlreadyReservedException(); }
        jdbc.update("INSERT INTO appointment_status_history(appointment_id,status_id,changed_by_user_id,change_source) VALUES (?,?,?,'USER')", appointmentId, statusId, patientId);
        return new AppointmentResult(appointmentId, status, start, start.plusMinutes(duration));
    }

    @Transactional
    public void decide(String adminEmail, long appointmentId, boolean approve, String reason) {
        if (!approve && (reason == null || reason.isBlank())) throw new IllegalArgumentException("El rechazo requiere un motivo");
        Map<String,Object> appointment = jdbc.queryForMap("SELECT s.code FROM appointments a JOIN appointment_statuses s ON s.id=a.status_id WHERE a.id=? FOR UPDATE", appointmentId);
        if (!"REQUESTED".equals(appointment.get("code"))) throw new IllegalArgumentException("Solo se pueden decidir citas REQUESTED");
        String status = approve ? "APPROVED" : "REJECTED"; long newStatusId = statusId(status); long adminId = userId(adminEmail);
        jdbc.update("UPDATE appointments SET status_id=?, rejection_reason=? WHERE id=?", newStatusId, approve ? null : reason.trim(), appointmentId);
        if (!approve) jdbc.update("DELETE FROM slot_reservations WHERE appointment_id=?", appointmentId);
        jdbc.update("INSERT INTO appointment_status_history(appointment_id,status_id,changed_by_user_id,change_source,reason) VALUES (?,?,?,'ADMIN',?)", appointmentId, newStatusId, adminId, approve ? null : reason.trim());
    }

    public record AppointmentResult(long id, String status, LocalDateTime start, LocalDateTime end) { }
    private long professionalId(String email) { return jdbc.queryForObject("SELECT p.id FROM professionals p JOIN users u ON u.id=p.user_id WHERE u.email=? AND p.is_active=TRUE", Long.class, email); }
    private long userId(String email) { return jdbc.queryForObject("SELECT id FROM users WHERE email=? AND is_active=TRUE", Long.class, email); }
    private long statusId(String code) { return jdbc.queryForObject("SELECT id FROM appointment_statuses WHERE code=?", Long.class, code); }
    private int count(String sql, Object... args) { return jdbc.queryForObject(sql, Integer.class, args); }
}
