package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Places rare aura nodes in open air, both at the surface and in caves.
 *
 * <p>TC4 used one wild-node attempt per 36 chunks and did not reserve a
 * minimum distance between successful attempts. The modern placed feature is
 * invoked once per eligible chunk and keeps that same independent roll.</p>
 */
public final class AuraNodeFeature extends Feature<NoneFeatureConfiguration> {
    public AuraNodeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        boolean taintedLands = level.getBiome(origin).is(
                ModWorldgenKeys.TAINTED_LANDS
        );
        if (!AuraNodeSpawnRoll.roll(random, taintedLands)) {
            return false;
        }

        int y = firstUncoveredY(level, origin.getX(), origin.getZ());
        BlockPos base = new BlockPos(origin.getX(), y, origin.getZ());
        if (level.isEmptyBlock(base.above())) {
            base = base.above();
        }
        BlockPos candidate = base.above(random.nextInt(4));
        BlockState candidateState = level.getBlockState(candidate);
        BlockPos position = level.isEmptyBlock(candidate)
                || candidateState.canBeReplaced()
                ? candidate
                : base;
        if (level.isOutsideBuildHeight(position)
                || !level.getFluidState(position).isEmpty()
                || (!level.isEmptyBlock(position)
                && !level.getBlockState(position).canBeReplaced())) {
            return false;
        }

        level.setBlock(position, ModBlocks.AURA_NODE.get().defaultBlockState(), 2);
        if (!(level.getBlockEntity(position) instanceof AuraNodeBlockEntity node)) {
            return false;
        }
        return node.initializeOnce(
                ClassicAuraNodeWorldFactory.create(level, position, random)
        );
    }

    private static int firstUncoveredY(
            WorldGenLevel level,
            int x,
            int z
    ) {
        int minimum = level.getMinBuildHeight() + 5;
        int maximum = level.getMaxBuildHeight() - 3;
        for (int y = minimum; y < maximum; y++) {
            if (level.isEmptyBlock(new BlockPos(x, y + 1, z))) {
                return y;
            }
        }
        return level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                x,
                z
        );
    }

}
