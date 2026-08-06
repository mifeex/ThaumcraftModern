package com.thaumcraftmodern.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;

/** Discovers one complete tagged tree and gives the lumber golem a stable order. */
final class GolemLumberTask {
    private static final int MAX_TREE_LOGS = 65_536;

    private GolemLumberTask() {
    }

    static Tree discover(Level level, BlockPos center, int range) {
        Set<BlockPos> examined = new HashSet<>();
        Tree best = null;
        double bestDistance = Double.MAX_VALUE;
        int minY = Math.max(level.getMinBuildHeight(), center.getY() - range);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, center.getY() + range);
        for (BlockPos cursor : BlockPos.betweenClosed(
                new BlockPos(center.getX() - range, minY, center.getZ() - range),
                new BlockPos(center.getX() + range, maxY, center.getZ() + range))) {
            BlockPos seed = cursor.immutable();
            if (examined.contains(seed) || !level.getBlockState(seed).is(BlockTags.LOGS)) {
                continue;
            }
            Set<BlockPos> logs = collectConnectedLogs(level, seed);
            examined.addAll(logs);
            if (logs.isEmpty() || !touchesLeaves(level, logs)) {
                continue;
            }
            BlockPos base = logs.stream().min(logOrder(seed)).orElse(seed);
            double dx = base.getX() + 0.5D - (center.getX() + 0.5D);
            double dz = base.getZ() + 0.5D - (center.getZ() + 0.5D);
            double distance = dx * dx + dz * dz;
            if (distance <= range * range && distance < bestDistance) {
                best = new Tree(base, orderedLogs(base, logs));
                bestDistance = distance;
            }
        }
        return best;
    }

    static List<BlockPos> orderedLogs(BlockPos base, Collection<BlockPos> logs) {
        ArrayList<BlockPos> ordered = new ArrayList<>(logs.size());
        for (BlockPos log : logs) {
            ordered.add(log.immutable());
        }
        ordered.sort(logOrder(base));
        return List.copyOf(ordered);
    }

    private static Comparator<BlockPos> logOrder(BlockPos base) {
        return Comparator.comparingInt((BlockPos pos) -> pos.getY())
                .thenComparingLong(pos -> horizontalDistanceSquared(pos, base))
                .thenComparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getZ);
    }

    private static long horizontalDistanceSquared(BlockPos left, BlockPos right) {
        long dx = left.getX() - right.getX();
        long dz = left.getZ() - right.getZ();
        return dx * dx + dz * dz;
    }

    private static Set<BlockPos> collectConnectedLogs(Level level, BlockPos seed) {
        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);
        while (!queue.isEmpty() && logs.size() < MAX_TREE_LOGS) {
            BlockPos current = queue.removeFirst().immutable();
            if (!visited.add(current) || level.isOutsideBuildHeight(current)
                    || !level.getBlockState(current).is(BlockTags.LOGS)) {
                continue;
            }
            logs.add(current);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                            BlockPos neighbour = current.offset(dx, dy, dz);
                            if (!visited.contains(neighbour)) {
                                queue.addLast(neighbour);
                            }
                        }
                    }
                }
            }
        }
        return logs;
    }

    private static boolean touchesLeaves(Level level, Collection<BlockPos> logs) {
        for (BlockPos log : logs) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if ((dx != 0 || dy != 0 || dz != 0)
                                && level.getBlockState(log.offset(dx, dy, dz))
                                        .is(BlockTags.LEAVES)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    record Tree(BlockPos base, List<BlockPos> logs) {
    }
}
