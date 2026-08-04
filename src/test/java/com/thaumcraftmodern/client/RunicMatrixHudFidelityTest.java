package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RunicMatrixHudFidelityTest {
    @Test
    void matrixUsesSharedAspectContainerHudForRemainingEssentia()
            throws Exception {
        String registry = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/AspectContainerHudRegistry.java"
        ));
        assertTrue(registry.contains(
                "register(RunicMatrixBlockEntity.class"));
        assertTrue(registry.contains("matrix.remainingEssentia()"));
        assertTrue(registry.contains(
                "ClientAspectContainerReadout.crucibleContents("));
    }
}
