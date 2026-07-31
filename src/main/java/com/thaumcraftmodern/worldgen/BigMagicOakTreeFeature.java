package com.thaumcraftmodern.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern equivalent of TC4's {@code WorldGenBigMagicTree}.
 *
 * <p>Magical Forest uses this tall, branching vanilla-oak variant as its
 * ordinary tree instead of the mixed small oak/birch forest feature.</p>
 */
public final class BigMagicOakTreeFeature {
    private static final int MIN_HEIGHT = 11;
    private static final int EXTRA_HEIGHT = 12;
    private static final int LEAF_CLUSTER_HEIGHT = 4;

    private BigMagicOakTreeFeature() {
    }

    public static boolean placeTree(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random
    ) {
        int height = MIN_HEIGHT + random.nextInt(EXTRA_HEIGHT);
        int trunkHeight = Math.min(
                height - 1,
                Math.max(7, (int) (height * 0.6618D))
        );
        if (!TreeSitePolicy.hasDrySupportedSoil(level, origin)
                || level.isOutsideBuildHeight(origin.above(height + 2))
                || !clearTrunk(level, origin, height)) {
            return false;
        }

        List<LeafNode> nodes = createLeafNodes(origin, height, trunkHeight, random);
        for (LeafNode node : nodes) {
            BlockPos branchBase = new BlockPos(
                    origin.getX(),
                    node.branchY(),
                    origin.getZ()
            );
            if (!clearLine(level, node.position(), node.position().above(3))
                    || !clearLine(level, branchBase, node.position())) {
                continue;
            }
            placeLeafCluster(level, node.position());
            if (node.branchY() - origin.getY() >= height * 0.2D) {
                placeLogLine(level, branchBase, node.position());
            }
        }
        placeLogLine(level, origin, origin.above(trunkHeight));
        return true;
    }

    private static List<LeafNode> createLeafNodes(
            BlockPos origin,
            int height,
            int trunkHeight,
            RandomSource random
    ) {
        List<LeafNode> nodes = new ArrayList<>();
        int nodesPerLayer = Math.max(
                1,
                (int) (1.382D + Math.pow(height / 13.0D, 2.0D))
        );
        int topY = origin.getY() + height - 3;
        nodes.add(new LeafNode(
                new BlockPos(origin.getX(), topY, origin.getZ()),
                origin.getY() + trunkHeight
        ));

        for (int y = topY - 1; y >= origin.getY(); y--) {
            int relativeY = y - origin.getY();
            double layerRadius = layerRadius(relativeY, height);
            if (layerRadius < 0.0D) {
                continue;
            }
            for (int node = 0; node < nodesPerLayer; node++) {
                double distance = layerRadius
                        * (random.nextDouble() + 0.328D);
                double angle = random.nextDouble() * Math.PI * 2.0D;
                int x = (int) Math.floor(
                        distance * Math.sin(angle) + origin.getX() + 0.5D
                );
                int z = (int) Math.floor(
                        distance * Math.cos(angle) + origin.getZ() + 0.5D
                );
                double horizontal = Math.hypot(
                        origin.getX() - x,
                        origin.getZ() - z
                );
                int branchY = (int) Math.min(
                        origin.getY() + trunkHeight,
                        y - horizontal * 0.381D
                );
                nodes.add(new LeafNode(new BlockPos(x, y, z), branchY));
            }
        }
        return nodes;
    }

    private static double layerRadius(int y, int height) {
        if (y < height * 0.3D) {
            return -1.0D;
        }
        double half = height / 2.0D;
        double offset = half - y;
        if (Math.abs(offset) >= half) {
            return 0.0D;
        }
        return Math.sqrt(half * half - offset * offset) * 0.5D;
    }

    private static boolean clearTrunk(
            WorldGenLevel level,
            BlockPos origin,
            int height
    ) {
        for (int y = 0; y < height; y++) {
            if (!replaceable(level.getBlockState(origin.above(y)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean clearLine(
            WorldGenLevel level,
            BlockPos start,
            BlockPos end
    ) {
        int steps = longestAxis(start, end);
        for (int step = 0; step <= steps; step++) {
            if (!replaceable(level.getBlockState(interpolate(start, end, step, steps)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean replaceable(BlockState state) {
        return state.isAir()
                || state.is(BlockTags.LEAVES)
                || state.canBeReplaced();
    }

    private static void placeLeafCluster(
            WorldGenLevel level,
            BlockPos base
    ) {
        for (int y = 0; y < LEAF_CLUSTER_HEIGHT; y++) {
            int radius = y == 0 || y == LEAF_CLUSTER_HEIGHT - 1 ? 2 : 3;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.pow(Math.abs(x) + 0.5D, 2.0D)
                            + Math.pow(Math.abs(z) + 0.5D, 2.0D);
                    if (distance > radius * radius) {
                        continue;
                    }
                    BlockPos position = base.offset(x, y, z);
                    if (replaceable(level.getBlockState(position))) {
                        level.setBlock(
                                position,
                                Blocks.OAK_LEAVES.defaultBlockState()
                                        .setValue(LeavesBlock.PERSISTENT, false)
                                        .setValue(
                                                LeavesBlock.DISTANCE,
                                                Math.max(
                                                        1,
                                                        Math.min(
                                                                6,
                                                                Math.abs(x)
                                                                        + Math.abs(z)
                                                                        + y
                                                        )
                                                )
                                        ),
                                2
                        );
                    }
                }
            }
        }
    }

    private static void placeLogLine(
            WorldGenLevel level,
            BlockPos start,
            BlockPos end
    ) {
        int steps = longestAxis(start, end);
        for (int step = 0; step <= steps; step++) {
            BlockPos position = interpolate(start, end, step, steps);
            Direction.Axis axis = logAxis(start, end);
            level.setBlock(
                    position,
                    Blocks.OAK_LOG.defaultBlockState()
                            .setValue(RotatedPillarBlock.AXIS, axis),
                    2
            );
        }
    }

    private static int longestAxis(BlockPos start, BlockPos end) {
        return Math.max(
                Math.abs(end.getX() - start.getX()),
                Math.max(
                        Math.abs(end.getY() - start.getY()),
                        Math.abs(end.getZ() - start.getZ())
                )
        );
    }

    private static BlockPos interpolate(
            BlockPos start,
            BlockPos end,
            int step,
            int steps
    ) {
        if (steps == 0) {
            return start;
        }
        double progress = step / (double) steps;
        return new BlockPos(
                (int) Math.floor(
                        start.getX() + 0.5D
                                + (end.getX() - start.getX()) * progress
                ),
                (int) Math.floor(
                        start.getY() + 0.5D
                                + (end.getY() - start.getY()) * progress
                ),
                (int) Math.floor(
                        start.getZ() + 0.5D
                                + (end.getZ() - start.getZ()) * progress
                )
        );
    }

    private static Direction.Axis logAxis(BlockPos start, BlockPos end) {
        int x = Math.abs(end.getX() - start.getX());
        int y = Math.abs(end.getY() - start.getY());
        int z = Math.abs(end.getZ() - start.getZ());
        if (x > y && x >= z) {
            return Direction.Axis.X;
        }
        if (z > y && z > x) {
            return Direction.Axis.Z;
        }
        return Direction.Axis.Y;
    }

    private record LeafNode(BlockPos position, int branchY) {
    }
}
