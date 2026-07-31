package com.thaumcraftmodern.client;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlockEntity;
import com.thaumcraftmodern.scan.AspectReward;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import com.thaumcraftmodern.world.block.entity.CrucibleBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Extensible client registry for the shared TC4 container HUD.
 * New block-entity classes add one adapter instead of extending an
 * {@code instanceof} chain in the overlay renderer.
 */
public final class AspectContainerHudRegistry {
    /** TC4 tagscale settles at 0.3 and offsets by tagscale * 2. */
    public static final double CLASSIC_FACE_OFFSET = 0.6D;
    private static final List<Registration<?>> ADAPTERS = new ArrayList<>();

    static {
        register(AuraNodeBlockEntity.class, (node, hit) -> Optional.of(
                new Readout(
                        ClientAspectContainerReadout.nodeContents(
                                node.snapshotState().snapshot()),
                        aboveBlock(hit.getBlockPos(),
                                ClientAspectContainerReadout.DEFAULT_HUD_ANCHOR_HEIGHT))));
        register(JarredAuraNodeBlockEntity.class, (jar, hit) -> jar.data()
                .map(data -> new Readout(
                        ClientAspectContainerReadout.nodeContents(
                                data.node().snapshot()),
                        aboveBlock(hit.getBlockPos(),
                                ClientAspectContainerReadout.DEFAULT_HUD_ANCHOR_HEIGHT))));
        register(CrucibleBlockEntity.class, (crucible, hit) -> Optional.of(
                new Readout(
                        ClientAspectContainerReadout.crucibleContents(
                                crucible.essentia()),
                        aboveBlock(hit.getBlockPos(),
                                ClientAspectContainerReadout.DEFAULT_HUD_ANCHOR_HEIGHT))));
        register(ArcaneAlembicBlockEntity.class, (alembic, hit) ->
                alembic.storedAspect() == null || alembic.storedAmount() <= 0
                        ? Optional.empty()
                        : Optional.of(new Readout(
                                ClientAspectContainerReadout.singleAspectContents(
                                        alembic.storedAspect(), alembic.storedAmount()),
                                onHitFace(hit))));
        register(EssentiaJarBlockEntity.class, (jar, hit) ->
                jar.aspect() == null || jar.amount() <= 0
                        ? Optional.empty()
                        : Optional.of(new Readout(
                                ClientAspectContainerReadout.singleAspectContents(
                                        jar.aspect(), jar.amount()),
                                onHitFace(hit))));
        register(EssentiaTubeBlockEntity.class, (tube, hit) ->
                tube.filter() == null ? Optional.empty()
                        : Optional.of(new Readout(
                                List.of(new AspectReward(tube.filter(), 1)),
                                onHitFace(hit))));
    }

    private AspectContainerHudRegistry() {
    }

    public static synchronized <T extends BlockEntity> void register(
            Class<T> type, Adapter<T> adapter) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(adapter, "adapter");
        ADAPTERS.removeIf(registration -> registration.type() == type);
        // Latest registrations win, so an add-on can specialize a subclass
        // even when a broader built-in adapter was registered first.
        ADAPTERS.add(0, new Registration<>(type, adapter));
    }

    static synchronized Optional<Readout> resolve(
            BlockEntity blockEntity, BlockHitResult hit) {
        for (Registration<?> registration : ADAPTERS) {
            Optional<Readout> result = registration.resolve(blockEntity, hit);
            if (result != null) {
                return result.filter(readout -> !readout.aspects().isEmpty());
            }
        }
        return Optional.empty();
    }

    public static Vec3 onHitFace(BlockHitResult hit) {
        Vec3 center = Vec3.atCenterOf(hit.getBlockPos());
        return center.add(
                hit.getDirection().getStepX() * CLASSIC_FACE_OFFSET,
                hit.getDirection().getStepY() * CLASSIC_FACE_OFFSET,
                hit.getDirection().getStepZ() * CLASSIC_FACE_OFFSET);
    }

    public static Vec3 aboveBlock(BlockPos position, double height) {
        return new Vec3(position.getX() + 0.5D,
                position.getY() + height, position.getZ() + 0.5D);
    }

    @FunctionalInterface
    public interface Adapter<T extends BlockEntity> {
        Optional<Readout> resolve(T blockEntity, BlockHitResult hit);
    }

    public record Readout(List<AspectReward> aspects, Vec3 anchor) {
        public Readout {
            aspects = List.copyOf(aspects);
            Objects.requireNonNull(anchor, "anchor");
        }
    }

    private record Registration<T extends BlockEntity>(
            Class<T> type, Adapter<T> adapter) {
        private Optional<Readout> resolve(
                BlockEntity blockEntity, BlockHitResult hit) {
            return type.isInstance(blockEntity)
                    ? adapter.resolve(type.cast(blockEntity), hit)
                    : null;
        }
    }
}
