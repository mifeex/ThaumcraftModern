package com.thaumcraftmodern.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/** Faithful modern adapter for TC4's runic shield layer over the health bar. */
public final class ClientRunicShieldOverlay {
    static final int HEALTH_BAR_LEFT_OFFSET = 91;
    static final int HEALTH_BAR_BOTTOM_OFFSET = 39;
    static final int ICON_SPACING = 8;
    private static final int ATLAS_SIZE = 256;
    private static final ResourceLocation PARTICLES = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/particles.png"
    );

    private ClientRunicShieldOverlay() {
    }

    public static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int width,
            int height
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        int charge = ClientRunicShieldState.charge();
        int maximum = ClientRunicShieldState.maximum();
        if (minecraft.player == null
                || minecraft.player.isCreative()
                || minecraft.options.hideGui
                || charge <= 0
                || maximum <= 0) {
            return;
        }

        int icons = visibleIconCount(charge, maximum);
        int left = barLeft(width);
        int top = barTop(height);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int index = 0; index < icons; index++) {
            int x = left + index * ICON_SPACING;
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(
                    PARTICLES,
                    x,
                    top,
                    160,
                    16,
                    9,
                    9,
                    ATLAS_SIZE,
                    ATLAS_SIZE
            );

            float pulse = 0.6F + (float) Math.sin(
                    minecraft.player.tickCount / 4.0F + index
            ) * 0.4F;
            graphics.flush();
            graphics.setColor(1.0F, 0.75F, 0.24F, pulse);
            ClassicUiRender.drawScaledTexture(
                    graphics,
                    PARTICLES,
                    x,
                    top,
                    8,
                    8,
                    index * 16,
                    96,
                    16,
                    16,
                    ATLAS_SIZE,
                    ATLAS_SIZE
            );
        }
        graphics.flush();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    static int visibleIconCount(int charge, int maximum) {
        if (charge <= 0 || maximum <= 0) {
            return 0;
        }
        return (int) Math.ceil(charge / (float) maximum * 10.0F);
    }

    static int barLeft(int screenWidth) {
        return screenWidth / 2 - HEALTH_BAR_LEFT_OFFSET;
    }

    static int barTop(int screenHeight) {
        return screenHeight - HEALTH_BAR_BOTTOM_OFFSET;
    }
}
