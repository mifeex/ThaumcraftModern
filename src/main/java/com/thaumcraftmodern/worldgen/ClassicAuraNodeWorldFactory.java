package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.ClassicAuraNodeGeneration;
import com.thaumcraftmodern.aura.LegacyUniformDarkNodeMigration;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Adapts modern biome/block data to TC4's biome-aura generation inputs.
 */
public final class ClassicAuraNodeWorldFactory {
    private static final long LEGACY_MIGRATION_SALT = 0x5443344441524B4CL;
    private static final List<String> FALLBACK_COMPOUNDS = List.of(
            "alienis", "arbor", "auram", "bestia", "cognitio", "corpus",
            "exanimis", "fabrico", "fames", "gelum", "herba", "humanus",
            "instrumentum", "iter", "limus", "lucrum", "lux", "machina",
            "messis", "metallum", "meto", "mortuus", "motus", "pannus",
            "permutatio", "perfodio", "potentia", "praecantatio", "sano",
            "sensus", "spiritus", "telum", "tempestas", "tenebrae",
            "tutamen", "vacuos", "venenum", "vinculum", "vitium", "vitreus",
            "victus", "volatus"
    );

    private ClassicAuraNodeWorldFactory() {
    }

    public static void migrateLegacyUniformDark(
            ServerLevel level,
            BlockPos position,
            com.thaumcraftmodern.aura.AuraNodeBlockEntity node
    ) {
        AuraNodeState.Snapshot legacy = node.snapshotState().snapshot();
        if (!LegacyUniformDarkNodeMigration.matches(legacy)) {
            return;
        }
        RandomSource random = RandomSource.create(
                level.getSeed() ^ position.asLong() ^ LEGACY_MIGRATION_SALT
        );
        AuraNodeState generated = createEerie(level, position, random);
        node.migrateLegacyUniformDark(
                LegacyUniformDarkNodeMigration.replacement(
                        legacy,
                        generated.snapshot()
                )
        );
    }

    static AuraNodeState create(
            WorldGenLevel level,
            BlockPos position,
            RandomSource random
    ) {
        return create(level, position, random, false, false);
    }

    static AuraNodeState createEerie(
            WorldGenLevel level,
            BlockPos position,
            RandomSource random
    ) {
        return create(level, position, random, false, true);
    }

    static AuraNodeState createSilverwood(
            WorldGenLevel level,
            BlockPos position,
            RandomSource random
    ) {
        return create(level, position, random, true, false);
    }

    private static AuraNodeState create(
            WorldGenLevel level,
            BlockPos position,
            RandomSource random,
            boolean forcedPure,
            boolean forcedEerie
    ) {
        Surroundings surroundings = scan(level, position);
        boolean tainted = level.getBiome(position).is(
                ModWorldgenKeys.TAINTED_LANDS
        );
        boolean eerie = forcedEerie
                || level.getBiome(position).is(ModWorldgenKeys.EERIE);
        String stableId = level.getLevel().dimension().location() + ":"
                + position.getX() + ":" + position.getY() + ":"
                + position.getZ();
        return ClassicAuraNodeGeneration.generate(
                UUID.nameUUIDFromBytes(stableId.getBytes(StandardCharsets.UTF_8)),
                random,
                new ClassicAuraNodeGeneration.Environment(
                        biomeAffinities(level.getBiome(position)),
                        tainted,
                        forcedPure,
                        eerie,
                        false,
                        surroundings.water(),
                        surroundings.lava(),
                        surroundings.stone(),
                        surroundings.foliage()
                ),
                primalIds(),
                compoundIds()
        );
    }

    private static List<ClassicAuraNodeGeneration.BiomeAffinity>
            biomeAffinities(Holder<Biome> biome) {
        List<ClassicAuraNodeGeneration.BiomeAffinity> result =
                new ArrayList<>();

        add(result, biome.is(Tags.Biomes.IS_WATER), 100, "aqua");
        add(result, biome.is(BiomeTags.IS_OCEAN), 120, "aqua");
        add(result, biome.is(BiomeTags.IS_RIVER), 100, "aqua");
        add(result, biome.is(Tags.Biomes.IS_WET), 80, "aqua");

        add(result, biome.is(Tags.Biomes.IS_HOT), 100, "ignis");
        add(result, biome.is(Tags.Biomes.IS_DESERT), 100, "ignis");
        add(result, biome.is(BiomeTags.IS_NETHER), 120, "ignis");
        add(result, biome.is(BiomeTags.IS_BADLANDS), 80, "ignis");

        add(result, biome.is(Tags.Biomes.IS_DENSE), 100, "ordo");
        add(result, biome.is(Tags.Biomes.IS_SNOWY), 80, "ordo");
        add(result, biome.is(Tags.Biomes.IS_COLD), 80, "ordo");
        add(result, biome.is(Tags.Biomes.IS_MUSHROOM), 140, "ordo");

        add(result, biome.is(Tags.Biomes.IS_CONIFEROUS), 100, "terra");
        add(result, biome.is(BiomeTags.IS_FOREST), 120, "terra");
        add(result, biome.is(Tags.Biomes.IS_SANDY), 80, "terra");
        add(result, biome.is(BiomeTags.IS_BEACH), 80, "terra");

        add(result, biome.is(BiomeTags.IS_SAVANNA), 80, "aer");
        add(result, biome.is(BiomeTags.IS_MOUNTAIN), 100, "aer");
        add(result, biome.is(BiomeTags.IS_HILL), 120, "aer");
        add(result, biome.is(Tags.Biomes.IS_PLAINS), 80, "aer");

        add(result, biome.is(Tags.Biomes.IS_DRY), 80, "perditio");
        add(result, biome.is(Tags.Biomes.IS_SPARSE), 80, "perditio");
        add(result, biome.is(Tags.Biomes.IS_SWAMP), 120, "perditio");
        add(result, biome.is(Tags.Biomes.IS_WASTELAND), 80, "perditio");

        add(result, biome.is(BiomeTags.IS_JUNGLE), 100, "herba");
        add(result, biome.is(Tags.Biomes.IS_LUSH), 100, "herba");
        add(result, biome.is(Tags.Biomes.IS_MAGICAL), 100, null);
        add(result, biome.is(BiomeTags.IS_END), 80, "vacuos");
        add(result, biome.is(Tags.Biomes.IS_SPOOKY), 80, "spiritus");
        add(result, biome.is(Tags.Biomes.IS_DEAD), 50, "mortuus");

        /*
         * The project's own biomes must retain their TC4 dictionary traits
         * even when a third-party datapack replaces Forge biome tags.
         */
        addMissingCustomAffinities(result, biome);
        if (result.isEmpty()) {
            result.add(new ClassicAuraNodeGeneration.BiomeAffinity(100, null));
        }
        return List.copyOf(result);
    }

    private static void addMissingCustomAffinities(
            List<ClassicAuraNodeGeneration.BiomeAffinity> affinities,
            Holder<Biome> biome
    ) {
        if (biome.is(ModWorldgenKeys.MAGICAL_FOREST)) {
            addIfMissing(affinities, 100, null);
            addIfMissing(affinities, 120, "terra");
            addIfMissing(affinities, 100, "herba");
        } else if (biome.is(ModWorldgenKeys.TAINTED_LANDS)) {
            addIfMissing(affinities, 100, null);
            addIfMissing(affinities, 80, "perditio");
        } else if (biome.is(ModWorldgenKeys.EERIE)) {
            addIfMissing(affinities, 100, null);
            addIfMissing(affinities, 80, "spiritus");
        } else if (biome.is(ModWorldgenKeys.ELDRITCH)) {
            addIfMissing(affinities, 100, null);
            addIfMissing(affinities, 80, "spiritus");
            addIfMissing(affinities, 80, "vacuos");
        }
    }

    private static void add(
            List<ClassicAuraNodeGeneration.BiomeAffinity> affinities,
            boolean matches,
            int aura,
            String aspect
    ) {
        if (matches) {
            affinities.add(
                    new ClassicAuraNodeGeneration.BiomeAffinity(aura, aspect)
            );
        }
    }

    private static void addIfMissing(
            List<ClassicAuraNodeGeneration.BiomeAffinity> affinities,
            int aura,
            String aspect
    ) {
        ClassicAuraNodeGeneration.BiomeAffinity candidate =
                new ClassicAuraNodeGeneration.BiomeAffinity(aura, aspect);
        if (!affinities.contains(candidate)) {
            affinities.add(candidate);
        }
    }

    private static Surroundings scan(
            WorldGenLevel level,
            BlockPos center
    ) {
        int water = 0;
        int lava = 0;
        int stone = 0;
        int foliage = 0;
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockState state = level.getBlockState(
                            center.offset(x, y, z)
                    );
                    if (state.getFluidState().is(FluidTags.WATER)) {
                        water++;
                    } else if (state.getFluidState().is(FluidTags.LAVA)) {
                        lava++;
                    } else if (state.is(Blocks.STONE)) {
                        stone++;
                    }
                    if (state.is(BlockTags.LEAVES)) {
                        foliage++;
                    }
                }
            }
        }
        return new Surroundings(water, lava, stone, foliage);
    }

    private static List<String> primalIds() {
        return PrimalAspect.ordered().stream().map(PrimalAspect::id).toList();
    }

    private static List<String> compoundIds() {
        try {
            List<String> loaded = AspectRegistryRuntime.catalog().definitions()
                    .stream()
                    .filter(AspectDefinition::isCompound)
                    .map(AspectDefinition::id)
                    .toList();
            return loaded.isEmpty() ? FALLBACK_COMPOUNDS : loaded;
        } catch (IllegalStateException ignored) {
            return FALLBACK_COMPOUNDS;
        }
    }

    private record Surroundings(
            int water,
            int lava,
            int stone,
            int foliage
    ) {
    }
}
