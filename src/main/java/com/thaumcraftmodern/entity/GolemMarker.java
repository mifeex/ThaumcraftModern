package com.thaumcraftmodern.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

/** Server-authoritative TC4 bell marker: block, clicked side and optional Ordo color. */
public record GolemMarker(BlockPos pos, Direction side, byte color) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Pos", pos.asLong());
        tag.putByte("Side", (byte) side.get3DDataValue());
        tag.putByte("Color", color);
        return tag;
    }

    public static GolemMarker load(CompoundTag tag) {
        return new GolemMarker(BlockPos.of(tag.getLong("Pos")),
                Direction.from3DDataValue(tag.getByte("Side")), tag.getByte("Color"));
    }
}
