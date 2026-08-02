package com.thaumcraftmodern.client.screen;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parser for the lightweight markup embedded in classic Thaumcraft research
 * localization strings.
 *
 * <p>The original content uses block-level {@code <BR>}, {@code <LINE>} and
 * {@code <IMG>resource:u:v:w:h:scale</IMG>} tags. Parsing is intentionally
 * case-insensitive, while unknown or malformed tags remain visible as text so
 * content mistakes do not silently erase paragraphs.</p>
 */
public final class ThaumonomiconMarkup {
    private ThaumonomiconMarkup() {
    }

    public static List<Node> parse(String source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<Node> result = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        String lower = source.toLowerCase(Locale.ROOT);
        int cursor = 0;
        int italicDepth = 0;
        int boldDepth = 0;
        while (cursor < source.length()) {
            if (matchesTag(lower, cursor, "<br>")) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                result.add(new Break());
                cursor += 4;
                continue;
            }
            if (matchesTag(lower, cursor, "<br/>")) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                result.add(new Break());
                cursor += 5;
                continue;
            }
            if (matchesTag(lower, cursor, "<br />")) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                result.add(new Break());
                cursor += 6;
                continue;
            }
            if (matchesTag(lower, cursor, "<line>")) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                result.add(new Divider());
                cursor += 6;
                continue;
            }
            if (matchesTag(lower, cursor, "<hr>")) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                result.add(new Divider());
                cursor += 4;
                continue;
            }
            if (matchesTag(lower, cursor, "<i>")
                    && hasClosingTag(lower, cursor + 3, "</i>")) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                italicDepth++;
                cursor += 3;
                continue;
            }
            if (matchesTag(lower, cursor, "</i>") && italicDepth > 0) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                italicDepth--;
                cursor += 4;
                continue;
            }
            if (matchesTag(lower, cursor, "<b>")
                    && hasClosingTag(lower, cursor + 3, "</b>")) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                boldDepth++;
                cursor += 3;
                continue;
            }
            if (matchesTag(lower, cursor, "</b>") && boldDepth > 0) {
                flushText(result, text, italicDepth > 0, boldDepth > 0);
                boldDepth--;
                cursor += 4;
                continue;
            }
            if (matchesTag(lower, cursor, "<img>")) {
                int close = lower.indexOf("</img>", cursor + 5);
                if (close >= 0) {
                    String descriptor = source.substring(cursor + 5, close).trim();
                    ImageSpec image = ImageSpec.parse(descriptor);
                    if (image != null) {
                        flushText(result, text, italicDepth > 0, boldDepth > 0);
                        result.add(new Image(image));
                    } else {
                        text.append(source, cursor, close + 6);
                    }
                    cursor = close + 6;
                    continue;
                }
            }
            text.append(source.charAt(cursor));
            cursor++;
        }
        flushText(result, text, italicDepth > 0, boldDepth > 0);
        return List.copyOf(result);
    }

    private static boolean hasClosingTag(String lower, int start, String tag) {
        return lower.indexOf(tag, start) >= 0;
    }

    private static boolean matchesTag(String lower, int offset, String tag) {
        return offset + tag.length() <= lower.length()
                && lower.regionMatches(offset, tag, 0, tag.length());
    }

    private static void flushText(
            List<Node> result,
            StringBuilder text,
            boolean italic,
            boolean bold
    ) {
        if (!text.isEmpty()) {
            result.add(new Text(text.toString(), italic, bold));
            text.setLength(0);
        }
    }

    public sealed interface Node permits Text, Break, Divider, Image {
    }

    public record Text(String value, boolean italic, boolean bold) implements Node {
    }

    public record Break() implements Node {
    }

    public record Divider() implements Node {
    }

    public record Image(ImageSpec spec) implements Node {
    }

    public record ImageSpec(
            ResourceLocation texture,
            int sourceX,
            int sourceY,
            int sourceWidth,
            int sourceHeight,
            float scale
    ) {
        private static ImageSpec parse(String descriptor) {
            String[] fields = descriptor.split(":");
            if (fields.length < 6) {
                return null;
            }
            int numericStart = fields.length - 5;
            String resource = String.join(
                    ":",
                    java.util.Arrays.copyOfRange(fields, 0, numericStart)
            );
            ResourceLocation texture = ResourceLocation.tryParse(resource);
            if (texture == null) {
                return null;
            }
            try {
                int sourceX = Integer.parseInt(fields[numericStart]);
                int sourceY = Integer.parseInt(fields[numericStart + 1]);
                int sourceWidth = Integer.parseInt(fields[numericStart + 2]);
                int sourceHeight = Integer.parseInt(fields[numericStart + 3]);
                float scale = Float.parseFloat(fields[numericStart + 4]);
                if (sourceX < 0
                        || sourceY < 0
                        || sourceWidth < 1
                        || sourceHeight < 1
                        || !Float.isFinite(scale)
                        || scale <= 0.0F) {
                    return null;
                }
                return new ImageSpec(
                        texture,
                        sourceX,
                        sourceY,
                        sourceWidth,
                        sourceHeight,
                        scale
                );
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
}
