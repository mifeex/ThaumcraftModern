package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumometerForegroundDepthTest {
    @Test
    void foregroundLayerPreservesPerspectiveAndRestoresDepthRange()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "ClientRenderEvents.java"
        ));

        assertTrue(source.contains(
                "THAUMOMETER_DEPTH_FAR = 0.05D"
        ));
        assertTrue(source.contains(
                "renderThaumometerInForeground("
        ));
        assertTrue(source.contains(
                "finally {\n            GL11.glDepthRange(0.0D, 1.0D);"
        ));
        assertFalse(source.contains("GL11.glClear("));
        assertFalse(source.contains("RenderSystem.disableDepthTest()"));
    }
}
