package com.fcv.citas.infrastructure.database;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fcv.citas.application.appointments.AppointmentLifecycleService;
import com.fcv.citas.application.appointments.SchedulingService;
import com.fcv.citas.application.appointments.SlotRules;
import com.fcv.citas.application.catalogs.CatalogAdminService;
import com.fcv.citas.application.auth.PasswordRecoveryService;
import com.fcv.citas.infrastructure.security.JwtService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.security.password-reset.expose-token=true")
@AutoConfigureMockMvc
@Transactional
class FlywayMigrationIT {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private AppointmentLifecycleService lifecycle;
    @Autowired
    private SchedulingService scheduling;
    @Autowired
    private CatalogAdminService catalogs;
    @Autowired
    private PasswordRecoveryService passwordRecovery;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void appliesLifecycleAndRecoverySchemaOnMysql() {
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM reschedule_request_statuses WHERE code='PENDING'", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='slot_reservations' AND column_name='reschedule_request_id'", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='password_reset_tokens'", Integer.class));
    }

    @Test
    @Transactional
    void rebookingKeepsOriginalSlotsUntilApprovalAndCancellationReleasesSlots() {
        String suffix = Long.toString(System.nanoTime());
        long patientId = insertUser("patient-" + suffix + "@example.test");
        long adminId = insertUser("admin-" + suffix + "@example.test");
        String professionalEmail = "professional-" + suffix + "@example.test";
        long professionalUserId = insertUser(professionalEmail);
        jdbc.update("INSERT INTO professionals(user_id,professional_code,license_number) VALUES (?,?,?)", professionalUserId, "P" + suffix, "L" + suffix);
        long professionalId = jdbc.queryForObject("SELECT id FROM professionals WHERE user_id=?", Long.class, professionalUserId);
        long specialtyId = jdbc.queryForObject("SELECT id FROM specialties WHERE code='CARDIOLOGY'", Long.class);
        long locationId = jdbc.queryForObject("SELECT id FROM locations WHERE code='HIC'", Long.class);
        jdbc.update("INSERT INTO professional_specialties(professional_id,specialty_id,is_primary) VALUES (?,?,TRUE)", professionalId, specialtyId);
        jdbc.update("INSERT INTO professional_locations(professional_id,location_id) VALUES (?,?)", professionalId, locationId);

        LocalDate day = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(10);
        scheduling.createBlock(professionalEmail, locationId, day, LocalTime.of(9, 0), LocalTime.of(12, 0));
        LocalDateTime originalStart = LocalDateTime.of(day, LocalTime.of(9, 0));
        long appointmentId = insertAppointment(patientId, professionalId, locationId, specialtyId, originalStart, 60);
        reserveSlots(appointmentId, originalStart, 60);

        assertEquals(List.of(originalStart.plusHours(1), originalStart.plusMinutes(90), originalStart.plusHours(2)), scheduling.availability(professionalId, specialtyId, locationId, day));
        long requestId = lifecycle.requestReschedule("patient-" + suffix + "@example.test", appointmentId, locationId, originalStart.plusHours(1));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM slot_reservations WHERE appointment_id=? AND reschedule_request_id IS NULL", Integer.class, appointmentId));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM slot_reservations WHERE reschedule_request_id=?", Integer.class, requestId));
        Map<String, Object> pendingAppointment = lifecycle.myAppointments("patient-" + suffix + "@example.test", null, null, null).getFirst();
        assertEquals("PENDING", pendingAppointment.get("rescheduleStatus"));
        assertEquals(originalStart.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), pendingAppointment.get("rescheduleRequestedStart"));
        lifecycle.decideReschedule("admin-" + suffix + "@example.test", requestId, true, null);
        assertEquals(originalStart.plusHours(1), jdbc.queryForObject("SELECT scheduled_start_at FROM appointments WHERE id=?", LocalDateTime.class, appointmentId));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM slot_reservations WHERE appointment_id=? AND reschedule_request_id IS NULL", Integer.class, appointmentId));

        LocalDateTime secondStart = LocalDateTime.of(day, LocalTime.of(9, 0));
        long secondAppointmentId = insertAppointment(patientId, professionalId, locationId, specialtyId, secondStart, 60);
        reserveSlots(secondAppointmentId, secondStart, 60);
        long rejectedRequestId = lifecycle.requestReschedule("patient-" + suffix + "@example.test", secondAppointmentId, locationId, secondStart.plusHours(2));
        lifecycle.decideReschedule("admin-" + suffix + "@example.test", rejectedRequestId, false, "Cambio no disponible");
        assertEquals(secondStart, jdbc.queryForObject("SELECT scheduled_start_at FROM appointments WHERE id=?", LocalDateTime.class, secondAppointmentId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM slot_reservations WHERE reschedule_request_id=?", Integer.class, rejectedRequestId));

        assertDoesNotThrow(() -> lifecycle.cancel("patient-" + suffix + "@example.test", secondAppointmentId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM slot_reservations WHERE appointment_id=?", Integer.class, secondAppointmentId));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM appointment_status_history WHERE appointment_id=? AND status_id=(SELECT id FROM appointment_statuses WHERE code='CANCELLED')", Integer.class, secondAppointmentId));
        assertThrows(IllegalArgumentException.class, () -> lifecycle.cancel("admin-" + suffix + "@example.test", appointmentId));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE id=?", Integer.class, adminId));
    }

    @Test
    void userCannotReadAdministrativeRequests() throws Exception {
        String suffix = Long.toString(System.nanoTime());
        String email = "role-user-" + suffix + "@example.test";
        long userId = insertUser(email);
        long roleId = jdbc.queryForObject("SELECT id FROM roles WHERE code='USER'", Long.class);
        jdbc.update("INSERT INTO user_roles(user_id,role_id) VALUES (?,?)", userId, roleId);
        String token = jwtService.issueAccess(userId, email, List.of("USER")).value();

        mockMvc.perform(get("/api/v1/admin/appointments/requests").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void appointmentListFiltersByOwnerStatusAndInclusiveDateRange() {
        String suffix = Long.toString(System.nanoTime());
        String patientEmail = "filter-patient-" + suffix + "@example.test";
        String otherEmail = "filter-other-" + suffix + "@example.test";
        long patientId = insertUser(patientEmail);
        insertUser(otherEmail);
        long professionalUserId = insertUser("filter-professional-" + suffix + "@example.test");
        jdbc.update("INSERT INTO professionals(user_id,professional_code,license_number) VALUES (?,?,?)", professionalUserId, "FP" + suffix, "FL" + suffix);
        long professionalId = jdbc.queryForObject("SELECT id FROM professionals WHERE user_id=?", Long.class, professionalUserId);
        long specialtyId = jdbc.queryForObject("SELECT id FROM specialties WHERE code='GENERAL_MEDICINE'", Long.class);
        long locationId = jdbc.queryForObject("SELECT id FROM locations WHERE code='HIC'", Long.class);
        LocalDate day = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(3);
        insertAppointment(patientId, professionalId, locationId, specialtyId, LocalDateTime.of(day, LocalTime.of(9, 0)), 30);

        List<Map<String, Object>> filtered = lifecycle.myAppointments(patientEmail, "APPROVED", day, day);

        assertEquals(1, filtered.size());
        assertEquals("APPROVED", filtered.getFirst().get("status"));
        assertEquals(List.of(), lifecycle.myAppointments(otherEmail, null, day, day));
        assertThrows(IllegalArgumentException.class, () -> lifecycle.myAppointments(patientEmail, null, day.plusDays(1), day));
        String professionalEmail = "filter-professional-" + suffix + "@example.test";
        assertEquals(1, lifecycle.professionalAgenda(professionalEmail, day, day, locationId).size());
        long pastAppointmentId = insertAppointment(patientId, professionalId, locationId, specialtyId,
            LocalDateTime.of(day.minusDays(4), LocalTime.of(9, 0)), 30);
        lifecycle.closeAppointment(professionalEmail, pastAppointmentId, "COMPLETED");
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM appointments a JOIN appointment_statuses s ON s.id=a.status_id WHERE a.id=? AND s.code='COMPLETED'", Integer.class, pastAppointmentId));
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class,
            () -> lifecycle.closeAppointment(otherEmail, pastAppointmentId, "NO_SHOW"));
    }

    @Test
    void passwordRecoveryTokenIsSingleUseAndRevokesRefreshTokens() {
        String suffix = Long.toString(System.nanoTime());
        String email = "recovery-" + suffix + "@example.test";
        long userId = insertUser(email);
        String tokenHash = hash("synthetic-refresh-" + suffix);
        jdbc.update("INSERT INTO refresh_tokens(user_id,token_hash,expires_at) VALUES (?,?,?)", userId, tokenHash, java.sql.Timestamp.from(java.time.Instant.now().plusSeconds(3600)));

        String recoveryToken = (String) passwordRecovery.request(email).get("developmentToken");
        passwordRecovery.reset(recoveryToken, "NewTrainingPassword123");

        String updatedHash = jdbc.queryForObject("SELECT password_hash FROM users WHERE id=?", String.class, userId);
        assertEquals(true, passwordEncoder.matches("NewTrainingPassword123", updatedHash));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM password_reset_tokens WHERE user_id=? AND used_at IS NOT NULL", Integer.class, userId));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM refresh_tokens WHERE user_id=? AND revoked_at IS NOT NULL", Integer.class, userId));
        assertThrows(IllegalArgumentException.class, () -> passwordRecovery.reset(recoveryToken, "AnotherTrainingPassword123"));
    }

    @Test
    @Transactional
    void referencedCatalogsCanBeDeactivatedWithoutPhysicalDeletion() {
        String suffix = Long.toUnsignedString(System.nanoTime());
        long epsId = catalogs.createEps("T" + suffix, "EPS sintética " + suffix);
        long regimeId = jdbc.queryForObject("SELECT id FROM insurance_regimes WHERE code='CONTRIBUTIVO'", Long.class);
        long planId = catalogs.createPlan(epsId, regimeId, "P" + suffix, "Plan sintético");
        catalogs.activateEps(epsId, false);
        catalogs.activatePlan(planId, false);

        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM eps WHERE id=?", Integer.class, epsId));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM eps_plans WHERE id=? AND is_active=FALSE", Integer.class, planId));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM eps_plans WHERE id=? AND eps_id=?", Integer.class, planId, epsId));

        long specialtyId = catalogs.createSpecialty("S" + suffix, "Especialidad sintética", 60, false, true);
        catalogs.activateSpecialty(specialtyId, false);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM specialties WHERE id=? AND is_active=FALSE", Integer.class, specialtyId));
        assertThrows(IllegalArgumentException.class, () -> catalogs.createSpecialty("X" + suffix, "Duración inválida", 45, false, true));
    }

    @Test
    @Transactional
    void secondReservationForAnOccupiedSlotReturnsConflict() {
        String suffix = Long.toUnsignedString(System.nanoTime());
        long firstPatient = insertUser("reserve-first-" + suffix + "@example.test");
        long secondPatient = insertUser("reserve-second-" + suffix + "@example.test");
        String professionalEmail = "reserve-professional-" + suffix + "@example.test";
        long professionalUser = insertUser(professionalEmail);
        jdbc.update("INSERT INTO professionals(user_id,professional_code,license_number) VALUES (?,?,?)", professionalUser, "RP" + suffix, "RL" + suffix);
        long professionalId = jdbc.queryForObject("SELECT id FROM professionals WHERE user_id=?", Long.class, professionalUser);
        long specialtyId = jdbc.queryForObject("SELECT id FROM specialties WHERE code='GENERAL_MEDICINE'", Long.class);
        long locationId = jdbc.queryForObject("SELECT id FROM locations WHERE code='HIC'", Long.class);
        jdbc.update("INSERT INTO professional_specialties(professional_id,specialty_id,is_primary) VALUES (?,?,TRUE)", professionalId, specialtyId);
        jdbc.update("INSERT INTO professional_locations(professional_id,location_id) VALUES (?,?)", professionalId, locationId);
        LocalDate day = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(5);
        scheduling.createBlock(professionalEmail, locationId, day, LocalTime.of(9, 0), LocalTime.of(10, 0));
        LocalDateTime start = LocalDateTime.of(day, LocalTime.of(9, 0));

        assertEquals("APPROVED", scheduling.reserve("reserve-first-" + suffix + "@example.test", professionalId, specialtyId, locationId, start).status());
        assertThrows(SlotRules.SlotAlreadyReservedException.class,
                () -> scheduling.reserve("reserve-second-" + suffix + "@example.test", professionalId, specialtyId, locationId, start));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM appointments WHERE patient_user_id IN (?,?)", Integer.class, firstPatient, secondPatient));
    }

    private long insertUser(String email) {
        jdbc.update("INSERT INTO users(first_name,last_name,document_type,document_number,email,password_hash) VALUES ('Persona','Sintetica','TEST',?,?, 'not-used')", "T" + Integer.toUnsignedString(email.hashCode()), email);
        return jdbc.queryForObject("SELECT id FROM users WHERE email=?", Long.class, email);
    }

    private long insertAppointment(long patientId, long professionalId, long locationId, long specialtyId, LocalDateTime start, int duration) {
        long approvedId = jdbc.queryForObject("SELECT id FROM appointment_statuses WHERE code='APPROVED'", Long.class);
        jdbc.update("INSERT INTO appointments(patient_user_id,professional_id,location_id,specialty_id,status_id,scheduled_start_at,scheduled_end_at,duration_minutes) VALUES (?,?,?,?,?,?,?,?)",
                patientId, professionalId, locationId, specialtyId, approvedId, start, start.plusMinutes(duration), duration);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    private void reserveSlots(long appointmentId, LocalDateTime start, int duration) {
        jdbc.update("INSERT INTO slot_reservations(professional_slot_id,appointment_id) SELECT id,? FROM professional_slots WHERE start_at>=? AND start_at<? ORDER BY start_at",
                appointmentId, start, start.plusMinutes(duration));
    }

    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }
}