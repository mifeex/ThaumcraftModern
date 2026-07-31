package com.thaumcraftmodern.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * Faithful modern adapter for TC4's upper-left casting-wand vis dial.
 */
public final class ClientWandVisOverlay {
    static final int DIAL_SIZE = 32;
    static final int MAX_FILL_PIXELS = 30;
    private static final float FIRST_PRIMAL_ANGLE = 75.0F;
    private static final float PRIMAL_ANGLE_STEP = 24.0F;
    private static final float AMOUNT_LABEL_RADIUS = 38.0F;

    private static final ResourceLocation HUD =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/hud.png"
            );

    private ClientWandVisOverlay() {
    }

    public static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.options.hideGui
                || minecraft.screen != null) {
            return;
        }

        ItemStack wand = minecraft.player.getMainHandItem();
        WandState state = WandVisService.state(wand).orElse(null);
        int capacityCentivis = WandVisService.capacityCentivis(wand);
        if (state == null || capacityCentivis <= 0) {
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        ClassicUiRender.drawScaledTexture(
                graphics,
                HUD,
                0,
                0,
                DIAL_SIZE,
                DIAL_SIZE,
                0,
                0,
                64,
                64,
                256,
                256
        );

        int index = 0;
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            renderPrimal(
                    graphics,
                    aspect,
                    state.visCentivis(aspect),
                    capacityCentivis,
                    index
            );
            index++;
        }

        graphics.flush();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().popPose();

        /*
         * Font glyphs must not be submitted through the scaled and rotated
         * branch pose. Modern Minecraft's font renderer does not reproduce
         * TC4's old transformed-font path and produces stretched white glyph
         * fragments. Draw readable labels in screen space instead.
         */
        if (minecraft.player.isShiftKeyDown()) {
            renderAmounts(graphics, minecraft, state);
        }
    }

    private static void renderPrimal(
            GuiGraphics graphics,
            PrimalAspect aspect,
            int amountCentivis,
            int capacityCentivis,
            int index
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(16.0F, 16.0F, 0.0F);

        /*
         * These are the original top-dial transforms from
         * ClientTickEventsFML.renderCastingWandHud:
         * 90 degrees, then -15 + index * 24, then 32 pixels outward.
         */
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(90.0F));
        graphics.pose().mulPose(
                Axis.ZP.rotationDegrees(-15.0F + index * PRIMAL_ANGLE_STEP)
        );
        graphics.pose().translate(0.0F, -32.0F, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);

        int fill = fillPixels(amountCentivis, capacityCentivis);
        if (fill > 0) {
            int color = AspectRegistryRuntime.find(aspect.id())
                    .map(definition -> definition.color())
                    .orElse(0xFFFFFF);
            float red = ((color >>> 16) & 0xFF) / 255.0F;
            float green = ((color >>> 8) & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            graphics.flush();
            graphics.setColor(red, green, blue, 0.8F);
            graphics.blit(
                    HUD,
                    -4,
                    35 - fill,
                    104,
                    0,
                    8,
                    fill,
                    256,
                    256
            );
            graphics.flush();
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        graphics.blit(
                HUD,
                -8,
                -3,
                72,
                0,
                16,
                42,
                256,
                256
        );

        graphics.pose().popPose();
    }

    private static void renderAmounts(
            GuiGraphics graphics,
            Minecraft minecraft,
            WandState state
    ) {
        int index = 0;
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            String amount = formatVis(state.visCentivis(aspect));
            double angle = Math.toRadians(
                    FIRST_PRIMAL_ANGLE + index * PRIMAL_ANGLE_STEP
            );
            int centerX = Math.round(
                    DIAL_SIZE / 2.0F
                            + (float) Math.sin(angle) * AMOUNT_LABEL_RADIUS
            );
            int centerY = Math.round(
                    DIAL_SIZE / 2.0F
                            - (float) Math.cos(angle) * AMOUNT_LABEL_RADIUS
            );
            graphics.drawString(
                    minecraft.font,
                    amount,
                    centerX - minecraft.font.width(amount) / 2,
                    centerY - minecraft.font.lineHeight / 2,
                    0xFFFFFF,
                    true
            );
            index++;
        }
        graphics.flush();
    }

    static int fillPixels(int amountCentivis, int capacityCentivis) {
        if (capacityCentivis <= 0 || amountCentivis <= 0) {
            return 0;
        }
        return Math.min(
                MAX_FILL_PIXELS,
                (int) (
                        MAX_FILL_PIXELS
                                * (amountCentivis / (float) capacityCentivis)
                )
        );
    }

    static String formatVis(int centivis) {
        return Integer.toString(
                Math.max(0, centivis) / WandVisService.CENTIVIS_PER_VIS
        );
    }
}
