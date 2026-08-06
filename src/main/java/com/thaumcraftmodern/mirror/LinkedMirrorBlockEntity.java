package com.thaumcraftmodern.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

/** Shared reciprocal-link contract for both TC4 mirror families. */
public abstract class LinkedMirrorBlockEntity extends BlockEntity {
    private @Nullable MirrorLink link;
    private int linkCheckInterval = 40;
    private int ticks;

    protected LinkedMirrorBlockEntity(BlockEntityType<?> type, BlockPos pos,
            BlockState state) {
        super(type, pos, state);
    }

    public @Nullable MirrorLink link() {
        return link;
    }

    public boolean linked() {
        return link != null;
    }

    public void setDestination(MirrorLink destination) {
        link = destination;
        sync();
    }

    public boolean restoreLink() {
        if (!(level instanceof ServerLevel local) || link == null) return false;
        ServerLevel remoteLevel = link.level(local.getServer());
        if (remoteLevel == null || !remoteLevel.hasChunkAt(link.position())) return false;
        if (!(remoteLevel.getBlockEntity(link.position())
                instanceof LinkedMirrorBlockEntity remote)
                || remote.getClass() != getClass()
                || remote.validReciprocalLink()) return false;
        remote.link = MirrorLink.of(local, worldPosition);
        remote.sync();
        sync();
        return true;
    }

    public boolean validReciprocalLink() {
        if (!(level instanceof ServerLevel local) || link == null) return false;
        ServerLevel remoteLevel = link.level(local.getServer());
        if (remoteLevel == null || !remoteLevel.hasChunkAt(link.position())) return false;
        if (!(remoteLevel.getBlockEntity(link.position())
                instanceof LinkedMirrorBlockEntity remote)
                || remote.getClass() != getClass()) return invalidateLocal();
        MirrorLink back = remote.link;
        if (back == null || !back.dimension().equals(local.dimension().location())
                || !back.position().equals(worldPosition)) return invalidateLocal();
        return true;
    }

    public void invalidatePair() {
        if (!(level instanceof ServerLevel local) || link == null) return;
        ServerLevel remoteLevel = link.level(local.getServer());
        if (remoteLevel != null && remoteLevel.hasChunkAt(link.position())
                && remoteLevel.getBlockEntity(link.position())
                instanceof LinkedMirrorBlockEntity remote
                && remote.getClass() == getClass()) {
            remote.link = null;
            remote.sync();
        }
    }

    protected void linkTick() {
        if (!(level instanceof ServerLevel)) return;
        if (++ticks % linkCheckInterval != 0) return;
        if (!validReciprocalLink()) {
            linkCheckInterval = Math.min(600, linkCheckInterval + 20);
            restoreLink();
        } else {
            linkCheckInterval = 40;
        }
    }

    private boolean invalidateLocal() {
        link = null;
        sync();
        return false;
    }

    protected final void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public CompoundTag saveLinkForItem() {
        CompoundTag tag = new CompoundTag();
        if (link != null) link.save(tag);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (link != null) link.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        link = MirrorLink.load(tag);
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
