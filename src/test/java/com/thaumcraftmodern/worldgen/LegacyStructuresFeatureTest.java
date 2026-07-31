package com.thaumcraftmodern.worldgen;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyStructuresFeatureTest {
    @Test
    void hilltopGeometryMatchesOriginalEightTotemLayout() {
        int pillars = 0;
        int foundation = 0;
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (HilltopStonesGeneration.isFoundationPosition(x, z)) {
                    foundation++;
                }
                if (HilltopStonesGeneration.isPillarPosition(x, z)) {
                    pillars++;
                }
            }
        }
        assertEquals(45, foundation);
        assertEquals(8, pillars);
    }

    @Test
    void hilltopPillarHeightAndSurfaceGateMatchTc4() {
        assertEquals(
                new HilltopStonesGeneration.PillarPlan(2, true),
                HilltopStonesGeneration.planPillar(height -> height == 2)
        );
        assertEquals(
                new HilltopStonesGeneration.PillarPlan(3, true),
                HilltopStonesGeneration.planPillar(height -> height == 3)
        );
        assertEquals(
                new HilltopStonesGeneration.PillarPlan(4, false),
                HilltopStonesGeneration.planPillar(height -> false)
        );
        assertTrue(HilltopStonesGeneration.compatibleSurfaceHeight(85, 85));
        assertTrue(HilltopStonesGeneration.compatibleSurfaceHeight(85, 87));
        assertFalse(HilltopStonesGeneration.compatibleSurfaceHeight(84, 84));
        assertFalse(HilltopStonesGeneration.compatibleSurfaceHeight(85, 84));
        assertFalse(HilltopStonesGeneration.compatibleSurfaceHeight(85, 88));
        assertEquals(84, HilltopStonesGeneration.floorY(85));
        assertEquals(
                6,
                HilltopStonesGeneration.NODE_HEIGHT
                        - HilltopStonesGeneration.FLOOR_OFFSET
        );
        assertEquals(
                5,
                HilltopStonesGeneration.REQUIRED_SUPPORT_DEPTH
        );
        assertEquals(
                5,
                HilltopStonesGeneration.cardinalSupportSamples().length
        );
    }

    @Test
    void ancientMoundBlueprintMatchesOriginalStaticPayload() {
        assertTrue(AncientMoundBlueprint.dimensionsAreValid());
        assertEquals(
                AncientMoundBlueprint.STATIC_PLACEMENT_COUNT,
                AncientMoundBlueprint.encodedPlacementCount()
        );
    }

    @Test
    void moundAndHilltopSurfaceBlocksMatchTc4() {
        assertTrue(ClassicStructureSurfacePolicy.acceptsMoundOrHilltop(
                true, false, false, false, false
        ));
        assertTrue(ClassicStructureSurfacePolicy.acceptsMoundOrHilltop(
                false, true, false, false, false
        ));
        assertTrue(ClassicStructureSurfacePolicy.acceptsMoundOrHilltop(
                false, false, true, false, false
        ));
        assertTrue(ClassicStructureSurfacePolicy.acceptsMoundOrHilltop(
                false, false, false, true, true
        ));
        assertFalse(ClassicStructureSurfacePolicy.acceptsMoundOrHilltop(
                false, false, false, true, false
        ));
    }

    @Test
    void standaloneSurfaceSearchMatchesLegacyLimit() {
        assertEquals(
                2,
                ClassicStructureSurfacePolicy.MAX_UPWARD_SEARCH
        );
    }

    @Test
    void eldritchRingAcceptsItsSixOriginalGroundBlocks() {
        assertTrue(ClassicStructureSurfacePolicy.acceptsEldritchRing(
                true, false, false, false, false, false, false, false
        ));
        assertTrue(ClassicStructureSurfacePolicy.acceptsEldritchRing(
                false, true, false, false, false, false, false, false
        ));
        assertTrue(ClassicStructureSurfacePolicy.acceptsEldritchRing(
                false, false, true, false, false, false, false, false
        ));
        assertTrue(ClassicStructureSurfacePolicy.acceptsEldritchRing(
                false, false, false, true, false, false, false, false
        ));
        assertTrue(ClassicStructureSurfacePolicy.acceptsEldritchRing(
                false, false, false, false, true, false, false, false
        ));
        assertTrue(ClassicStructureSurfacePolicy.acceptsEldritchRing(
                false, false, false, false, false, true, false, false
        ));
        assertFalse(ClassicStructureSurfacePolicy.acceptsEldritchRing(
                false, false, false, false, false, false, true, false
        ));
    }

    @Test
    void crimsonCultUsesExactlyFourOfTenOriginalRingVariants() {
        long cultVariants = IntStream.range(
                        0,
                        CrimsonCultStructureSpawn.VARIANT_COUNT
                )
                .filter(CrimsonCultStructureSpawn::isCultVariant)
                .count();

        assertEquals(4L, cultVariants);
        assertFalse(CrimsonCultStructureSpawn.isCultVariant(0));
        assertTrue(CrimsonCultStructureSpawn.isCultVariant(1));
        assertTrue(CrimsonCultStructureSpawn.isCultVariant(4));
        assertFalse(CrimsonCultStructureSpawn.isCultVariant(5));
    }

    @Test
    void crimsonCultGuardCompositionMatchesStableOriginalPopulation() {
        assertEquals(
                4,
                CrimsonCultStructureSpawn.clericOffsets().size()
        );
        assertEquals(
                4,
                CrimsonCultStructureSpawn.knightOffsets().size()
        );
    }
}
