package com.thaumcraftmodern.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Rendering helpers for the original Thaumcraft atlases.
 *
 * <p>The classic assets frequently store 32px icons or several independent
 * panels in one texture. Drawing a smaller destination with vanilla's blit
 * overload crops the source instead of scaling it, so every scaled draw goes
 * through this helper.</p>
 */
public final class ClassicUiRender {
    private ClassicUiRender() {
    }

    public static void drawScaledTexture(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int destinationWidth,
            int destinationHeight,
            int sourceX,
            int sourceY,
            int sourceWidth,
            int sourceHeight,
            int textureWidth,
            int textureHeight
    ) {
        if (destinationWidth <= 0 || destinationHeight <= 0
                || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(
                destinationWidth / (float) sourceWidth,
                destinationHeight / (float) sourceHeight,
                1.0F
        );
        graphics.blit(
                texture,
                0,
                0,
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                textureWidth,
                textureHeight
        );
        graphics.pose().popPose();
    }

    public static void drawHorizontallyFlippedScaledTexture(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int destinationWidth,
            int destinationHeight,
            int sourceX,
            int sourceY,
            int sourceWidth,
            int sourceHeight,
            int textureWidth,
            int textureHeight
    ) {
        if (destinationWidth <= 0 || destinationHeight <= 0
                || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x + destinationWidth, y, 0.0F);
        graphics.pose().scale(
                -destinationWidth / (float) sourceWidth,
                destinationHeight / (float) sourceHeight,
                1.0F
        );
        graphics.blit(
                texture,
                0,
                0,
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                textureWidth,
                textureHeight
        );
        graphics.pose().popPose();
    }

    public static void drawItemCentered(
            GuiGraphics graphics,
            ItemStack stack,
            int centerX,
            int centerY,
            int size
    ) {
        if (stack.isEmpty() || size <= 0) {
            return;
        }
        float scale = size / 16.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(
                centerX - size / 2.0F,
                centerY - size / 2.0F,
                0.0F
        );
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    public static void drawTintedItemCentered(
            GuiGraphics graphics,
            ItemStack stack,
            int centerX,
            int centerY,
            int size,
            int argbColor
    ) {
        float alpha = ((argbColor >>> 24) & 0xFF) / 255.0F;
        float red = ((argbColor >>> 16) & 0xFF) / 255.0F;
        float green = ((argbColor >>> 8) & 0xFF) / 255.0F;
        float blue = (argbColor & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, alpha);
        try {
            drawItemCentered(graphics, stack, centerX, centerY, size);
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static void drawAspect(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int size,
            int rgbColor
    ) {
        drawAspect(graphics, texture, x, y, size, rgbColor, 1.0F);
    }

    public static void drawAspect(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int size,
            int rgbColor,
            float alpha
    ) {
        int alphaByte = Math.max(
                0,
                Math.min(255, Math.round(alpha * 255.0F))
        );
        drawTintedScaledTexture(
                graphics,
                texture,
                x,
                y,
                size,
                size,
                0,
                0,
                32,
                32,
                32,
                32,
                (alphaByte << 24) | (rgbColor & 0x00FFFFFF)
        );
    }

    /**
     * TC4's research expertise component preview used
     * {@code GL_SRC_ALPHA, GL_ONE}. The additive pass prevents the black
     * circular field in primal masks from covering the soft recipe backdrop.
     */
    public static void drawAspectAdditive(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int size,
            int rgbColor
    ) {
        drawTintedScaledTexture(
                graphics,
                texture,
                x,
                y,
                size,
                size,
                0,
                0,
                32,
                32,
                32,
                32,
                0xFF000000 | (rgbColor & 0x00FFFFFF),
                true
        );
    }

    public static void drawTintedScaledTexture(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int destinationWidth,
            int destinationHeight,
            int sourceX,
            int sourceY,
            int sourceWidth,
            int sourceHeight,
            int textureWidth,
            int textureHeight,
            int argbColor
    ) {
        drawTintedScaledTexture(
                graphics,
                texture,
                x,
                y,
                destinationWidth,
                destinationHeight,
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                textureWidth,
                textureHeight,
                argbColor,
                false
        );
    }

    private static void drawTintedScaledTexture(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int destinationWidth,
            int destinationHeight,
            int sourceX,
            int sourceY,
            int sourceWidth,
            int sourceHeight,
            int textureWidth,
            int textureHeight,
            int argbColor,
            boolean additive
    ) {
        float alpha = ((argbColor >>> 24) & 0xFF) / 255.0F;
        float red = ((argbColor >>> 16) & 0xFF) / 255.0F;
        float green = ((argbColor >>> 8) & 0xFF) / 255.0F;
        float blue = (argbColor & 0xFF) / 255.0F;

        graphics.flush();
        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE
            );
        } else {
            RenderSystem.defaultBlendFunc();
        }
        graphics.setColor(red, green, blue, alpha);
        drawScaledTexture(
                graphics,
                texture,
                x,
                y,
                destinationWidth,
                destinationHeight,
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                textureWidth,
                textureHeight
        );
        graphics.flush();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
