package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Pure NBT codec for the payload carried by TC4 filled jar items. */
public record WardedJarContents(@Nullable String aspect, int amount,
        @Nullable String filter, Direction filterFacing) {
    public static Optional<WardedJarContents> read(CompoundTag payload) {
        String aspect = blankToNull(payload.getString("Aspect"));
        String filter = blankToNull(payload.getString("AspectFilter"));
        int amount = Math.max(0, Math.min(EssentiaJarBlockEntity.CAPACITY,
                payload.getInt("Amount")));
        Direction facing = Direction.NORTH;
        int facingIndex = payload.getInt("Facing");
        Direction[] directions = Direction.values();
        if (facingIndex >= 0 && facingIndex < directions.length
                && directions[facingIndex].getAxis().isHorizontal()) {
            facing = directions[facingIndex];
        }
        if (amount > 0 && aspect == null) return Optional.empty();
        return Optional.of(new WardedJarContents(aspect, amount, filter, facing));
    }

    private static @Nullable String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
