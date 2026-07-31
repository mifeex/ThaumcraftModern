package com.thaumcraftmodern.essentia;

import org.jetbrains.annotations.Nullable;

/** Furnace-facing contract implemented by distillation stages. */
public interface AlembicStorage {
    @Nullable String storedAspect();

    @Nullable String filterAspect();

    int storedAmount();

    int capacity();

    int acceptFromFurnace(String aspect, int amount);
}
