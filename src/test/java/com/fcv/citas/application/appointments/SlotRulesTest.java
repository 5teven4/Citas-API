package com.fcv.citas.application.appointments;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class SlotRulesTest {
    private final LocalDateTime nine = LocalDateTime.of(2030, 1, 10, 9, 0);

    @Test void thirtyMinutesNeedsOneFreeSlot() {
        assertEquals(List.of(nine), SlotRules.availableStarts(List.of(nine), 30));
    }

    @Test void sixtyMinutesNeedsTwoConsecutiveSlots() {
        assertEquals(List.of(nine), SlotRules.availableStarts(List.of(nine, nine.plusMinutes(30)), 60));
        assertTrue(SlotRules.availableStarts(List.of(nine, nine.plusMinutes(60)), 60).isEmpty());
    }

    @Test void overlappingReservationIsRejected() {
        assertThrows(SlotRules.SlotAlreadyReservedException.class,
                () -> SlotRules.assertCanReserve(Set.of(nine.plusMinutes(30)), nine, 60));
    }
}
