package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete server-world adapter for structure checks, capture and placement.
 * The registered jar block state is injected, keeping central registries out
 * of this package.
 */
public final class ServerNodeJarWorld
        implements NodeJarStructure.WorldView,
        NodeJarCaptureService.CaptureWorld,
        NodeJarPlacementService.PlacementWorld {
    private static final int UPDATE_FLAGS =
            Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;

    private final ServerLevel level;
    private final BlockState jarBlockState;
    private final Map<UUID, CaptureSnapshot> captureRollbacks =
            new LinkedHashMap<>();
    private final Map<UUID, BlockState> placementRollbacks =
            new LinkedHashMap<>();

    public ServerNodeJarWorld(ServerLevel level, BlockState jarBlockState) {
        this.level = Objects.requireNonNull(level, "level");
        this.jarBlockState = Objects.requireNonNull(jarBlockState, "jarBlockState");
    }

    @Override
    public boolean isLoaded(BlockPos position) {
        return level.hasChunkAt(position);
    }

    @Override
    public boolean isAuraNode(BlockPos position, UUID expectedNodeId) {
        return level.getBlockEntity(position) instanceof AuraNodeBlockEntity node
                && node.scanIdentity().nodeId().equals(expectedNodeId);
    }

    @Override
    public boolean isGlass(BlockPos position) {
        return level.getBlockState(position).is(Blocks.GLASS);
    }

    @Override
    public boolean isWoodenSlab(BlockPos position) {
        return level.getBlockState(position).is(BlockTags.WOODEN_SLABS);
    }

    @Override
    public Optional<AuraNodeState> snapshotNode(BlockPos position) {
        return level.getBlockEntity(position) instanceof AuraNodeBlockEntity node
                ? Optional.of(node.snapshotState())
                : Optional.empty();
    }

    @Override
    public boolean captureAtomically(
            BlockPos nodePosition,
            List<BlockPos> consumedMaterials,
            NodeJarData data
    ) {
        Objects.requireNonNull(nodePosition, "nodePosition");
        Objects.requireNonNull(consumedMaterials, "consumedMaterials");
        Objects.requireNonNull(data, "data");
        if (level.isClientSide
                || consumedMaterials.size()
                != NodeJarStructure.GLASS_COUNT + NodeJarStructure.WOODEN_SLAB_COUNT
                || !isAuraNode(nodePosition, data.node().nodeId())) {
            return false;
        }

        AuraNodeState originalNode = snapshotNode(nodePosition).orElse(null);
        if (originalNode == null) {
            return false;
        }
        LinkedHashMap<BlockPos, BlockState> previous = new LinkedHashMap<>();
        previous.put(nodePosition.immutable(), level.getBlockState(nodePosition));
        for (BlockPos material : consumedMaterials) {
            if (!level.hasChunkAt(material)) {
                return false;
            }
            previous.put(material.immutable(), level.getBlockState(material));
        }

        CaptureSnapshot rollback = new CaptureSnapshot(previous, originalNode);
        for (BlockPos material : consumedMaterials) {
            if (!level.setBlock(material, Blocks.AIR.defaultBlockState(), UPDATE_FLAGS)) {
                restoreCapture(nodePosition, rollback);
                return false;
            }
        }
        if (!level.setBlock(nodePosition, jarBlockState, UPDATE_FLAGS)) {
            restoreCapture(nodePosition, rollback);
            return false;
        }
        BlockEntity placed = level.getBlockEntity(nodePosition);
        if (!(placed instanceof JarredAuraNodeBlockEntity jar)
                || !jar.initializeOnce(data)) {
            restoreCapture(nodePosition, rollback);
            return false;
        }
        captureRollbacks.put(data.payloadId(), rollback);
        return true;
    }

    @Override
    public void rollbackCapture(BlockPos nodePosition, NodeJarData data) {
        CaptureSnapshot snapshot = captureRollbacks.remove(data.payloadId());
        if (snapshot != null) {
            restoreCapture(nodePosition, snapshot);
        }
    }

    @Override
    public void commitCapture(BlockPos nodePosition, NodeJarData data) {
        captureRollbacks.remove(data.payloadId());
    }

    @Override
    public boolean placeAtomically(BlockPos position, NodeJarData data) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(data, "data");
        if (level.isClientSide
                || !level.hasChunkAt(position)
                || !level.getBlockState(position).canBeReplaced()) {
            return false;
        }
        BlockState previous = level.getBlockState(position);
        if (!level.setBlock(position, jarBlockState, UPDATE_FLAGS)) {
            return false;
        }
        BlockEntity placed = level.getBlockEntity(position);
        if (!(placed instanceof JarredAuraNodeBlockEntity jar)
                || !jar.initializeOnce(data)) {
            level.setBlock(position, previous, UPDATE_FLAGS);
            return false;
        }
        placementRollbacks.put(data.payloadId(), previous);
        return true;
    }

    @Override
    public void rollbackPlacement(BlockPos position, NodeJarData data) {
        BlockState previous = placementRollbacks.remove(data.payloadId());
        if (previous != null) {
            level.setBlock(position, previous, UPDATE_FLAGS);
        }
    }

    @Override
    public void commitPlacement(BlockPos position, NodeJarData data) {
        placementRollbacks.remove(data.payloadId());
    }

    private void restoreCapture(
            BlockPos nodePosition,
            CaptureSnapshot snapshot
    ) {
        for (Map.Entry<BlockPos, BlockState> entry : snapshot.blocks().entrySet()) {
            if (!entry.getKey().equals(nodePosition)) {
                level.setBlock(entry.getKey(), entry.getValue(), UPDATE_FLAGS);
            }
        }
        BlockState nodeBlock = snapshot.blocks().get(nodePosition);
        level.setBlock(nodePosition, nodeBlock, UPDATE_FLAGS);
        if (level.getBlockEntity(nodePosition) instanceof AuraNodeBlockEntity node) {
            node.initializeOnce(snapshot.node());
        }
    }

    private record CaptureSnapshot(
            Map<BlockPos, BlockState> blocks,
            AuraNodeState node
    ) {
        private CaptureSnapshot {
            blocks = Map.copyOf(blocks);
            node = node.copy();
        }

        @Override
        public AuraNodeState node() {
            return node.copy();
        }
    }
}
