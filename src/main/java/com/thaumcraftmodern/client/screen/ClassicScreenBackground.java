package com.thaumcraftmodern.client.screen;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Shared world dimming behind classic Thaumcraft screens.
 */
public final class ClassicScreenBackground {
    public static final float ALPHA = 0.6F;
    private static final int COLOR = 0x99000000;

    private ClassicScreenBackground() {
    }

    public static void render(GuiGraphics graphics, int width, int height) {
        graphics.fill(0, 0, width, height, COLOR);
    }
}
