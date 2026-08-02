package com.thaumcraftmodern.essentia;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumatoriumTubeRenderingFidelityTest {
    @Test
    void machineAcceptsTubesAndOnlyOutputsCancelledCraftRefunds()
            throws Exception {
        String modern = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "ThaumatoriumBlockEntity.java"));
        String original = Files.readString(Path.of(
                "reference/Thaumcraft-4.2-FOREVA-master/src/main/java/"
                        + "thaumcraft/common/tiles/TileThaumatorium.java"));

        assertTrue(modern.contains(
                "boolean isConnectable(Direction side) { return side != facing(); }"));
        assertTrue(modern.contains(
                "return side != facing() && !refundingEssentia();"));
        assertTrue(modern.contains(
                "return side != facing() && refundingEssentia();"));
        assertFalse(modern.contains("boolean renderExtendedTube()"));
        assertTrue(original.contains(
                "boolean renderExtendedTube() {\n        return false;\n    }"));
    }
}
