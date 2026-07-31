package com.thaumcraftmodern.crucible;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleTransactionTest {
    @Test
    void failedEssentiaReservationIsAtomic() {
        EssentiaStore essentia = new EssentiaStore();
        essentia.add("ignis", 3);

        assertFalse(essentia.removeAll(Map.of(
                "ignis", 3,
                "potentia", 1
        )));
        assertEquals(Map.of("ignis", 3), essentia.view());
    }

    @Test
    void successfulEssentiaReservationConsumesExactCost() {
        EssentiaStore essentia = new EssentiaStore();
        essentia.add("ignis", 5);
        essentia.add("potentia", 4);

        assertTrue(essentia.removeAll(Map.of(
                "ignis", 3,
                "potentia", 3
        )));
        assertEquals(2, essentia.amount("ignis"));
        assertEquals(1, essentia.amount("potentia"));
    }

    @Test
    void essentiaSurvivesNbtRoundTrip() {
        EssentiaStore original = new EssentiaStore();
        original.add("ignis", 4);
        original.add("potentia", 3);
        EssentiaStore loaded = new EssentiaStore();
        loaded.load(original.save());
        assertEquals(original.view(), loaded.view());
    }

}
