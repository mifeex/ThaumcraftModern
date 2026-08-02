package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ModernWorldgenPolicyTest {
    @Test
    void biomeOverlayUsesTheSeededModernClimateRouter() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyOverworldBiomeSource.java"
        ));

        assertTrue(source.contains("sampler.sample(quartX, 0, quartZ)"));
        assertTrue(source.contains("climate.weirdness()"));
        assertTrue(source.contains("climate.erosion()"));
        assertFalse(source.contains("valueNoise("));
    }

    @Test
    void proceduralSitesUseOceanFloorAndSharedTerrainGates()
            throws IOException {
        String vegetation = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyVegetationFeature.java"
        ));
        String piece = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyWorldStructurePiece.java"
        ));
        String treePolicy = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "TreeSitePolicy.java"
        ));
        String structurePolicy = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "StructureSitePolicy.java"
        ));

        assertTrue(vegetation.contains("Heightmap.Types.OCEAN_FLOOR_WG"));
        assertFalse(vegetation.contains("Heightmap.Types.WORLD_SURFACE_WG"));
        assertTrue(piece.contains("Heightmap.Types.OCEAN_FLOOR_WG"));
        assertTrue(treePolicy.contains("SUPPORT_DEPTH = 2"));
        assertTrue(treePolicy.contains("getFluidState(origin)"));
        assertTrue(structurePolicy.contains(
                "hasDryReplaceableClearance("
        ));
        assertTrue(structurePolicy.contains("DEFAULT_SUPPORT_DEPTH = 2"));
    }

    @Test
    void magicalForestHugeMushroomsUseVanillaConfiguredFeatures()
            throws IOException {
        String vegetation = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyVegetationFeature.java"
        ));

        assertTrue(vegetation.contains("TreeFeatures.HUGE_RED_MUSHROOM"));
        assertTrue(vegetation.contains("TreeFeatures.HUGE_BROWN_MUSHROOM"));
        assertTrue(vegetation.contains("mushroom.place("));
        assertFalse(vegetation.contains("placeRedMushroomCap("));
        assertFalse(vegetation.contains("placeBrownMushroomCap("));
    }
}
