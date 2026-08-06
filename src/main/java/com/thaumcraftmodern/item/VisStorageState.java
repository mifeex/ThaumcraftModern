package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/** Bootstrap-free arithmetic and serialization rules for vis accessories. */
public final class VisStorageState {
    public static final String VERSION_TAG = "Version";
    public static final int SERIAL_VERSION = 1;

    private VisStorageState() {
    }

    public static int capacityCentivis(int capacityVis) {
        if (capacityVis < 0) {
            throw new IllegalArgumentException("capacity vis cannot be negative");
        }
        return Math.multiplyExact(capacityVis, WandVisService.CENTIVIS_PER_VIS);
    }

    public static int visCentivis(CompoundTag storage, PrimalAspect aspect) {
        Objects.requireNonNull(storage, "storage");
        return Math.max(0, storage.getInt(
                Objects.requireNonNull(aspect, "aspect").id()));
    }

    public static int addCentivis(CompoundTag storage, PrimalAspect aspect,
            int amount, int capacityCentivis) {
        if (amount < 0) {
            throw new IllegalArgumentException("added centivis cannot be negative");
        }
        int stored = Math.min(capacityCentivis,
                visCentivis(storage, aspect));
        int accepted = Math.min(capacityCentivis - stored, amount);
        if (accepted > 0) storage.putInt(aspect.id(), stored + accepted);
        return accepted;
    }

    public static int removeCentivis(CompoundTag storage, PrimalAspect aspect,
            int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("removed centivis cannot be negative");
        }
        int stored = visCentivis(storage, aspect);
        int removed = Math.min(stored, amount);
        if (removed > 0) storage.putInt(aspect.id(), stored - removed);
        return removed;
    }

    public static CompoundTag initialize(CompoundTag storage) {
        Objects.requireNonNull(storage, "storage");
        if (!storage.contains(VERSION_TAG)) {
            storage.putInt(VERSION_TAG, SERIAL_VERSION);
        }
        return storage;
    }
}
