package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.FluxGasBlock;
import com.thaumcraftmodern.world.block.FluxGooBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Exact 1000-tick redstone conversion state machine from TC4's TileNodeConverter. */
public final class NodeTransducerBlockEntity extends BlockEntity {
    private int count = -1;
    private int status;

    public NodeTransducerBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.NODE_TRANSDUCER.get(), position, state);
    }

    public static void serverTick(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            NodeTransducerBlockEntity converter
    ) {
        if (converter.count == -1) {
            converter.checkStatus();
        }
        if (converter.status == 1 && converter.count >= 1000
                && level.getBlockEntity(position.below())
                instanceof AuraNodeBlockEntity node) {
            AuraNodeState original = node.snapshotState();
            level.setBlock(
                    position.below(),
                    ModBlocks.ENERGIZED_AURA_NODE.get().defaultBlockState(),
                    Block.UPDATE_ALL
            );
            if (level.getBlockEntity(position.below())
                    instanceof EnergizedAuraNodeBlockEntity energized) {
                energized.initialize(original);
            }
            converter.burst();
            converter.checkStatus();
        }
        if (converter.status == 2 && converter.count <= 50
                && level.getBlockEntity(position.below())
                instanceof EnergizedAuraNodeBlockEntity energized) {
            AuraNodeState original = emptied(energized.originalState());
            level.setBlock(
                    position.below(),
                    ModBlocks.AURA_NODE.get().defaultBlockState(),
                    Block.UPDATE_ALL
            );
            if (level.getBlockEntity(position.below())
                    instanceof AuraNodeBlockEntity restored) {
                restored.initializeOnce(original);
            }
            converter.burst();
            converter.status = 0;
        }

        if (converter.status == 0 || !level.hasNeighborSignal(position)) {
            if (converter.count > 0) {
                converter.count--;
            }
        } else if (converter.count < 1000) {
            converter.count++;
            if (level.getBlockEntity(position.below())
                    instanceof AuraNodeBlockEntity node) {
                drainRandomAspect(level, node);
            }
        }
        converter.count = Math.min(1000, converter.count);
    }

    public static void clientTick(
            Level level,
            BlockPos position,
            BlockState state,
            NodeTransducerBlockEntity converter
    ) {
        if (converter.count < 0) {
            converter.count = 0;
        }
        if (converter.status != 0 && level.hasNeighborSignal(position)) {
            converter.count = Math.min(1000, converter.count + 1);
        } else {
            converter.count = Math.max(0, converter.count - 1);
        }
    }

    public void checkStatus() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (count < 0) {
            count = 0;
        }
        BlockPos nodePosition = worldPosition.below();
        boolean stabilizer = level.getBlockEntity(worldPosition.below(2))
                instanceof NodeStabilizerBlockEntity;
        if (status == 2 && count > 50
                && (!stabilizer || !(level.getBlockEntity(nodePosition)
                instanceof EnergizedAuraNodeBlockEntity))) {
            level.removeBlock(nodePosition, false);
            level.explode(
                    null,
                    nodePosition.getX() + 0.5D,
                    nodePosition.getY() + 0.5D,
                    nodePosition.getZ() + 0.5D,
                    3.0F,
                    Level.ExplosionInteraction.NONE
            );
            scatterFlux(server, nodePosition);
            status = 0;
            count = 50;
            sync();
        } else if (server.hasNeighborSignal(worldPosition)
                && server.getBlockEntity(nodePosition)
                instanceof AuraNodeBlockEntity && stabilizer) {
            status = 1;
            sync();
        } else if (server.getBlockEntity(nodePosition)
                instanceof EnergizedAuraNodeBlockEntity) {
            status = 2;
            count = 1000;
            sync();
        } else {
            status = 0;
            sync();
        }
    }

    /** Exact BlockAiry.explodify distribution: 50 triangular +/-7 attempts. */
    private static void scatterFlux(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 50; attempt++) {
            BlockPos target = origin.offset(
                    level.random.nextInt(8) - level.random.nextInt(8),
                    level.random.nextInt(8) - level.random.nextInt(8),
                    level.random.nextInt(8) - level.random.nextInt(8)
            );
            if (!level.getBlockState(target).isAir()) {
                continue;
            }
            BlockState flux = target.getY() < origin.getY()
                    ? ModBlocks.FLUX_GOO.get().defaultBlockState()
                            .setValue(FluxGooBlock.LEVEL, 7)
                    : ModBlocks.FLUX_GAS.get().defaultBlockState()
                            .setValue(FluxGasBlock.LEVEL, 7);
            level.setBlock(target, flux, Block.UPDATE_ALL);
        }
    }

    private static void drainRandomAspect(
            ServerLevel level,
            AuraNodeBlockEntity node
    ) {
        AuraNodeState.Snapshot snapshot = node.snapshotState().snapshot();
        List<String> available = new ArrayList<>();
        snapshot.aspectsCurrent().forEach((aspect, amount) -> {
            if (amount > 0) {
                available.add(aspect);
            }
        });
        if (available.isEmpty()) {
            return;
        }
        String selected = available.get(level.random.nextInt(available.size()));
        Map<String, Integer> current = new LinkedHashMap<>(
                snapshot.aspectsCurrent());
        current.put(selected, current.get(selected) - 1);
        node.replaceAspects(
                snapshot.revision(),
                current,
                snapshot.aspectsMaximum()
        );
    }

    private static AuraNodeState emptied(AuraNodeState state) {
        AuraNodeState.Snapshot snapshot = state.snapshot();
        Map<String, Integer> empty = new LinkedHashMap<>();
        snapshot.aspectsMaximum().keySet().forEach(key -> empty.put(key, 0));
        return AuraNodeState.withAspects(
                snapshot.nodeId(),
                snapshot.type(),
                snapshot.modifier(),
                empty,
                snapshot.aspectsMaximum(),
                snapshot.revision() + 1
        );
    }

    private void burst() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        server.sendParticles(
                ModParticles.NODE_BURST.get(),
                worldPosition.getX() + 0.5D,
                worldPosition.getY() - 0.5D,
                worldPosition.getZ() + 0.5D,
                1, 0, 0, 0, 0
        );
        server.playSound(
                null,
                worldPosition.below(),
                ModSounds.CRAFT_FAIL.get(),
                SoundSource.BLOCKS,
                0.5F,
                1.0F
        );
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public int count() {
        return Math.max(0, count);
    }

    public int status() {
        return status;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("count", count);
        tag.putInt("status", status);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        count = tag.getInt("count");
        status = tag.getInt("status");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        if (packet.getTag() != null) {
            load(packet.getTag());
        }
    }
}
