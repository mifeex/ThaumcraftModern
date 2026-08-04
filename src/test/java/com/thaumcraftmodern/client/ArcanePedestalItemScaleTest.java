package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArcanePedestalItemScaleTest {
    @Test
    void modernGroundTransformDoesNotDoubleBlockItems() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/ArcanePedestalBlockEntityRenderer.java"
        ));
        assertTrue(renderer.contains("float scale = 1.0F"));
        assertFalse(renderer.contains("? 2.0F : 1.0F"));
    }
}
