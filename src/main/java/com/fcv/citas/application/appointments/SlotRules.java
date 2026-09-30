package com.fcv.citas.application.appointments;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/** Reglas puras del dominio para slots discretos de 30 minutos. */
public final class SlotRules {
    private SlotRules() { }

    public static List<LocalDateTime> availableStarts(List<LocalDateTime> freeSlotStarts, int durationMinutes) {
        int requiredSlots = requiredSlots(durationMinutes);
        Set<LocalDateTime> free = Set.copyOf(freeSlotStarts);
        return free.stream().sorted(Comparator.naturalOrder())
                .filter(start -> IntStream.range(0, requiredSlots)
                        .mapToObj(index -> start.plusMinutes(index * 30L)).allMatch(free::contains))
                .toList();
    }

    public static int requiredSlots(int durationMinutes) {
        if (durationMinutes != 30 && durationMinutes != 60) {
            throw new IllegalArgumentException("La duración debe ser 30 o 60 minutos");
        }
        return durationMinutes / 30;
    }

    public static void assertCanReserve(Set<LocalDateTime> reservedStarts, LocalDateTime start, int durationMinutes) {
        int requiredSlots = requiredSlots(durationMinutes);
        boolean collision = IntStream.range(0, requiredSlots)
                .mapToObj(index -> start.plusMinutes(index * 30L)).anyMatch(reservedStarts::contains);
        if (collision) throw new SlotAlreadyReservedException();
    }

    public static final class SlotAlreadyReservedException extends RuntimeException {
        public SlotAlreadyReservedException() { super("Uno o más slots ya están reservados"); }
    }
}
