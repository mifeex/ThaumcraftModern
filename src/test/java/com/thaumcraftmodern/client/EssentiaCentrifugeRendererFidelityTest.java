package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EssentiaCentrifugeRendererFidelityTest {
    @Test
    void modernModelPartIsNotScaledDownTwice() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EssentiaCentrifugeBlockEntityRenderer.java"));

        assertTrue(renderer.contains("pose.translate(.5, .5, .5)"));
        assertFalse(renderer.contains("pose.scale(1 / 16.0F"));
    }
}
