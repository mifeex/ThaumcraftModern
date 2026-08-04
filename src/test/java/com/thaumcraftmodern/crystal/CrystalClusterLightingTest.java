package com.thaumcraftmodern.crystal;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalClusterLightingTest {
    @Test
    void clustersHaveNoVisualOcclusionOrShade() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/CrystalClusterBlock.java"));
        assertTrue(source.contains("getVisualShape")
                && source.contains("return Shapes.empty();"));
        assertTrue(source.contains("propagatesSkylightDown")
                && source.contains("return true;"));
        assertTrue(source.contains("getShadeBrightness")
                && source.contains("return 1.0F;"));
    }
}
