package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumonomiconAspectCostLayoutTest {
    @Test
    void keepsSixClassicPrimalsOnOneCenteredRow() {
        List<ThaumonomiconAspectCostLayout.Slot> slots =
                ThaumonomiconAspectCostLayout.arrange(6, 112, 198);

        assertEquals(6, slots.size());
        assertTrue(slots.stream().mapToInt(
                ThaumonomiconAspectCostLayout.Slot::y
        ).distinct().count() == 1);
        assertEquals(182, slots.get(0).y());
        assertEquals(109, slots.get(5).x() + 16);
    }

    @Test
    void wrapsWithoutDroppingAnyAspectCost() {
        List<ThaumonomiconAspectCostLayout.Slot> slots =
                ThaumonomiconAspectCostLayout.arrange(14, 112, 198);

        assertEquals(14, slots.size());
        assertEquals(3, slots.stream().mapToInt(
                ThaumonomiconAspectCostLayout.Slot::y
        ).distinct().count());
        assertEquals(52, ThaumonomiconAspectCostLayout.requiredHeight(
                14,
                112
        ));
    }
}
