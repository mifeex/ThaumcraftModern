package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.menu.PechMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;

public final class PechScreen extends AbstractContainerScreen<PechMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/gui/gui_pech.png"
    );

    public PechScreen(
            PechMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = 175;
        imageHeight = 232;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        ClassicScreenBackground.render(graphics, width, height);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(
                BACKGROUND,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight,
                256,
                256
        );
        if (menu.canTrade()) {
            graphics.blit(
                    BACKGROUND,
                    leftPos + 67,
                    topPos + 24,
                    176,
                    0,
                    25,
                    25,
                    256,
                    256
            );
        }
        graphics.flush();
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.canTrade()
                && mouseX >= leftPos + 67
                && mouseX < leftPos + 92
                && mouseY >= topPos + 24
                && mouseY < topPos + 49
                && minecraft != null
                && minecraft.gameMode != null
                && minecraft.player != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    PechMenu.TRADE_BUTTON
            );
            minecraft.player.level().playLocalSound(
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    ModSounds.PECH_DICE.get(),
                    SoundSource.PLAYERS,
                    0.5F,
                    0.95F + minecraft.player.getRandom().nextFloat() * 0.1F,
                    false
            );
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
