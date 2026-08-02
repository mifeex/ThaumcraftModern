package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.ManaPodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Server-authored TC4 mana-pod aspect and growth state. */
public final class ManaPodBlockEntity extends BlockEntity {
    public static final String ASPECT_TAG = "aspect";
    private static final String FALLBACK_ASPECT = "herba";
    private static final List<String> PRIMAL_ASPECTS = List.of(
            "aer", "ignis", "aqua", "terra", "ordo", "perditio"
    );

    private String aspect;

    public ManaPodBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.MANA_POD.get(), position, state);
    }

    public @Nullable String aspect() {
        return aspect;
    }

    public void setAspect(@Nullable String aspectId) {
        aspect = validAspect(aspectId) ? aspectId : null;
        sync();
    }

    /**
     * Initializes a newly generated pod without touching the live
     * {@link ServerLevel}. Worldgen already owns the containing proto-chunk;
     * sending block updates or querying neighbour BlockEntities here can
     * synchronously request that same chunk and deadlock generation.
     */
    public void initializeWorldgen(RandomSource random) {
        if (aspect == null) {
            aspect = randomAspect(random);
        }
    }

    /** Exact modern equivalent of TC4 TileManaPod.checkGrowth(). */
    public void checkGrowth(ServerLevel level) {
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof ManaPodBlock)) {
            return;
        }
        int age = state.getValue(ManaPodBlock.AGE);
        if (age < ManaPodBlock.MAX_AGE) {
            age++;
            state = state.setValue(ManaPodBlock.AGE, age);
            level.setBlock(worldPosition, state, Block.UPDATE_ALL);
        }
        if (age <= 2) {
            return;
        }
        if (age == 3) {
            chooseCrossbredAspect(level);
        }
        if (aspect == null) {
            aspect = randomAspect(level.random);
            sync();
        }
    }

    private static String randomAspect(RandomSource random) {
        return random.nextInt(8) == 0
                ? FALLBACK_ASPECT
                : PRIMAL_ASPECTS.get(random.nextInt(PRIMAL_ASPECTS.size()));
    }

    private void chooseCrossbredAspect(ServerLevel level) {
        Set<String> neighbors = new LinkedHashSet<>();
        if (aspect != null) {
            neighbors.add(aspect);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                    instanceof ManaPodBlockEntity pod
                    && pod.aspect != null) {
                neighbors.add(pod.aspect);
            }
        }
        if (neighbors.size() > 1) {
            List<String> values = List.copyOf(neighbors);
            List<String> candidates = new ArrayList<>();
            for (int first = 0; first < values.size(); first++) {
                candidates.add(values.get(first));
                for (int second = 0; second < values.size(); second++) {
                    if (first == second) {
                        continue;
                    }
                    composition(values.get(first), values.get(second))
                            .ifPresent(result -> {
                                candidates.add(result);
                                candidates.add(result);
                            });
                }
            }
            if (!candidates.isEmpty()) {
                aspect = candidates.get(level.random.nextInt(candidates.size()));
                sync();
            }
        } else if (aspect == null && neighbors.size() == 1) {
            aspect = neighbors.iterator().next();
            sync();
        }
    }

    private static java.util.Optional<String> composition(
            String first,
            String second
    ) {
        if (AspectRegistryRuntime.find(first).isEmpty()
                || AspectRegistryRuntime.find(second).isEmpty()) {
            return java.util.Optional.empty();
        }
        return AspectRegistryRuntime.catalog()
                .compositionResult(first, second)
                .map(AspectDefinition::id);
    }

    private static boolean validAspect(@Nullable String aspectId) {
        return aspectId != null
                && !aspectId.isBlank()
                && aspectId.equals(aspectId.trim())
                && aspectId.equals(aspectId.toLowerCase(java.util.Locale.ROOT));
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (aspect != null) {
            tag.putString(ASPECT_TAG, aspect);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        String stored = tag.getString(ASPECT_TAG);
        aspect = validAspect(stored) ? stored : null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
