package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

import java.util.List;

/**
 * One feature reproduces TC4's per-chunk ore pass. Unlike the 1.7.10 pass it
 * understands the negative build range and deepslate cave layer.
 */
public final class LegacyOreFeature extends Feature<NoneFeatureConfiguration> {
    private static final List<java.util.function.Supplier<net.minecraft.world.level.block.Block>>
            INFUSED_STONES = List.of(
                    ModBlocks.AIR_INFUSED_STONE,
                    ModBlocks.FIRE_INFUSED_STONE,
                    ModBlocks.WATER_INFUSED_STONE,
                    ModBlocks.EARTH_INFUSED_STONE,
                    ModBlocks.ORDER_INFUSED_STONE,
                    ModBlocks.ENTROPY_INFUSED_STONE
            );

    public LegacyOreFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!ThaumcraftModernServerConfig.generateOres()) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int chunkMinX = Math.floorDiv(context.origin().getX(), 16) * 16;
        int chunkMinZ = Math.floorDiv(context.origin().getZ(), 16) * 16;
        int placed = 0;

        for (int attempt = 0;
             attempt < ThaumcraftModernServerConfig.cinnabarAttempts();
             attempt++) {
            int x = chunkMinX + random.nextInt(16);
            int z = chunkMinZ + random.nextInt(16);
            int surface = level.getHeight(
                    Heightmap.Types.OCEAN_FLOOR_WG,
                    x,
                    z
            );
            int minimum = level.getMinBuildHeight() + 8;
            int maximum = Math.min(48, surface - 8);
            if (maximum >= minimum) {
                int y = minimum + random.nextInt(maximum - minimum + 1);
                placed += replaceOre(
                        level,
                        new BlockPos(x, y, z),
                        ModBlocks.CINNABAR_ORE.get().defaultBlockState()
                );
            }
        }

        for (int attempt = 0;
             attempt < ThaumcraftModernServerConfig.amberAttempts();
             attempt++) {
            int x = chunkMinX + random.nextInt(16);
            int z = chunkMinZ + random.nextInt(16);
            int surface = level.getHeight(
                    Heightmap.Types.OCEAN_FLOOR_WG,
                    x,
                    z
            );
            int y = surface - 1 - random.nextInt(25);
            placed += replaceOre(
                    level,
                    new BlockPos(x, y, z),
                    ModBlocks.AMBER_ORE.get().defaultBlockState()
            );
        }

        int baseInfusedAttempts = ThaumcraftModernServerConfig.infusedStoneAttempts();
        int upperAttempts = InfusedStoneGenerationPolicy.scaledAttemptCount(
                baseInfusedAttempts, InfusedStoneGenerationPolicy.UPPER_PERCENT,
                random);
        int deepslateAttempts = InfusedStoneGenerationPolicy.scaledAttemptCount(
                baseInfusedAttempts, InfusedStoneGenerationPolicy.DEEPSLATE_PERCENT,
                random);
        placed += placeInfusedStoneBand(level, random, chunkMinX, chunkMinZ,
                upperAttempts, 0, Integer.MAX_VALUE);
        placed += placeInfusedStoneBand(level, random, chunkMinX, chunkMinZ,
                deepslateAttempts, level.getMinBuildHeight() + 8, -1);
        return placed > 0;
    }

    private static int placeInfusedStoneBand(WorldGenLevel level,
            RandomSource random, int chunkMinX, int chunkMinZ, int attempts,
            int bandMinimum, int bandMaximum) {
        int placed = 0;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = chunkMinX + random.nextInt(16);
            int z = chunkMinZ + random.nextInt(16);
            int surface = level.getHeight(
                    Heightmap.Types.OCEAN_FLOOR_WG,
                    x,
                    z
            );
            int minimum = Math.max(level.getMinBuildHeight() + 8, bandMinimum);
            int maximum = Math.min(surface - 6, bandMaximum);
            if (maximum < minimum) continue;
            int y = minimum + random.nextInt(maximum - minimum + 1);
            int aspect = random.nextInt(INFUSED_STONES.size());
            if (random.nextInt(3) == 0) {
                aspect = biomeAspect(level.getBiome(new BlockPos(x, y, z)), random);
            }
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
            for (int veinBlock = 0; veinBlock < 6; veinBlock++) {
                placed += replaceInfusedOre(level, cursor, aspect);
                cursor.move(
                        random.nextInt(3) - 1,
                        random.nextInt(3) - 1,
                        random.nextInt(3) - 1
                );
            }
        }
        return placed;
    }

    private static int replaceOre(
            WorldGenLevel level,
            BlockPos position,
            BlockState ore
    ) {
        if (level.isOutsideBuildHeight(position)) {
            return 0;
        }
        BlockState current = level.getBlockState(position);
        if (!current.is(BlockTags.STONE_ORE_REPLACEABLES)
                && !current.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
            return 0;
        }
        return level.setBlock(position, ore, 2) ? 1 : 0;
    }

    private static int replaceInfusedOre(
            WorldGenLevel level,
            BlockPos position,
            int aspect
    ) {
        if (level.isOutsideBuildHeight(position)) {
            return 0;
        }
        BlockState current = level.getBlockState(position);
        if (current.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
            return level.setBlock(
                    position,
                    deepslateInfusedStone(aspect).defaultBlockState(),
                    2
            ) ? 1 : 0;
        }
        if (current.is(BlockTags.STONE_ORE_REPLACEABLES)) {
            return level.setBlock(
                    position,
                    INFUSED_STONES.get(aspect).get().defaultBlockState(),
                    2
            ) ? 1 : 0;
        }
        return 0;
    }

    private static net.minecraft.world.level.block.Block deepslateInfusedStone(
            int aspect
    ) {
        return switch (aspect) {
            case 0 -> ModBlocks.DEEPSLATE_AIR_INFUSED_STONE.get();
            case 1 -> ModBlocks.DEEPSLATE_FIRE_INFUSED_STONE.get();
            case 2 -> ModBlocks.DEEPSLATE_WATER_INFUSED_STONE.get();
            case 3 -> ModBlocks.DEEPSLATE_EARTH_INFUSED_STONE.get();
            case 4 -> ModBlocks.DEEPSLATE_ORDER_INFUSED_STONE.get();
            case 5 -> ModBlocks.DEEPSLATE_ENTROPY_INFUSED_STONE.get();
            default -> throw new IllegalArgumentException("Unknown infused aspect: " + aspect);
        };
    }

    private static int biomeAspect(
            Holder<Biome> biome,
            RandomSource random
    ) {
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) {
            return 2;
        }
        if (biome.is(BiomeTags.IS_MOUNTAIN)
                || biome.is(BiomeTags.IS_HILL)) {
            return random.nextBoolean() ? 0 : 3;
        }
        if (biome.is(BiomeTags.IS_BADLANDS)
                || biome.is(Tags.Biomes.IS_HOT)) {
            return 1;
        }
        if (biome.is(Tags.Biomes.IS_COLD)
                || biome.is(Tags.Biomes.IS_SNOWY)) {
            return 4;
        }
        if (biome.is(Tags.Biomes.IS_SWAMP)
                || biome.is(Tags.Biomes.IS_DRY)) {
            return 5;
        }
        return random.nextInt(6);
    }
}
