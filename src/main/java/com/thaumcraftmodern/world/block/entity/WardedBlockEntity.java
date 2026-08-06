package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Owner and original block state for a focus-created ward. */
public final class WardedBlockEntity extends BlockEntity {
    private BlockState stored;
    private UUID owner;
    public WardedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARDED_BLOCK.get(), pos, state);
    }
    public void configure(BlockState stored, UUID owner) {
        this.stored = stored; this.owner = owner; setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public BlockState stored() { return stored; }
    public boolean ownedBy(UUID id) { return owner != null && owner.equals(id); }
    public void restore() { if (level != null && stored != null) level.setBlock(worldPosition, stored, 3); }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (stored != null) tag.put("Stored", NbtUtils.writeBlockState(stored));
        if (owner != null) tag.putUUID("Owner", owner);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Stored")) stored = NbtUtils.readBlockState(
                level != null ? level.holderLookup(Registries.BLOCK)
                        : net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),
                tag.getCompound("Stored"));
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
