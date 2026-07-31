package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Persistent placed jar payload. Invalid data is quarantined instead of
 * generating a replacement node that could duplicate vis.
 */
public final class JarredAuraNodeBlockEntity extends BlockEntity {
    private static final String DATA_KEY = "NodeJar";

    private NodeJarData data;
    private String invalidDiagnostic = "";

    public JarredAuraNodeBlockEntity(
            BlockEntityType<?> type,
            BlockPos position,
            BlockState state
    ) {
        super(Objects.requireNonNull(type, "type"), position, state);
    }

    public synchronized boolean initializeOnce(NodeJarData initialData) {
        Objects.requireNonNull(initialData, "initialData");
        if (data != null || !invalidDiagnostic.isEmpty()) {
            return false;
        }
        data = copy(initialData);
        markChangedAndSync();
        return true;
    }

    public synchronized Optional<NodeJarData> data() {
        return Optional.ofNullable(data).map(JarredAuraNodeBlockEntity::copy);
    }

    public synchronized Optional<ItemStack> createDrop(Item jarItem) {
        if (data == null) {
            return Optional.empty();
        }
        ItemStack stack = new ItemStack(Objects.requireNonNull(jarItem, "jarItem"));
        NodeJarCodec.write(stack, data);
        return Optional.of(stack);
    }

    public synchronized String invalidDiagnostic() {
        return invalidDiagnostic;
    }

    @Override
    protected synchronized void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (data != null) {
            tag.put(DATA_KEY, NodeJarCodec.encode(data));
        }
    }

    @Override
    public synchronized void load(CompoundTag tag) {
        super.load(tag);
        data = null;
        invalidDiagnostic = "";
        try {
            data = NodeJarCodec.decode(tag.getCompound(DATA_KEY));
        } catch (RuntimeException exception) {
            invalidDiagnostic = exception.getClass().getSimpleName()
                    + ": " + exception.getMessage();
            ThaumcraftModern.LOGGER.error(
                    "Quarantined invalid jarred aura node at {}: {}",
                    worldPosition,
                    invalidDiagnostic
            );
        }
    }

    @Override
    public synchronized CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public synchronized @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public synchronized void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    private static NodeJarData copy(NodeJarData source) {
        return new NodeJarData(source.payloadId(), source.origin(), source.node());
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }
}
