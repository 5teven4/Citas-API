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
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentLifecycleService {
    private static final ZoneId PROJECT_ZONE = ZoneId.of("America/Bogota");
    private final JdbcTemplate jdbc;
    private final SchedulingService scheduling;

    public AppointmentLifecycleService(JdbcTemplate jdbc, SchedulingService scheduling) {
        this.jdbc = jdbc;
        this.scheduling = scheduling;
    }

    public List<Map<String, Object>> myAppointments(String email, String status, LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        String sql = "SELECT a.id, a.professional_id AS professionalId, a.specialty_id AS specialtyId, a.rejection_reason AS rejectionReason, s.code AS status, s.name AS statusName, sp.name AS specialtyName, " +
                "CONCAT(u.first_name,' ',u.last_name) AS professionalName, l.name AS locationName, " +
                "DATE_FORMAT(a.scheduled_start_at,'%Y-%m-%dT%H:%i:%s') AS start, " +
            "DATE_FORMAT(a.scheduled_end_at,'%Y-%m-%dT%H:%i:%s') AS end, a.duration_minutes AS durationMinutes, " +
            "(SELECT rs.code FROM reschedule_requests rr JOIN reschedule_request_statuses rs ON rs.id=rr.status_id WHERE rr.appointment_id=a.id ORDER BY rr.created_at DESC LIMIT 1) AS rescheduleStatus, " +
            "(SELECT DATE_FORMAT(rr.requested_start_at,'%Y-%m-%dT%H:%i:%s') FROM reschedule_requests rr WHERE rr.appointment_id=a.id ORDER BY rr.created_at DESC LIMIT 1) AS rescheduleRequestedStart, " +
            "(SELECT rr.decision_reason FROM reschedule_requests rr WHERE rr.appointment_id=a.id ORDER BY rr.created_at DESC LIMIT 1) AS rescheduleDecisionReason " +
                "FROM appointments a JOIN appointment_statuses s ON s.id=a.status_id " +
                "JOIN specialties sp ON sp.id=a.specialty_id JOIN professionals p ON p.id=a.professional_id " +
                "JOIN users u ON u.id=p.user_id JOIN locations l ON l.id=a.location_id " +
                "JOIN users patient ON patient.id=a.patient_user_id WHERE patient.email=?";
        List<Object> parameters = new java.util.ArrayList<>(List.of(email));
        if (status != null && !status.isBlank()) { sql += " AND s.code=?"; parameters.add(status.trim().toUpperCase()); }
        if (from != null) { sql += " AND a.scheduled_start_at>=?"; parameters.add(from.atStartOfDay()); }
        if (to != null) { sql += " AND a.scheduled_start_at<?"; parameters.add(to.plusDays(1).atStartOfDay()); }
        sql += " ORDER BY a.scheduled_start_at DESC";
        return jdbc.queryForList(sql, parameters.toArray());
    }

    @Transactional
    public void cancel(String email, long appointmentId) {
        Map<String, Object> appointment = appointmentForUpdate(appointmentId);
        long patientId = userId(email);
        if (number(appointment, "patient_user_id") != patientId) throw new IllegalArgumentException("La cita no pertenece al usuario autenticado");
        String status = (String) appointment.get("status_code");
        if (List.of("CANCELLED", "REJECTED", "COMPLETED", "NO_SHOW").contains(status)) throw new IllegalArgumentException("La cita ya está cerrada");
        if (!timestamp(appointment, "scheduled_start_at").isAfter(now())) throw new IllegalArgumentException("Solo se pueden cancelar citas futuras");

        long cancelledId = statusId("CANCELLED");
        long pendingId = rescheduleStatusId("PENDING");
        long cancelledRequestId = rescheduleStatusId("CANCELLED");
        jdbc.update("DELETE sr FROM slot_reservations sr JOIN reschedule_requests rr ON rr.id=sr.reschedule_request_id WHERE rr.appointment_id=? AND rr.status_id=?", appointmentId, pendingId);
        jdbc.update("UPDATE reschedule_requests SET status_id=?, decision_reason='Cita cancelada por el usuario', decided_at=CURRENT_TIMESTAMP WHERE appointment_id=? AND status_id=?", cancelledRequestId, appointmentId, pendingId);
        jdbc.update("DELETE FROM slot_reservations WHERE appointment_id=?", appointmentId);
        jdbc.update("UPDATE appointments SET status_id=? WHERE id=?", cancelledId, appointmentId);
        addHistory(appointmentId, cancelledId, patientId, "USER", "Cancelada por el usuario");
    }

    @Transactional
    public long requestReschedule(String email, long appointmentId, long locationId, LocalDateTime start) {
        Map<String, Object> appointment = appointmentForUpdate(appointmentId);
        long patientId = userId(email);
        if (number(appointment, "patient_user_id") != patientId) throw new IllegalArgumentException("La cita no pertenece al usuario autenticado");
        if (!"APPROVED".equals(appointment.get("status_code"))) throw new IllegalArgumentException("Solo se pueden reprogramar citas aprobadas");
        if (!timestamp(appointment, "scheduled_start_at").isAfter(now())) throw new IllegalArgumentException("Solo se pueden reprogramar citas futuras");
        if (!start.isAfter(now())) throw new IllegalArgumentException("La nueva fecha debe ser futura");
        long appointmentProfessionalId = number(appointment, "professional_id");
        long specialtyId = number(appointment, "specialty_id");
        int duration = Math.toIntExact(number(appointment, "duration_minutes"));
        long pendingId = rescheduleStatusId("PENDING");
        if (jdbc.queryForObject("SELECT COUNT(*) FROM reschedule_requests WHERE appointment_id=? AND status_id=?", Integer.class, appointmentId, pendingId) > 0) {
            throw new IllegalArgumentException("La cita ya tiene una reprogramación pendiente");
        }
        if (scheduling.availability(appointmentProfessionalId, specialtyId, locationId, start.toLocalDate()).stream().noneMatch(start::equals)) {
            throw new IllegalArgumentException("El nuevo horario no está disponible para el mismo profesional y especialidad");
        }

        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO reschedule_requests(appointment_id,requested_by_user_id,requested_location_id,status_id,requested_start_at,requested_end_at) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, appointmentId);
            statement.setLong(2, patientId);
            statement.setLong(3, locationId);
            statement.setLong(4, pendingId);
            statement.setObject(5, start);
            statement.setObject(6, start.plusMinutes(duration));
            return statement;
        }, holder);
        long requestId = holder.getKey().longValue();
        List<Long> slotIds = jdbc.query("SELECT ps.id FROM professional_slots ps JOIN availability_blocks b ON b.id=ps.availability_block_id " +
                        "WHERE b.professional_id=? AND b.location_id=? AND b.available_date=? AND b.is_active=TRUE " +
                        "AND ps.start_at>=? AND ps.start_at<? AND NOT EXISTS (SELECT 1 FROM slot_reservations sr WHERE sr.professional_slot_id=ps.id) ORDER BY ps.start_at",
                (rs, row) -> rs.getLong(1), appointmentProfessionalId, locationId, start.toLocalDate(), start, start.plusMinutes(duration));
        if (slotIds.size() != SlotRules.requiredSlots(duration)) throw new SlotRules.SlotAlreadyReservedException();
        try {
            slotIds.forEach(slotId -> jdbc.update("INSERT INTO slot_reservations(professional_slot_id,appointment_id,reschedule_request_id) VALUES (?,NULL,?)", slotId, requestId));
        } catch (DataIntegrityViolationException exception) {
            throw new SlotRules.SlotAlreadyReservedException();
        }
        return requestId;
    }

    public List<Map<String, Object>> myHistory(String email, long appointmentId) {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM appointments a JOIN users u ON u.id=a.patient_user_id WHERE a.id=? AND u.email=?", Integer.class, appointmentId, email) == 0) {
            throw new IllegalArgumentException("La cita no pertenece al usuario autenticado");
        }
        return jdbc.queryForList("SELECT s.code AS status, h.change_source AS source, h.reason, h.created_at AS changedAt " +
                "FROM appointment_status_history h JOIN appointment_statuses s ON s.id=h.status_id WHERE h.appointment_id=? ORDER BY h.created_at", appointmentId);
    }

    public List<Map<String, Object>> professionalAgenda(String email, LocalDate from, LocalDate to, Long locationId) {
        if (to.isBefore(from) || to.isAfter(from.plusDays(31))) throw new IllegalArgumentException("El rango debe ser de hasta 31 días");
        String sql = "SELECT a.id, s.code AS status, CONCAT(patient.first_name,' ',patient.last_name) AS patientName, " +
                "sp.name AS specialtyName, l.name AS locationName, DATE_FORMAT(a.scheduled_start_at,'%Y-%m-%dT%H:%i:%s') AS start, " +
                "DATE_FORMAT(a.scheduled_end_at,'%Y-%m-%dT%H:%i:%s') AS end FROM appointments a " +
                "JOIN appointment_statuses s ON s.id=a.status_id JOIN users patient ON patient.id=a.patient_user_id " +
                "JOIN specialties sp ON sp.id=a.specialty_id JOIN locations l ON l.id=a.location_id " +
                "JOIN professionals p ON p.id=a.professional_id JOIN users owner ON owner.id=p.user_id " +
                "WHERE owner.email=? AND s.code IN ('APPROVED','COMPLETED','NO_SHOW') AND a.scheduled_start_at>=? AND a.scheduled_start_at<?";
        List<Object> parameters = new java.util.ArrayList<>(List.of(email, from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
        if (locationId != null) { sql += " AND a.location_id=?"; parameters.add(locationId); }
        sql += " ORDER BY a.scheduled_start_at";
        return jdbc.queryForList(sql, parameters.toArray());
    }

    @Transactional
    public void closeAppointment(String email, long appointmentId, String newStatus) {
        if (!List.of("COMPLETED", "NO_SHOW").contains(newStatus)) throw new IllegalArgumentException("El estado debe ser COMPLETED o NO_SHOW");
        Map<String, Object> appointment = appointmentForUpdate(appointmentId);
        long professionalUserId = jdbc.queryForObject("SELECT u.id FROM professionals p JOIN users u ON u.id=p.user_id WHERE p.id=? AND u.email=? AND p.is_active=TRUE", Long.class, number(appointment, "professional_id"), email);
        if (!"APPROVED".equals(appointment.get("status_code"))) throw new IllegalArgumentException("Solo se pueden cerrar citas APPROVED");
        if (timestamp(appointment, "scheduled_start_at").isAfter(now())) throw new IllegalArgumentException("La cita aún no ha iniciado");
        long statusId = statusId(newStatus);
        jdbc.update("UPDATE appointments SET status_id=? WHERE id=?", statusId, appointmentId);
        addHistory(appointmentId, statusId, professionalUserId, "USER", "Cierre realizado por PROFESSIONAL");
    }

    public List<Map<String, Object>> pendingReschedules() {
        return jdbc.queryForList("SELECT rr.id, rr.appointment_id AS appointmentId, CONCAT(patient.first_name,' ',patient.last_name) AS patientName, " +
                "CONCAT(pro.first_name,' ',pro.last_name) AS professionalName, sp.name AS specialtyName, l.name AS locationName, " +
                "DATE_FORMAT(a.scheduled_start_at,'%Y-%m-%dT%H:%i:%s') AS currentStart, " +
                "DATE_FORMAT(rr.requested_start_at,'%Y-%m-%dT%H:%i:%s') AS requestedStart " +
                "FROM reschedule_requests rr JOIN appointments a ON a.id=rr.appointment_id JOIN users patient ON patient.id=a.patient_user_id " +
                "JOIN professionals p ON p.id=a.professional_id JOIN users pro ON pro.id=p.user_id JOIN specialties sp ON sp.id=a.specialty_id " +
                "JOIN locations l ON l.id=rr.requested_location_id JOIN reschedule_request_statuses rs ON rs.id=rr.status_id " +
                "WHERE rs.code='PENDING' ORDER BY rr.created_at");
    }

    @Transactional
    public void decideReschedule(String adminEmail, long requestId, boolean approve, String reason) {
        long pendingId = rescheduleStatusId("PENDING");
        Long appointmentId = jdbc.queryForObject("SELECT appointment_id FROM reschedule_requests WHERE id=?", Long.class, requestId);
        appointmentForUpdate(appointmentId);
        Map<String, Object> request = jdbc.queryForMap("SELECT appointment_id,requested_location_id,requested_start_at,requested_end_at,status_id FROM reschedule_requests WHERE id=? FOR UPDATE", requestId);
        if (number(request, "status_id") != pendingId) throw new IllegalArgumentException("La solicitud ya fue decidida");
        if (!approve && (reason == null || reason.isBlank())) throw new IllegalArgumentException("El rechazo requiere un motivo");
        long adminId = userId(adminEmail);
        long terminalId = rescheduleStatusId(approve ? "APPROVED" : "REJECTED");
        if (approve) {
            LocalDateTime requestedStart = jdbc.queryForObject("SELECT requested_start_at FROM reschedule_requests WHERE id=?", (rs, row) -> rs.getObject(1, LocalDateTime.class), requestId);
            LocalDateTime requestedEnd = jdbc.queryForObject("SELECT requested_end_at FROM reschedule_requests WHERE id=?", (rs, row) -> rs.getObject(1, LocalDateTime.class), requestId);
            jdbc.update("DELETE FROM slot_reservations WHERE appointment_id=? AND reschedule_request_id IS NULL", appointmentId);
            jdbc.update("UPDATE slot_reservations SET appointment_id=?,reschedule_request_id=NULL WHERE reschedule_request_id=?", appointmentId, requestId);
            jdbc.update("UPDATE appointments SET location_id=?,scheduled_start_at=?,scheduled_end_at=? WHERE id=?", number(request, "requested_location_id"), requestedStart, requestedEnd, appointmentId);
        } else {
            jdbc.update("DELETE FROM slot_reservations WHERE reschedule_request_id=?", requestId);
        }
        jdbc.update("UPDATE reschedule_requests SET status_id=?,decision_reason=?,decided_by_user_id=?,decided_at=CURRENT_TIMESTAMP WHERE id=?",
                terminalId, approve ? null : reason.trim(), adminId, requestId);
    }

    private Map<String, Object> appointmentForUpdate(long id) {
        return jdbc.queryForMap("SELECT a.id,a.patient_user_id,a.professional_id,a.specialty_id,a.duration_minutes,a.scheduled_start_at,s.code AS status_code " +
                "FROM appointments a JOIN appointment_statuses s ON s.id=a.status_id WHERE a.id=? FOR UPDATE", id);
    }

    private void addHistory(long appointmentId, long statusId, long actorId, String source, String reason) {
        jdbc.update("INSERT INTO appointment_status_history(appointment_id,status_id,changed_by_user_id,change_source,reason) VALUES (?,?,?,?,?)", appointmentId, statusId, actorId, source, reason);
    }

    private long userId(String email) { return jdbc.queryForObject("SELECT id FROM users WHERE email=? AND is_active=TRUE", Long.class, email); }
    private long statusId(String code) { return jdbc.queryForObject("SELECT id FROM appointment_statuses WHERE code=?", Long.class, code); }
    private long rescheduleStatusId(String code) { return jdbc.queryForObject("SELECT id FROM reschedule_request_statuses WHERE code=?", Long.class, code); }
    private long number(Map<String, Object> values, String key) { return ((Number) values.get(key)).longValue(); }
    private LocalDateTime timestamp(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value instanceof LocalDateTime localDateTime ? localDateTime : ((java.sql.Timestamp) value).toLocalDateTime();
    }
    private LocalDateTime now() { return LocalDateTime.now(PROJECT_ZONE); }
}