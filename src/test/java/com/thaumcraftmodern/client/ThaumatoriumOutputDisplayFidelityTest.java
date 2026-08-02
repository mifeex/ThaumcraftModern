package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumatoriumOutputDisplayFidelityTest {
    @Test
    void frontPanelUsesTheOriginalGroundTransformAndPanelCenter() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ThaumatoriumBlockEntityRenderer.java"));
        String original = Files.readString(Path.of(
                "reference/Thaumcraft-4.2-FOREVA-master/src/main/java/"
                        + "thaumcraft/client/renderers/tile/"
                        + "TileThaumatoriumRenderer.java"));

        assertTrue(source.contains("ItemDisplayContext.GROUND"));
        assertTrue(source.contains("1.42"));
        assertTrue(source.contains("pose.scale(.75F, .75F, .75F)"));
        assertFalse(source.contains("ItemDisplayContext.GUI"));
        assertFalse(source.contains("ItemDisplayContext.FIXED"));
        assertFalse(source.contains("getGameTime() + partialTick"));
        assertTrue(source.contains("machine.formulae()"));
        assertTrue(source.contains("machine.catalyst().isEmpty()"));
        assertTrue(source.contains("/ 40L % formulae.size()"));
        assertFalse(source.contains("machine.selectedRecipe()"));
        assertFalse(source.contains("machine.displayedRecipe()"));
        assertTrue(original.contains("tile.recipeHash.isEmpty()"));
        assertTrue(original.contains(
                "/ 40L % tile.recipeHash.size()"));
    }
}
