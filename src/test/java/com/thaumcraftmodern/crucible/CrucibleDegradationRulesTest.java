package com.thaumcraftmodern.crucible;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.testing.AspectFixtures;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleDegradationRulesTest {
    private final AspectCatalog aspects =
            AspectFixtures.firstDiscoveryCatalog();

    @Test
    void compoundAspectBecomesExactlyOneDirectComponent() {
        EssentiaStore essentia = new EssentiaStore();
        essentia.add("potentia", 1);

        CrucibleDegradationRules.Result result =
                CrucibleDegradationRules.degradeOne(
                        essentia,
                        aspects::lookup,
                        bound -> 0,
                        () -> true
                );

        assertTrue(result.changed());
        assertFalse(result.spill());
        assertEquals("potentia", result.removedAspect());
        assertEquals("ordo", result.addedComponent());
        assertEquals(0, essentia.amount("potentia"));
        assertEquals(1, essentia.amount("ordo"));
        assertEquals(1, essentia.total());
    }

    @Test
    void primalFirstChoiceIsRerolledExactlyOnceLikeTc4() {
        EssentiaStore essentia = new EssentiaStore();
        essentia.add("ignis", 1);
        essentia.add("potentia", 1);
        AtomicInteger calls = new AtomicInteger();

        CrucibleDegradationRules.Result result =
                CrucibleDegradationRules.degradeOne(
                        essentia,
                        aspects::lookup,
                        bound -> calls.getAndIncrement(),
                        () -> false
                );

        assertEquals(2, calls.get());
        assertFalse(result.spill());
        assertEquals("potentia", result.removedAspect());
        assertEquals("ignis", result.addedComponent());
        assertEquals(2, essentia.amount("ignis"));
    }

    @Test
    void secondPrimalChoiceIsLostAndRequestsSpill() {
        EssentiaStore essentia = new EssentiaStore();
        essentia.add("ignis", 1);

        CrucibleDegradationRules.Result result =
                CrucibleDegradationRules.degradeOne(
                        essentia,
                        aspects::lookup,
                        bound -> 0,
                        () -> true
                );

        assertTrue(result.changed());
        assertTrue(result.spill());
        assertEquals("ignis", result.removedAspect());
        assertNull(result.addedComponent());
        assertTrue(essentia.isEmpty());
    }

    @Test
    void emptyStoreDoesNothing() {
        CrucibleDegradationRules.Result result =
                CrucibleDegradationRules.degradeOne(
                        new EssentiaStore(),
                        aspects::lookup,
                        bound -> 0,
                        () -> true
                );

        assertFalse(result.changed());
        assertFalse(result.spill());
    }
}
