package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.menu.AlchemicalFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class AlchemicalFurnaceScreen
        extends AbstractContainerScreen<AlchemicalFurnaceMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/gui/gui_alchemyfurnace.png");

    public AlchemicalFurnaceScreen(AlchemicalFurnaceMenu menu,
            Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick) {
        ClassicScreenBackground.render(graphics, width, height);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick,
            int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0,
                imageWidth, imageHeight, 256, 256);
        if (menu.burning()) {
            int burn = menu.burnScaled(20);
            graphics.blit(TEXTURE, leftPos + 80, topPos + 26 + 20 - burn,
                    176, 20 - burn, 16, burn, 256, 256);
        }
        int cook = menu.cookScaled(46);
        graphics.blit(TEXTURE, leftPos + 106, topPos + 13 + 46 - cook,
                216, 46 - cook, 9, cook, 256, 256);
        int contents = menu.contentsScaled(48);
        graphics.blit(TEXTURE, leftPos + 61, topPos + 12 + 48 - contents,
                200, 48 - contents, 8, contents, 256, 256);
        graphics.blit(TEXTURE, leftPos + 60, topPos + 8,
                232, 0, 10, 55, 256, 256);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // TC4 intentionally draws no foreground labels on this screen.
    }
}
