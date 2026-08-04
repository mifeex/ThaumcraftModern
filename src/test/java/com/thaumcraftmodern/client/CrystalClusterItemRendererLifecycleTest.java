package com.thaumcraftmodern.client.render;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrystalClusterItemRendererLifecycleTest {
    @Test
    void rendererDoesNotCaptureUninitializedSubclassVariant() throws Exception {
        String item = source(
                "item/CrystalClusterItem.java"
        );
        String extensions = source(
                "client/render/CrystalClusterItemClientExtensions.java"
        );
        String renderer = source(
                "client/render/CrystalClusterItemRenderer.java"
        );

        assertTrue(item.contains("CrystalClusterItemClientExtensions.create()"));
        assertFalse(extensions.contains("CrystalClusterVariant variant"));
        assertTrue(renderer.contains(
                "stack.getItem() instanceof CrystalClusterItem clusterItem"
        ));
        assertTrue(renderer.contains("clusterItem.variant()"));
    }

    @Test
    void legacyOpenGlTintIsClampedBeforeModernVertexPacking() {
        assertEquals(1.0F, CrystalClusterRenderer.legacyTint(255));
        assertEquals(1.0F, CrystalClusterRenderer.legacyTint(220));
        assertEquals(126.0F / 220.0F,
                CrystalClusterRenderer.legacyTint(126));
        assertEquals(0.0F, CrystalClusterRenderer.legacyTint(0));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/" + relative
        ));
    }
}
