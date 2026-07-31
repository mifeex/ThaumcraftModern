package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ThaumonomiconCrucibleRecipeLayoutTest {
    @Test
    void raisesTheCompleteRecipeDiagramByTenPixels() {
        assertEquals(
                -10,
                ThaumonomiconCrucibleRecipeLayout.CONTENT_OFFSET_Y
        );
    }

    @Test
    void centersOneAndTwoAspectsInsideThreeSlotBasin() {
        assertEquals(
                List.of(
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                0,
                                48,
                                128
                        )
                ),
                ThaumonomiconCrucibleRecipeLayout.aspectSlots(1)
        );
        assertEquals(
                List.of(
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                0,
                                38,
                                128
                        ),
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                1,
                                58,
                                128
                        )
                ),
                ThaumonomiconCrucibleRecipeLayout.aspectSlots(2)
        );
    }

    @Test
    void keepsCompleteRowsOnTheOriginalTwentyPixelGrid() {
        assertEquals(
                List.of(
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                0,
                                28,
                                118
                        ),
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                1,
                                48,
                                118
                        ),
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                2,
                                68,
                                118
                        ),
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                3,
                                28,
                                138
                        ),
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                4,
                                48,
                                138
                        ),
                        new ThaumonomiconCrucibleRecipeLayout.Slot(
                                5,
                                68,
                                138
                        )
                ),
                ThaumonomiconCrucibleRecipeLayout.aspectSlots(6)
        );
    }

    @Test
    void keepsTc4LegacyTwoRowLeftAlignment() {
        List<ThaumonomiconCrucibleRecipeLayout.Slot> slots =
                ThaumonomiconCrucibleRecipeLayout.aspectSlots(4);

        assertEquals(4, slots.size());
        assertEquals(
                new ThaumonomiconCrucibleRecipeLayout.Slot(3, 28, 138),
                slots.get(3)
        );
    }

    @Test
    void centersIncompleteFinalRowForThreeRowGridLikeTc4() {
        List<ThaumonomiconCrucibleRecipeLayout.Slot> slots =
                ThaumonomiconCrucibleRecipeLayout.aspectSlots(7);

        assertEquals(7, slots.size());
        assertEquals(
                new ThaumonomiconCrucibleRecipeLayout.Slot(6, 48, 148),
                slots.get(6)
        );
    }

    @Test
    void centersTheLayoutWithinAnyResearchPageWidth() {
        assertEquals(13, ThaumonomiconCrucibleRecipeLayout.left(0, 139));
        assertEquals(30, ThaumonomiconCrucibleRecipeLayout.left(25, 123));
    }
}
