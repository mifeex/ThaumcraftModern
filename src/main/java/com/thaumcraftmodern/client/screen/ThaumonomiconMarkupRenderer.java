package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.client.ClassicUiRender;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class ThaumonomiconMarkupRenderer {
    private static final float[] TEXT_SCALES = {1.0F, 0.9F, 0.8F, 0.72F, 0.65F};
    private static final int TEXT_COLOR = 0x342117;
    private static final int DIVIDER_COLOR = 0x8A3E281A;
    private static final int LEGACY_TEXTURE_SIZE = 256;

    private ThaumonomiconMarkupRenderer() {
    }

    public static void render(
            GuiGraphics graphics,
            Font font,
            String markup,
            int x,
            int y,
            int width,
            int bottom
    ) {
        List<ThaumonomiconMarkup.Node> nodes = ThaumonomiconMarkup.parse(markup);
        float textScale = selectScale(font, nodes, width, bottom - y);
        graphics.enableScissor(x, y, x + width, bottom);
        renderNodes(graphics, font, nodes, x, y, width, textScale, true);
        graphics.disableScissor();
    }

    private static float selectScale(
            Font font,
            List<ThaumonomiconMarkup.Node> nodes,
            int width,
            int availableHeight
    ) {
        for (float scale : TEXT_SCALES) {
            if (renderNodes(null, font, nodes, 0, 0, width, scale, false)
                    <= availableHeight) {
                return scale;
            }
        }
        return TEXT_SCALES[TEXT_SCALES.length - 1];
    }

    /**
     * @return rendered height in unscaled GUI pixels
     */
    private static int renderNodes(
            GuiGraphics graphics,
            Font font,
            List<ThaumonomiconMarkup.Node> nodes,
            int x,
            int y,
            int width,
            float textScale,
            boolean draw
    ) {
        int cursor = 0;
        int lineHeight = Math.max(1, Math.round((font.lineHeight + 1) * textScale));
        boolean lineHasContent = false;
        MutableComponent paragraph = Component.empty();

        for (ThaumonomiconMarkup.Node node : nodes) {
            if (node instanceof ThaumonomiconMarkup.Text text) {
                if (text.value().isEmpty()) {
                    continue;
                }
                paragraph.append(Component.literal(text.value()).withStyle(style -> style
                        .withItalic(text.italic())
                        .withBold(text.bold())));
                continue;
            }

            TextFlow flow = renderParagraph(
                    graphics, font, paragraph, x, y, width, textScale, draw, cursor, lineHasContent
            );
            cursor = flow.cursor();
            lineHasContent = flow.lineHasContent();
            paragraph = Component.empty();

            if (node instanceof ThaumonomiconMarkup.Break) {
                cursor += lineHeight;
                lineHasContent = false;
                continue;
            }

            if (lineHasContent) {
                cursor += lineHeight;
                lineHasContent = false;
            }

            if (node instanceof ThaumonomiconMarkup.Divider) {
                cursor += 3;
                if (draw && graphics != null) {
                    int lineX = x + 8;
                    int lineWidth = Math.max(1, width - 16);
                    graphics.fill(
                            lineX,
                            y + cursor,
                            lineX + lineWidth,
                            y + cursor + 1,
                            DIVIDER_COLOR
                    );
                    int center = x + width / 2;
                    graphics.fill(
                            center - 1,
                            y + cursor - 1,
                            center + 2,
                            y + cursor + 2,
                            DIVIDER_COLOR
                    );
                }
                cursor += 5;
                continue;
            }

            if (node instanceof ThaumonomiconMarkup.Image image) {
                ImageMetrics metrics = imageMetrics(image.spec(), width);
                if (draw && graphics != null) {
                    int imageX = x + (width - metrics.width()) / 2;
                    ClassicUiRender.drawScaledTexture(
                            graphics,
                            image.spec().texture(),
                            imageX,
                            y + cursor,
                            metrics.width(),
                            metrics.height(),
                            image.spec().sourceX(),
                            image.spec().sourceY(),
                            image.spec().sourceWidth(),
                            image.spec().sourceHeight(),
                            LEGACY_TEXTURE_SIZE,
                            LEGACY_TEXTURE_SIZE
                    );
                }
                cursor += metrics.height() + 2;
            }
        }
        TextFlow flow = renderParagraph(
                graphics, font, paragraph, x, y, width, textScale, draw, cursor, lineHasContent
        );
        cursor = flow.cursor();
        lineHasContent = flow.lineHasContent();
        return cursor + (lineHasContent ? lineHeight : 0);
    }

    private static TextFlow renderParagraph(
            GuiGraphics graphics,
            Font font,
            Component paragraph,
            int x,
            int y,
            int width,
            float textScale,
            boolean draw,
            int cursor,
            boolean lineHasContent
    ) {
        if (paragraph.getString().isEmpty()) {
            return new TextFlow(cursor, lineHasContent);
        }
        int lineHeight = Math.max(1, Math.round((font.lineHeight + 1) * textScale));
        List<FormattedCharSequence> lines = font.split(
                paragraph,
                Math.max(1, (int) (width / textScale))
        );
        for (FormattedCharSequence line : lines) {
            if (lineHasContent) {
                cursor += lineHeight;
            }
            if (draw && graphics != null) {
                graphics.pose().pushPose();
                graphics.pose().translate(x, y + cursor, 0.0F);
                graphics.pose().scale(textScale, textScale, 1.0F);
                graphics.drawString(font, line, 0, 0, TEXT_COLOR, false);
                graphics.pose().popPose();
            }
            lineHasContent = true;
        }
        return new TextFlow(cursor, lineHasContent);
    }

    private static ImageMetrics imageMetrics(
            ThaumonomiconMarkup.ImageSpec image,
            int availableWidth
    ) {
        float scale = image.scale();
        int width = Math.max(1, Math.round(image.sourceWidth() * scale));
        int height = Math.max(1, Math.round(image.sourceHeight() * scale));
        if (width > availableWidth) {
            float fit = availableWidth / (float) width;
            width = availableWidth;
            height = Math.max(1, Math.round(height * fit));
        }
        return new ImageMetrics(width, height);
    }

    private record ImageMetrics(int width, int height) {
    }

    private record TextFlow(int cursor, boolean lineHasContent) {
    }
}
