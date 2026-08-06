package com.thaumcraftmodern.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/** Compact TC-style aspect strip: icons, corner amounts and a dark backing. */
public final class ClientAspectTooltipComponent
        implements ClientTooltipComponent {
    private static final int ICON_SIZE = 16;
    private static final int MAX_COLUMNS = 8;
    private static final int CELL_SIZE = 18;
    private static final int PADDING = 2;
    private static final int BACKGROUND = 0xB0100010;
    private final AspectTooltipComponent component;

    public ClientAspectTooltipComponent(AspectTooltipComponent component) {
        this.component = component;
    }

    @Override
    public int getHeight() {
        return rows() * CELL_SIZE + PADDING * 2;
    }

    @Override
    public int getWidth(Font font) {
        return Math.min(MAX_COLUMNS, component.aspects().size()) * CELL_SIZE
                + PADDING * 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int width = getWidth(font);
        int height = getHeight();
        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        for (int index = 0; index < component.aspects().size(); index++) {
            AspectTooltipComponent.Entry entry = component.aspects().get(index);
            int column = index % MAX_COLUMNS;
            int row = index / MAX_COLUMNS;
            int iconX = x + PADDING + column * CELL_SIZE + 1;
            int iconY = y + PADDING + row * CELL_SIZE + 1;
            // Use the shared 32x32 aspect renderer. Apart from sampling the
            // complete original mask, it flushes and restores its tint/blend
            // state before returning. A tooltip must not leave global render
            // state behind for the scan notification drawn later in the same
            // frame.
            ClassicUiRender.drawAspectTag(graphics, font, entry.icon(), iconX,
                    iconY, ICON_SIZE, entry.color(), entry.amount());
        }
    }

    private int rows() {
        return (component.aspects().size() + MAX_COLUMNS - 1) / MAX_COLUMNS;
    }
}
