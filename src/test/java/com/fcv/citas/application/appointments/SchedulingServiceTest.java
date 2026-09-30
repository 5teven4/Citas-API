package com.fcv.citas.application.appointments;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SchedulingServiceTest {
    @Test
    void listsOnlyRequestedAppointmentsForAdministrativeDecision() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Map<String, Object> appointment = Map.of(
                "id", 17L,
                "patientName", "Paciente Sintetico",
                "professionalName", "Profesional Sintetico",
                "specialtyName", "Cardiologia",
                "locationName", "Sede de prueba",
                "start", "2030-01-10T09:00:00",
                "end", "2030-01-10T10:00:00");
        when(jdbc.queryForList(org.mockito.ArgumentMatchers.contains("status.code='REQUESTED'")))
                .thenReturn(List.of(appointment));

        List<Map<String, Object>> result = new SchedulingService(jdbc).requestedAppointments();

        assertEquals(List.of(appointment), result);
        verify(jdbc).queryForList(org.mockito.ArgumentMatchers.contains("status.code='REQUESTED'"));
    }
}