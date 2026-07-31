package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconCategoryTabFidelityTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconScreen.java"
    );

    @Test
    void categorySwitchUsesClassicFramesInsetAndClack() throws Exception {
        String source = Files.readString(SCREEN);
        assertTrue(source.contains(
                "public static final int CATEGORY_TAB_SELECTED_SOURCE_X = 152"
        ));
        assertTrue(source.contains(
                "public static final int CATEGORY_TAB_INACTIVE_SOURCE_X = 176"
        ));
        assertTrue(source.contains(
                "public static final int CATEGORY_TAB_INACTIVE_OVERLAY_SOURCE_X = 200"
        ));
        for (String control : new String[]{
                "public static final int CATEGORY_TAB_SIZE",
                "public static final int CATEGORY_ICON_SIZE",
                "public static final int CATEGORY_TAB_LEFT_X",
                "public static final int CATEGORY_TAB_RIGHT_X",
                "public static final int CATEGORY_TAB_START_Y",
                "public static final int CATEGORY_TAB_Y_STEP",
                "public static final int CATEGORY_ICON_X_OFFSET",
                "public static final int CATEGORY_ICON_Y_OFFSET",
                "public static final int ACTIVE_CATEGORY_ICON_X_OFFSET",
                "public static final int CATEGORY_TAB_ATLAS_SIZE",
                "public static final int INACTIVE_CATEGORY_TAB_X_OFFSET",
                "public static final int ACTIVE_CATEGORY_TAB_X_OFFSET",
                "public static final long CATEGORY_TAB_SWITCH_ANIMATION_MS",
                "public static final double CATEGORY_TAB_SWITCH_EASE_POWER",
                "public static final float CATEGORY_TAB_SWITCH_SOUND_PITCH",
                "public static final float CATEGORY_TAB_SWITCH_SOUND_VOLUME"
        }) {
            assertTrue(source.contains(control), control);
        }
        assertTrue(source.contains("ModSounds.CAMERA_CLACK.get()"));
        assertTrue(source.contains(
                "categoryTab(index, categories.get(index))"
        ));
        assertTrue(source.contains(
                "? -ACTIVE_CATEGORY_ICON_X_OFFSET\n"
                        + "                        "
                        + ": ACTIVE_CATEGORY_ICON_X_OFFSET"
        ));
    }

    @Test
    void woodenFrameMasksTabsAndTheirIcons() throws Exception {
        String source = Files.readString(SCREEN);
        int treeStart = source.indexOf(
                "private void renderResearchTree("
        );
        int treeEnd = source.indexOf(
                "private static int researchFrameSourceX",
                treeStart
        );
        String renderTree = source.substring(treeStart, treeEnd);
        assertTrue(
                renderTree.indexOf("renderCategoryTabs(graphics);")
                        < renderTree.indexOf(
                        "graphics.blit(\n"
                                + "                RESEARCH_FRAME"
                )
        );
        assertTrue(renderTree.contains(
                "mask every tucked-under part of\n"
                        + "         * both the tab and its icon"
        ));
    }
}
