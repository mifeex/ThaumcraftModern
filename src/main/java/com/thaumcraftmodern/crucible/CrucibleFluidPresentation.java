package com.thaumcraftmodern.crucible;

/**
 * Pure TC4 Crucible liquid presentation formulas.
 *
 * <p>Modern water sprites are neutral and receive their blue color from a
 * biome tint. TC4 instead rendered a pre-colored water sprite and multiplied
 * it by a saturation modifier derived from the total essentia amount. The
 * modern port therefore applies that original modifier to the biome water
 * color.</p>
 */
public final class CrucibleFluidPresentation {
    private CrucibleFluidPresentation() {
    }

    public static float height(
            int water,
            int capacity,
            int essentiaAmount,
            int maxEssentia
    ) {
        float base = 0.3F + 0.5F
                * ((float) water / (float) capacity);
        float height = base
                + (float) essentiaAmount / (float) maxEssentia
                * (1.0F - base);
        if (height > 1.0F) {
            return 1.001F;
        }
        if (height == 1.0F) {
            return 0.9999F;
        }
        return height;
    }

    public static int color(
            int baseWaterColor,
            int essentiaAmount,
            int maxEssentia
    ) {
        float raw = (float) essentiaAmount / (float) maxEssentia;
        float recolor = raw > 0.0F ? 0.5F + raw / 2.0F : 0.0F;
        int modifierRed = channel(1.0F - recolor / 3.0F);
        int modifierGreen = channel(1.0F - recolor);
        int modifierBlue = channel(1.0F - recolor / 2.0F);
        int red = multiplyChannel(
                (baseWaterColor >> 16) & 0xFF,
                modifierRed
        );
        int green = multiplyChannel(
                (baseWaterColor >> 8) & 0xFF,
                modifierGreen
        );
        int blue = multiplyChannel(
                baseWaterColor & 0xFF,
                modifierBlue
        );
        return red << 16 | green << 8 | blue;
    }

    private static int channel(float value) {
        return (int) (Math.max(0.0F, Math.min(1.0F, value)) * 255.0F);
    }

    private static int multiplyChannel(int base, int modifier) {
        return base * modifier / 255;
    }
}
