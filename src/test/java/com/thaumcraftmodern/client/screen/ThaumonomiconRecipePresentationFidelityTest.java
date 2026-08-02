package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconRecipePresentationFidelityTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconScreen.java"
    );
    private static final Path OUTPUT_RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconRecipeOutputRenderer.java"
    );
    private static final Path OVERLAY = Path.of(
            "src/main/resources/assets/thaumcraftmodern/textures/gui/"
                    + "gui_researchbook_overlay.png"
    );

    @Test
    void usesExactOriginalResearchBookOverlay() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(OVERLAY));
        assertEquals(
                "40b5aeea15fd2003f579dc717accebb0348dafa6837789f51e80e80bfbf22c58",
                HexFormat.of().formatHex(digest)
        );
    }

    @Test
    void sharesOneOutputPresentationAcrossRecipeKinds() throws Exception {
        String screen = Files.readString(SCREEN);
        assertEquals(
                3,
                occurrences(
                        screen,
                        "ThaumonomiconRecipeOutputRenderer.render("
                )
        );

        String renderer = Files.readString(OUTPUT_RENDERER);
        assertTrue(renderer.contains("static final int WIDTH = 112"));
        assertTrue(renderer.contains("static final int HEIGHT = 34"));
        assertTrue(renderer.contains("static final int ITEM_OFFSET_X = 48"));
        assertTrue(renderer.contains("static final int ITEM_OFFSET_Y = 8"));
        assertTrue(renderer.contains("graphics.renderItemDecorations("));
        assertFalse(renderer.contains("graphics.renderTooltip("));
        assertTrue(screen.contains("renderItemLinkTooltip("));
        assertTrue(screen.contains("tooltip.add(hovered.stack().getHoverName())"));
    }

    @Test
    void crucibleUsesOriginalCauldronAndArrowAtlasRegions()
            throws Exception {
        String screen = Files.readString(SCREEN);
        assertTrue(screen.contains(
                "ThaumonomiconCrucibleRecipeLayout.CAULDRON_TOP"
        ));
        assertTrue(screen.contains(
                "ThaumonomiconCrucibleRecipeLayout.CONTENT_OFFSET_Y"
        ));
        assertTrue(screen.contains(
                "ThaumonomiconCrucibleRecipeLayout.ARROW_Y"
        ));
        assertTrue(screen.contains(
                "ThaumonomiconAspectCostRenderer.renderCrucibleGrid("
        ));
        assertTrue(screen.contains(
                ".sorted(Comparator.comparing(AspectCost::aspectId))"
        ));
    }

    @Test
    void nonGridTransformationsUseOriginalSmeltingArrow()
            throws Exception {
        String screen = Files.readString(SCREEN);
        assertTrue(screen.contains("renderTransformationRecipe("));
        assertTrue(screen.contains(
                "ThaumonomiconTransformationRecipeLayout.OVERLAY_SOURCE_Y"
        ));
        assertTrue(screen.contains(
                "recipe instanceof AbstractCookingRecipe"
        ));
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = text.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }
}
