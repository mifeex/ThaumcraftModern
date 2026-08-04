package com.thaumcraftmodern.client;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcaneWorkbenchWandRendererFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");

    @Test
    void emptyWorkbenchKeepsStaticModelAndInsertedToolUsesItsNbtRenderer()
            throws Exception {
        String blockRenderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "ArcaneWorkbenchBlockEntityRenderer.java"
        );
        String wandRenderer = source(
                "com/thaumcraftmodern/client/render/ClassicWandItemRenderer.java"
        );
        assertTrue(blockRenderer.contains(
                "if (stack.isEmpty() || !(stack.getItem() instanceof WandItem))"
        ));
        assertTrue(blockRenderer.contains(
                "wandRenderer.renderOnArcaneWorkbench("
        ));
        assertTrue(wandRenderer.contains(
                "WandVisService.state(stack).orElse(null)"
        ));
        assertTrue(wandRenderer.contains(
                "ArcaneWorkbenchWandTransform.apply(poseStack, wand.form())"
        ));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(JAVA.resolve(relative));
    }
}
