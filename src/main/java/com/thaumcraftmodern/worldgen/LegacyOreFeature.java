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

        for (int attempt = 0;
             attempt < ThaumcraftModernServerConfig.infusedStoneAttempts();
             attempt++) {
            int x = chunkMinX + random.nextInt(16);
            int z = chunkMinZ + random.nextInt(16);
            int surface = level.getHeight(
                    Heightmap.Types.OCEAN_FLOOR_WG,
                    x,
                    z
            );
            int minimum = level.getMinBuildHeight() + 8;
            int maximum = Math.max(minimum, surface - 6);
            int y = minimum + random.nextInt(maximum - minimum + 1);
            int aspect = random.nextInt(INFUSED_STONES.size());
            if (random.nextInt(3) == 0) {
                aspect = biomeAspect(level.getBiome(new BlockPos(x, y, z)), random);
            }
            BlockState ore = INFUSED_STONES.get(aspect)
                    .get()
                    .defaultBlockState();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
            for (int veinBlock = 0; veinBlock < 6; veinBlock++) {
                placed += replaceOre(level, cursor, ore);
                cursor.move(
                        random.nextInt(3) - 1,
                        random.nextInt(3) - 1,
                        random.nextInt(3) - 1
                );
            }
        }
        return placed > 0;
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
