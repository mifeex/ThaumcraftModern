package com.thaumcraftmodern.aura;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Runtime-independent port of TC4 4.2.3.5
 * {@code ThaumcraftWorldGenerator#createRandomNodeAt}.
 */
public final class ClassicAuraNodeGeneration {
    public static final int SPECIAL_NODE_RARITY = 18;

    private ClassicAuraNodeGeneration() {
    }

    public static AuraNodeState generate(
            UUID nodeId,
            RandomSource random,
            Environment environment,
            List<String> primalAspects,
            List<String> compoundAspects
    ) {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(environment, "environment");
        List<String> primals = requireCatalog(primalAspects, "primalAspects");
        List<String> compounds = requireCatalog(
                compoundAspects,
                "compoundAspects"
        );

        AuraNodeType type = chooseType(random, environment);
        AuraNodeModifier modifier = chooseModifier(random);
        int aura = Math.max(2, environment.averageBiomeAura());
        if (environment.taintedBiome() && !environment.silverwood()) {
            /*
             * Ambient nodes in Tainted Land are always tainted. Keep the
             * 2.25x aura used by TC4 whenever its 50% conversion succeeded,
             * while making the biome invariant explicit for world generation.
             */
            type = AuraNodeType.TAINTED;
            aura = Math.max(2, (int) (aura * 2.25F));
        }
        if (environment.silverwood() || environment.small()) {
            aura = Math.max(2, aura / 4);
        }

        int halfAura = Math.max(1, aura / 2);
        int value = random.nextInt(halfAura) + halfAura;
        LinkedHashMap<String, Integer> weights = new LinkedHashMap<>();
        String biomeAspect = environment.randomBiomeAspect(random);
        if (biomeAspect != null) {
            merge(weights, biomeAspect, 2);
        } else {
            merge(weights, randomFrom(compounds, random), 1);
            merge(weights, randomFrom(primals, random), 1);
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            if (!random.nextBoolean()) {
                continue;
            }
            String aspect = random.nextInt(SPECIAL_NODE_RARITY) == 0
                    ? randomFrom(compounds, random)
                    : randomFrom(primals, random);
            merge(weights, aspect, 1);
        }

        switch (type) {
            case HUNGRY -> {
                merge(weights, "fames", 2);
                if (random.nextBoolean()) {
                    merge(weights, "lucrum", 1);
                }
            }
            case PURE -> merge(
                    weights,
                    random.nextBoolean() ? "victus" : "ordo",
                    2
            );
            case DARK -> {
                mergeRandomly(weights, "mortuus", random);
                mergeRandomly(weights, "exanimis", random);
                mergeRandomly(weights, "perditio", random);
                mergeRandomly(weights, "tenebrae", random);
            }
            case TAINTED -> merge(weights, "vitium", 2);
            default -> {
            }
        }

        if (environment.waterBlocks() > 100) {
            merge(weights, "aqua", 1);
        }
        if (environment.lavaBlocks() > 100) {
            merge(weights, "ignis", 1);
            merge(weights, "terra", 1);
        }
        if (environment.stoneBlocks() > 500) {
            merge(weights, "terra", 1);
        }
        if (environment.foliageBlocks() > 100) {
            merge(weights, "herba", 1);
        }

        List<String> aspects = new ArrayList<>(weights.keySet());
        int[] spread = new int[aspects.size()];
        float total = 0.0F;
        for (int index = 0; index < aspects.size(); index++) {
            spread[index] = weights.get(aspects.get(index)) == 2
                    ? 50 + random.nextInt(25)
                    : 25 + random.nextInt(50);
            total += spread[index];
        }

        LinkedHashMap<String, Integer> pools = new LinkedHashMap<>();
        for (int index = 0; index < aspects.size(); index++) {
            String aspect = aspects.get(index);
            int amount = weights.get(aspect)
                    + (int) (spread[index] / total * value);
            pools.put(aspect, Math.max(1, amount));
        }
        return AuraNodeState.withAspects(
                nodeId,
                type,
                modifier,
                pools,
                pools,
                0L
        );
    }

    private static AuraNodeType chooseType(
            RandomSource random,
            Environment environment
    ) {
        if (environment.silverwood()) {
            return AuraNodeType.PURE;
        }
        if (environment.eerie()) {
            return AuraNodeType.DARK;
        }
        if (random.nextInt(SPECIAL_NODE_RARITY) != 0) {
            return AuraNodeType.NORMAL;
        }
        return switch (random.nextInt(10)) {
            case 0, 1, 2 -> AuraNodeType.DARK;
            case 3, 4, 5 -> AuraNodeType.UNSTABLE;
            case 6, 7, 8 -> AuraNodeType.PURE;
            default -> AuraNodeType.HUNGRY;
        };
    }

    private static AuraNodeModifier chooseModifier(RandomSource random) {
        if (random.nextInt(SPECIAL_NODE_RARITY / 2) != 0) {
            return AuraNodeModifier.NORMAL;
        }
        return switch (random.nextInt(3)) {
            case 0 -> AuraNodeModifier.BRIGHT;
            case 1 -> AuraNodeModifier.PALE;
            default -> AuraNodeModifier.FADING;
        };
    }

    private static List<String> requireCatalog(
            List<String> source,
            String field
    ) {
        Objects.requireNonNull(source, field);
        if (source.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return List.copyOf(source);
    }

    private static String randomFrom(
            List<String> aspects,
            RandomSource random
    ) {
        return aspects.get(random.nextInt(aspects.size()));
    }

    private static void merge(
            Map<String, Integer> aspects,
            String aspect,
            int amount
    ) {
        if (aspect != null && !aspect.isBlank()) {
            aspects.merge(aspect, amount, Integer::sum);
        }
    }

    private static void mergeRandomly(
            Map<String, Integer> aspects,
            String aspect,
            RandomSource random
    ) {
        if (random.nextBoolean()) {
            merge(aspects, aspect, 1);
        }
    }

    public record Environment(
            List<BiomeAffinity> biomeAffinities,
            boolean taintedBiome,
            boolean silverwood,
            boolean eerie,
            boolean small,
            int waterBlocks,
            int lavaBlocks,
            int stoneBlocks,
            int foliageBlocks
    ) {
        public Environment {
            Objects.requireNonNull(biomeAffinities, "biomeAffinities");
            if (biomeAffinities.isEmpty()) {
                throw new IllegalArgumentException(
                        "biomeAffinities cannot be empty"
                );
            }
            biomeAffinities = List.copyOf(biomeAffinities);
        }

        public Environment(
                int biomeAura,
                String biomeAspect,
                boolean taintedBiome,
                boolean silverwood,
                boolean eerie,
                boolean small,
                int waterBlocks,
                int lavaBlocks,
                int stoneBlocks,
                int foliageBlocks
        ) {
            this(
                    List.of(new BiomeAffinity(biomeAura, biomeAspect)),
                    taintedBiome,
                    silverwood,
                    eerie,
                    small,
                    waterBlocks,
                    lavaBlocks,
                    stoneBlocks,
                    foliageBlocks
            );
        }

        int averageBiomeAura() {
            return biomeAffinities.stream()
                    .mapToInt(BiomeAffinity::aura)
                    .sum() / biomeAffinities.size();
        }

        String randomBiomeAspect(RandomSource random) {
            return biomeAffinities.get(
                    random.nextInt(biomeAffinities.size())
            ).aspect();
        }
    }

    /**
     * One modern biome tag adapted to one TC4 BiomeDictionary entry.
     * Multiple matching entries are intentionally retained: TC4 selected one
     * matching biome type uniformly for the node's dominant aspect and
     * averaged all matching aura values.
     */
    public record BiomeAffinity(int aura, String aspect) {
        public BiomeAffinity {
            if (aura < 2) {
                throw new IllegalArgumentException("aura must be at least 2");
            }
            if (aspect != null && aspect.isBlank()) {
                aspect = null;
            }
        }
    }
}
