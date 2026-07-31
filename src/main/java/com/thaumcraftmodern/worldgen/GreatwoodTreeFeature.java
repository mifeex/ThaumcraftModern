package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.ArrayList;
import java.util.List;

/**
 * Direct modern port of TC4 4.2.3.5 {@code WorldGenGreatwoodTrees}.
 *
 * <p>The unusual second pass is intentional. The original first builds a
 * 2x2, 11-21-block height-limit tree, moves the generator base up by the
 * attenuated trunk height, widens the crown from 1.2 to 1.66, and runs the
 * complete leaf/branch/trunk pass again. Removing that pass is what made the
 * previous modern Greatwood visibly shorter than TC4.</p>
 */
public final class GreatwoodTreeFeature
        extends Feature<NoneFeatureConfiguration> {
    static final double HEIGHT_ATTENUATION = GreatwoodClassicSettings.HEIGHT_ATTENUATION;
    static final double BRANCH_SLOPE = GreatwoodClassicSettings.BRANCH_SLOPE;
    static final double INITIAL_SCALE_WIDTH = GreatwoodClassicSettings.INITIAL_SCALE_WIDTH;
    static final double SECOND_PASS_SCALE_WIDTH = GreatwoodClassicSettings.SECOND_PASS_SCALE_WIDTH;
    static final double LEAF_DENSITY = GreatwoodClassicSettings.LEAF_DENSITY;
    static final int TRUNK_SIZE = GreatwoodClassicSettings.TRUNK_SIZE;
    static final int HEIGHT_LIMIT_BASE = GreatwoodClassicSettings.HEIGHT_LIMIT_BASE;
    static final int LEAF_DISTANCE_LIMIT = GreatwoodClassicSettings.LEAF_DISTANCE_LIMIT;
    static final int SAPLING_SPIDER_DENOMINATOR = GreatwoodClassicSettings.SAPLING_SPIDER_DENOMINATOR;
    static final int WORLDGEN_SPIDER_DENOMINATOR = GreatwoodClassicSettings.WORLDGEN_SPIDER_DENOMINATOR;
    static final int WEB_ATTEMPTS = GreatwoodClassicSettings.WEB_ATTEMPTS;

    public GreatwoodTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return placeTree(
                context.level(),
                context.origin(),
                context.random(),
                false
        );
    }

    /**
     * @param wild true for natural world generation (TC4: 1/16 spider hollow);
     *             false for sapling growth (TC4: 1/8 spider hollow)
     */
    public static boolean placeTree(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            boolean wild
    ) {
        int spiderDenominator = wild
                ? WORLDGEN_SPIDER_DENOMINATOR
                : SAPLING_SPIDER_DENOMINATOR;
        boolean spiders = random.nextInt(spiderDenominator) == 0;
        return new Generator(level, random, origin).generate(spiders);
    }

    static int sampledHeightLimit(RandomSource random) {
        return GreatwoodClassicSettings.sampledHeightLimit(random);
    }

    static int attenuatedHeight(int heightLimit) {
        return GreatwoodClassicSettings.attenuatedHeight(heightLimit);
    }

    static int maximumGeneratedY(int baseY, int heightLimit) {
        return GreatwoodClassicSettings.maximumGeneratedY(baseY, heightLimit);
    }

    private static final class Generator {
        private static final int[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};

        private final WorldGenLevel level;
        private final RandomSource random;
        private final int[] base = new int[3];
        private int heightLimit;
        private int height;
        private double scaleWidth = INITIAL_SCALE_WIDTH;
        private List<LeafNode> leafNodes = List.of();

        private Generator(
                WorldGenLevel level,
                RandomSource source,
                BlockPos origin
        ) {
            this.level = level;
            this.random = RandomSource.create(source.nextLong());
            this.base[0] = origin.getX();
            this.base[1] = origin.getY();
            this.base[2] = origin.getZ();
            this.heightLimit = sampledHeightLimit(this.random);
        }

        private boolean generate(boolean spiders) {
            boolean valid = false;
            int chosenX = 0;
            int chosenZ = 0;
            search:
            for (int offsetX = -1; offsetX < 2; offsetX++) {
                for (int offsetZ = -1; offsetZ < 2; offsetZ++) {
                    boolean allValid = true;
                    for (int trunkX = 0; trunkX < TRUNK_SIZE && allValid; trunkX++) {
                        for (int trunkZ = 0; trunkZ < TRUNK_SIZE; trunkZ++) {
                            if (!validTreeLocation(
                                    trunkX + offsetX,
                                    trunkZ + offsetZ
                            )) {
                                allValid = false;
                                break;
                            }
                        }
                    }
                    if (allValid) {
                        valid = true;
                        chosenX = offsetX;
                        chosenZ = offsetZ;
                        break search;
                    }
                }
            }
            if (!valid) {
                return false;
            }

            base[0] += chosenX;
            base[2] += chosenZ;
            int finalBaseX = base[0];
            int finalBaseY = base[1];
            int finalBaseZ = base[2];

            generatePass();

            scaleWidth = SECOND_PASS_SCALE_WIDTH;
            base[0] = finalBaseX;
            base[1] = finalBaseY + height;
            base[2] = finalBaseZ;
            generatePass();

            if (spiders) {
                decorateSpiderTree(new BlockPos(
                        finalBaseX,
                        finalBaseY,
                        finalBaseZ
                ));
            }
            return true;
        }

        private void generatePass() {
            generateLeafNodeList();
            generateLeaves();
            generateLeafNodeBases();
            generateTrunk();
        }

        private void generateLeafNodeList() {
            height = attenuatedHeight(heightLimit);
            int nodesPerLayer = (int) (
                    1.382D
                            + Math.pow(
                            LEAF_DENSITY * heightLimit / 13.0D,
                            2.0D
                    )
            );
            nodesPerLayer = Math.max(1, nodesPerLayer);

            List<LeafNode> nodes = new ArrayList<>(
                    nodesPerLayer * heightLimit
            );
            int layerY = base[1] + heightLimit - LEAF_DISTANCE_LIMIT;
            int trunkTopY = base[1] + height;
            int relativeLayer = layerY - base[1];
            nodes.add(new LeafNode(base[0], layerY--, base[2], trunkTopY));

            while (relativeLayer >= 0) {
                float layerSize = layerSize(relativeLayer);
                if (layerSize >= 0.0F) {
                    for (int attempt = 0; attempt < nodesPerLayer; attempt++) {
                        double distance = scaleWidth
                                * layerSize
                                * (random.nextFloat() + 0.328D);
                        double angle = random.nextFloat() * 2.0D * Math.PI;
                        int nodeX = Mth.floor(
                                distance * Math.sin(angle) + base[0] + 0.5D
                        );
                        int nodeZ = Mth.floor(
                                distance * Math.cos(angle) + base[2] + 0.5D
                        );
                        int[] node = {nodeX, layerY, nodeZ};
                        int[] nodeTop = {
                                nodeX,
                                layerY + LEAF_DISTANCE_LIMIT,
                                nodeZ
                        };
                        if (checkBlockLine(node, nodeTop) != -1) {
                            continue;
                        }

                        double horizontalDistance = Math.sqrt(
                                Math.pow(Math.abs(base[0] - nodeX), 2.0D)
                                        + Math.pow(
                                        Math.abs(base[2] - nodeZ),
                                        2.0D
                                )
                        );
                        int branchBaseY = (int) (
                                layerY - horizontalDistance * BRANCH_SLOPE
                        );
                        branchBaseY = Math.min(branchBaseY, trunkTopY);
                        int[] branchBase = {
                                base[0],
                                branchBaseY,
                                base[2]
                        };
                        if (checkBlockLine(branchBase, node) == -1) {
                            nodes.add(new LeafNode(
                                    nodeX,
                                    layerY,
                                    nodeZ,
                                    branchBaseY
                            ));
                        }
                    }
                }
                layerY--;
                relativeLayer--;
            }
            leafNodes = List.copyOf(nodes);
        }

        private float layerSize(int relativeY) {
            if (relativeY < heightLimit * 0.3D) {
                return -1.618F;
            }
            float radius = heightLimit / 2.0F;
            float distance = heightLimit / 2.0F - relativeY;
            float size;
            if (distance == 0.0F) {
                size = radius;
            } else if (Math.abs(distance) >= radius) {
                size = 0.0F;
            } else {
                size = (float) Math.sqrt(
                        radius * radius - Math.abs(distance) * Math.abs(distance)
                );
            }
            return size * 0.5F;
        }

        private void generateLeaves() {
            leafNodes.forEach(node -> generateLeafNode(
                    node.x(),
                    node.y(),
                    node.z()
            ));
        }

        private void generateLeafNode(int x, int y, int z) {
            for (int layerY = y; layerY < y + LEAF_DISTANCE_LIMIT; layerY++) {
                int relativeY = layerY - y;
                float radius = relativeY == 0
                        || relativeY == LEAF_DISTANCE_LIMIT - 1
                        ? 2.0F
                        : 3.0F;
                generateTreeLayer(x, layerY, z, radius);
            }
        }

        private void generateTreeLayer(
                int x,
                int y,
                int z,
                float radius
        ) {
            int extent = (int) (radius + 0.618D);
            for (int offsetX = -extent; offsetX <= extent; offsetX++) {
                for (int offsetZ = -extent; offsetZ <= extent; offsetZ++) {
                    double distance = Math.pow(Math.abs(offsetX) + 0.5D, 2.0D)
                            + Math.pow(Math.abs(offsetZ) + 0.5D, 2.0D);
                    if (distance <= radius * radius) {
                        placeLeaf(new BlockPos(
                                x + offsetX,
                                y,
                                z + offsetZ
                        ));
                    }
                }
            }
        }

        private void placeLeaf(BlockPos position) {
            if (level.isOutsideBuildHeight(position)) {
                return;
            }
            BlockState current = level.getBlockState(position);
            if (current.isAir()
                    || current.is(BlockTags.LEAVES)
                    || current.canBeReplaced()) {
                set(
                        position,
                        ModBlocks.GREATWOOD_LEAVES.get()
                                .defaultBlockState()
                                .setValue(
                                        BlockStateProperties.PERSISTENT,
                                        false
                                )
                );
            }
        }

        private void generateLeafNodeBases() {
            for (LeafNode node : leafNodes) {
                int relativeBase = node.branchY() - base[1];
                if (relativeBase >= heightLimit * 0.2D) {
                    placeBlockLine(
                            new int[]{base[0], node.branchY(), base[2]},
                            new int[]{node.x(), node.y(), node.z()}
                    );
                }
            }
        }

        private void generateTrunk() {
            int[] from = {base[0], base[1], base[2]};
            int[] to = {base[0], base[1] + height, base[2]};
            placeBlockLine(from, to);

            from[0]++;
            to[0]++;
            placeBlockLine(from, to);
            from[2]++;
            to[2]++;
            placeBlockLine(from, to);
            from[0]--;
            to[0]--;
            placeBlockLine(from, to);
        }

        private boolean validTreeLocation(int offsetX, int offsetZ) {
            BlockPos trunkOrigin = new BlockPos(
                    base[0] + offsetX,
                    base[1],
                    base[2] + offsetZ
            );
            if (!TreeSitePolicy.hasDrySupportedSoil(level, trunkOrigin)) {
                return false;
            }
            BlockPos soil = new BlockPos(
                    base[0] + offsetX,
                    base[1] - 1,
                    base[2] + offsetZ
            );
            if (!level.getBlockState(soil).is(BlockTags.DIRT)) {
                return false;
            }
            int[] from = {
                    base[0] + offsetX,
                    base[1],
                    base[2] + offsetZ
            };
            int[] to = {
                    base[0] + offsetX,
                    base[1] + heightLimit - 1,
                    base[2] + offsetZ
            };
            int obstruction = checkBlockLine(from, to);
            if (obstruction == -1) {
                return true;
            }
            if (obstruction < 6) {
                return false;
            }
            heightLimit = obstruction;
            return true;
        }

        private int checkBlockLine(int[] from, int[] to) {
            int[] delta = new int[3];
            int dominant = 0;
            for (int axis = 0; axis < 3; axis++) {
                delta[axis] = to[axis] - from[axis];
                if (Math.abs(delta[axis]) > Math.abs(delta[dominant])) {
                    dominant = axis;
                }
            }
            if (delta[dominant] == 0) {
                return -1;
            }

            int step = delta[dominant] > 0 ? 1 : -1;
            double firstRatio = delta[OTHER_COORD_PAIRS[dominant]]
                    / (double) delta[dominant];
            double secondRatio = delta[OTHER_COORD_PAIRS[dominant + 3]]
                    / (double) delta[dominant];
            int end = delta[dominant] + step;
            int distance;
            for (distance = 0; distance != end; distance += step) {
                int[] point = new int[3];
                point[dominant] = from[dominant] + distance;
                point[OTHER_COORD_PAIRS[dominant]] = Mth.floor(
                        from[OTHER_COORD_PAIRS[dominant]]
                                + distance * firstRatio
                );
                point[OTHER_COORD_PAIRS[dominant + 3]] = Mth.floor(
                        from[OTHER_COORD_PAIRS[dominant + 3]]
                                + distance * secondRatio
                );
                BlockState state = level.getBlockState(new BlockPos(
                        point[0],
                        point[1],
                        point[2]
                ));
                if (!state.isAir()
                        && !state.is(ModBlocks.GREATWOOD_LEAVES.get())) {
                    break;
                }
            }
            return distance == end ? -1 : Math.abs(distance);
        }

        private void placeBlockLine(int[] from, int[] to) {
            int[] delta = new int[3];
            int dominant = 0;
            for (int axis = 0; axis < 3; axis++) {
                delta[axis] = to[axis] - from[axis];
                if (Math.abs(delta[axis]) > Math.abs(delta[dominant])) {
                    dominant = axis;
                }
            }
            if (delta[dominant] == 0) {
                return;
            }
            int step = delta[dominant] > 0 ? 1 : -1;
            double firstRatio = delta[OTHER_COORD_PAIRS[dominant]]
                    / (double) delta[dominant];
            double secondRatio = delta[OTHER_COORD_PAIRS[dominant + 3]]
                    / (double) delta[dominant];
            int end = delta[dominant] + step;
            for (int distance = 0; distance != end; distance += step) {
                int[] point = new int[3];
                point[dominant] = Mth.floor(from[dominant] + distance + 0.5D);
                point[OTHER_COORD_PAIRS[dominant]] = Mth.floor(
                        from[OTHER_COORD_PAIRS[dominant]]
                                + distance * firstRatio
                                + 0.5D
                );
                point[OTHER_COORD_PAIRS[dominant + 3]] = Mth.floor(
                        from[OTHER_COORD_PAIRS[dominant + 3]]
                                + distance * secondRatio
                                + 0.5D
                );
                Direction.Axis axis = Direction.Axis.Y;
                int deltaX = Math.abs(point[0] - from[0]);
                int deltaZ = Math.abs(point[2] - from[2]);
                int horizontal = Math.max(deltaX, deltaZ);
                if (horizontal > 0) {
                    axis = deltaX == horizontal
                            ? Direction.Axis.X
                            : Direction.Axis.Z;
                }
                set(
                        new BlockPos(point[0], point[1], point[2]),
                        ModBlocks.GREATWOOD_LOG.get()
                                .defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, axis)
                );
            }
        }

        private void decorateSpiderTree(BlockPos origin) {
            BlockPos spawnerPosition = origin.below();
            set(spawnerPosition, Blocks.SPAWNER.defaultBlockState());
            if (level.getBlockEntity(spawnerPosition)
                    instanceof SpawnerBlockEntity spawner) {
                CompoundTag entity = new CompoundTag();
                entity.putString("id", "minecraft:cave_spider");
                CompoundTag spawnData = new CompoundTag();
                spawnData.put("entity", entity);
                CompoundTag spawnerData = new CompoundTag();
                spawnerData.put("SpawnData", spawnData);
                spawner.load(spawnerData);
                spawner.setChanged();
            }

            for (int attempt = 0; attempt < WEB_ATTEMPTS; attempt++) {
                BlockPos web = origin.offset(
                        -7 + random.nextInt(14),
                        random.nextInt(10),
                        -7 + random.nextInt(14)
                );
                if (level.getBlockState(web).isAir()
                        && touchingGreatwood(web)) {
                    set(web, Blocks.COBWEB.defaultBlockState());
                }
            }

            BlockPos chestPosition = origin.below(2);
            set(chestPosition, Blocks.CHEST.defaultBlockState());
            if (level.getBlockEntity(chestPosition)
                    instanceof RandomizableContainerBlockEntity chest) {
                chest.setLootTable(
                        BuiltInLootTables.SIMPLE_DUNGEON,
                        random.nextLong()
                );
            }
        }

        private boolean touchingGreatwood(BlockPos position) {
            for (Direction direction : Direction.values()) {
                BlockState state = level.getBlockState(
                        position.relative(direction)
                );
                if (state.is(ModBlocks.GREATWOOD_LOG.get())
                        || state.is(ModBlocks.GREATWOOD_LEAVES.get())) {
                    return true;
                }
            }
            return false;
        }

        private void set(BlockPos position, BlockState state) {
            level.setBlock(position, state, 2);
        }
    }

    private record LeafNode(int x, int y, int z, int branchY) {
    }
}
