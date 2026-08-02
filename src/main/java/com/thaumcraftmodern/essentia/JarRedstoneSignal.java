package com.thaumcraftmodern.essentia;

import net.minecraft.util.Mth;

/** Analog redstone levels exposed by warded and void essentia jars. */
public final class JarRedstoneSignal {
    private JarRedstoneSignal() {
    }

    public static int forAmount(int amount) {
        int clamped = Mth.clamp(amount, 0, 64);
        if (clamped == 0) return 0;
        if (clamped < 8) return Math.min(5, Mth.ceil(clamped * 5.0F / 7.0F));
        if (clamped < 16) return 6;
        if (clamped < 32) return 7;
        if (clamped < 64) return 8;
        return 10;
    }

    public static int forVoidJar(int amount, boolean overflowing) {
        return overflowing && amount >= 64 ? 11 : forAmount(amount);
    }
}
