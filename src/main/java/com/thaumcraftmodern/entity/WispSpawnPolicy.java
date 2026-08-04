package com.thaumcraftmodern.entity;

import net.minecraft.world.Difficulty;

/** TC4 wisps only require non-peaceful difficulty; they do not use monster light rules. */
public final class WispSpawnPolicy {
    private WispSpawnPolicy() {
    }

    public static boolean allows(Difficulty difficulty) {
        return difficulty != Difficulty.PEACEFUL;
    }
}
