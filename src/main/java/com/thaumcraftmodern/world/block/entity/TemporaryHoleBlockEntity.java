package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Saves and restores one block displaced by the portable-hole focus. */
public final class TemporaryHoleBlockEntity extends BlockEntity {
    private BlockState stored;
    private long restoreAt;

    public TemporaryHoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEMPORARY_HOLE.get(), pos, state);
    }
    public void configure(BlockState stored, long restoreAt) {
        this.stored = stored;
        this.restoreAt = restoreAt;
        setChanged();
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  TemporaryHoleBlockEntity hole) {
        if (hole.stored != null && level.getGameTime() >= hole.restoreAt)
            level.setBlock(pos, hole.stored, 3);
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (stored != null) tag.put("Stored", NbtUtils.writeBlockState(stored));
        tag.putLong("RestoreAt", restoreAt);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Stored")) stored = NbtUtils.readBlockState(
                level != null ? level.holderLookup(Registries.BLOCK)
                        : net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),
                tag.getCompound("Stored"));
        restoreAt = tag.getLong("RestoreAt");
    }
}
