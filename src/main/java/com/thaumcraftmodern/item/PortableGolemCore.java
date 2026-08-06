package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.GolemCoreType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/** Bootstrap-free reader for the picked-up golem NBT used by inventory tooltips. */
final class PortableGolemCore {
    private PortableGolemCore() {}

    static GolemCoreType read(CompoundTag root) {
        if (root == null || !root.contains("GolemData", Tag.TAG_COMPOUND)) return null;
        CompoundTag data = root.getCompound("GolemData");
        return data.contains("Core", Tag.TAG_INT)
                ? GolemCoreType.byLegacyId(data.getInt("Core"))
                : null;
    }
}
