package com.thaumcraftmodern.worldgen;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class SilverwoodFlowerPriorityTest {
    @Test
    void shimmerleafNeverReplacesSilverwoodGeometry() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "SilverwoodTreeFeature.java"));

        assertTrue(source.contains(
                "if (level.isEmptyBlock(surface)\n"
                        + "                        && shimmerleaf.canSurvive(level, surface))"));
    }
}
