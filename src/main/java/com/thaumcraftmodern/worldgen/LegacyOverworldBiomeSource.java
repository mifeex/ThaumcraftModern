package com.thaumcraftmodern.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Pair;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraftforge.common.Tags;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Surface-only TC4 biome overlay for the opt-in Thaumcraft Modern world
 * preset. The vanilla multi-noise source remains responsible for terrain,
 * oceans and all modern cave biomes.
 */
public final class LegacyOverworldBiomeSource extends BiomeSource {
    public static final Codec<LegacyOverworldBiomeSource> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("delegate")
                            .forGetter(source -> source.delegate),
                    Biome.CODEC.fieldOf("magical_forest")
                            .forGetter(source -> source.magicalForest),
                    Biome.CODEC.fieldOf("tainted_lands")
                            .forGetter(source -> source.taintedLands),
                    Biome.CODEC.fieldOf("eerie")
                            .forGetter(source -> source.eerie)
            ).apply(instance, LegacyOverworldBiomeSource::new));

    private final BiomeSource delegate;
    private final Holder<Biome> magicalForest;
    private final Holder<Biome> taintedLands;
    private final Holder<Biome> eerie;

    public LegacyOverworldBiomeSource(
            BiomeSource delegate,
            Holder<Biome> magicalForest,
            Holder<Biome> taintedLands,
            Holder<Biome> eerie
    ) {
        this.delegate = delegate;
        this.magicalForest = magicalForest;
        this.taintedLands = taintedLands;
        this.eerie = eerie;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(
                delegate.possibleBiomes().stream(),
                Stream.of(magicalForest, taintedLands, eerie)
        ).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(
            int quartX,
            int quartY,
            int quartZ,
            Climate.Sampler sampler
    ) {
        Holder<Biome> original = delegate.getNoiseBiome(
                quartX,
                quartY,
                quartZ,
                sampler
        );
        if (!ThaumcraftModernServerConfig.generateTc4Biomes()
                || preserveOriginal(original)) {
            return original;
        }
        Holder<Biome> overlay = overlayCandidate(quartX, quartZ, sampler);
        return overlay == null || !supportsOverlayClimate(original, overlay)
                ? original
                : overlay;
    }

    /**
     * Vanilla's three-dimensional locate scan repeatedly evaluates the
     * expensive multi-noise delegate at every X/Y/Z sample. TC4 overlay
     * biomes depend only on X/Z, so locate their candidate patches first and
     * consult the delegate only to reject protected ocean/river positions.
     */
    @Override
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
            BlockPos origin,
            int radius,
            int horizontalStep,
            int verticalStep,
            Predicate<Holder<Biome>> predicate,
            Climate.Sampler sampler,
            LevelReader level
    ) {
        boolean wantsOverlay = predicate.test(magicalForest)
                || predicate.test(taintedLands)
                || predicate.test(eerie);
        boolean wantsDelegate = delegate.possibleBiomes().stream()
                .anyMatch(predicate);
        if (!wantsOverlay || wantsDelegate) {
            return super.findClosestBiome3d(
                    origin,
                    radius,
                    horizontalStep,
                    verticalStep,
                    predicate,
                    sampler,
                    level
            );
        }

        int y = Math.max(0, Math.min(
                origin.getY(),
                level.getMaxBuildHeight() - 1
        ));
        for (int distance = 0; distance <= radius; distance += horizontalStep) {
            Pair<BlockPos, Holder<Biome>> found = findOverlayOnRing(
                    origin,
                    y,
                    distance,
                    horizontalStep,
                    predicate,
                    sampler
            );
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Pair<BlockPos, Holder<Biome>> findOverlayOnRing(
            BlockPos origin,
            int y,
            int distance,
            int step,
            Predicate<Holder<Biome>> predicate,
            Climate.Sampler sampler
    ) {
        if (distance == 0) {
            return overlayAt(origin.getX(), y, origin.getZ(), predicate, sampler);
        }
        for (int offset = -distance; offset <= distance; offset += step) {
            Pair<BlockPos, Holder<Biome>> found = overlayAt(
                    origin.getX() + offset,
                    y,
                    origin.getZ() - distance,
                    predicate,
                    sampler
            );
            if (found != null) {
                return found;
            }
            found = overlayAt(
                    origin.getX() + offset,
                    y,
                    origin.getZ() + distance,
                    predicate,
                    sampler
            );
            if (found != null) {
                return found;
            }
        }
        for (int offset = -distance + step;
             offset < distance;
             offset += step) {
            Pair<BlockPos, Holder<Biome>> found = overlayAt(
                    origin.getX() - distance,
                    y,
                    origin.getZ() + offset,
                    predicate,
                    sampler
            );
            if (found != null) {
                return found;
            }
            found = overlayAt(
                    origin.getX() + distance,
                    y,
                    origin.getZ() + offset,
                    predicate,
                    sampler
            );
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Pair<BlockPos, Holder<Biome>> overlayAt(
            int blockX,
            int blockY,
            int blockZ,
            Predicate<Holder<Biome>> predicate,
            Climate.Sampler sampler
    ) {
        int quartX = QuartPos.fromBlock(blockX);
        int quartY = QuartPos.fromBlock(blockY);
        int quartZ = QuartPos.fromBlock(blockZ);
        Holder<Biome> candidate = overlayCandidate(
                quartX,
                quartZ,
                sampler
        );
        if (candidate == null || !predicate.test(candidate)) {
            return null;
        }

        Holder<Biome> original = delegate.getNoiseBiome(
                quartX,
                quartY,
                quartZ,
                sampler
        );
        if (preserveOriginal(original)) {
            return null;
        }
        if (!supportsOverlayClimate(original, candidate)) {
            return null;
        }
        return Pair.of(new BlockPos(blockX, blockY, blockZ), candidate);
    }

    private static boolean supportsOverlayClimate(
            Holder<Biome> original,
            Holder<Biome> overlay
    ) {
        if (!overlay.is(ModWorldgenKeys.MAGICAL_FOREST)) {
            return true;
        }
        return MagicalForestGenerationPolicy.supportsClimate(
                original.value().getBaseTemperature(),
                original.is(Tags.Biomes.IS_COLD)
        );
    }

    private Holder<Biome> overlayCandidate(
            int quartX,
            int quartZ,
            Climate.Sampler sampler
    ) {
        if (!ThaumcraftModernServerConfig.generateTc4Biomes()) {
            return null;
        }
        /*
         * Reuse the seeded 1.20 climate router instead of painting a
         * seed-independent two-dimensional value-noise mask. Sampling at
         * quart Y=0 keeps each surface biome column coherent while
         * weirdness/erosion retain vanilla's large modern climate regions.
         */
        Climate.TargetPoint climate = sampler.sample(quartX, 0, quartZ);
        double primary = Climate.unquantizeCoord(climate.weirdness());
        int magicalWeight = ThaumcraftModernServerConfig.magicalForestWeight();
        int taintWeight = ThaumcraftModernServerConfig.taintedLandsWeight();
        double magicalThreshold = 1.0D - Math.min(
                0.30D,
                magicalWeight * 0.018D
        );
        if (primary >= magicalThreshold) {
            return magicalForest;
        }
        double taintField = Climate.unquantizeCoord(climate.erosion());
        if (taintField <= TaintedLandsGenerationPolicy.patchThreshold(
                taintWeight
        )) {
            return taintedLands;
        }
        /*
         * TC4 did not climate-generate Eerie. Sinister nodes paint it into
         * the live chunk biome data, so unrelated random Eerie pockets must
         * not originate here.
         */
        return null;
    }

    private static boolean preserveOriginal(Holder<Biome> original) {
        if (original.is(BiomeTags.IS_OCEAN)
                || original.is(BiomeTags.IS_RIVER)
                || original.is(BiomeTags.IS_NETHER)
                || original.is(BiomeTags.IS_END)) {
            return true;
        }
        return original.is(Biomes.LUSH_CAVES)
                        || original.is(Biomes.DRIPSTONE_CAVES)
                        || original.is(Biomes.DEEP_DARK);
    }

}
