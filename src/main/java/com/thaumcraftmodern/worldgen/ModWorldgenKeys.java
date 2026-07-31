package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class ModWorldgenKeys {
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE =
            configured("greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE =
            configured("silverwood_tree");

    public static final ResourceKey<PlacedFeature> ORES =
            placed("legacy_ores");
    public static final ResourceKey<PlacedFeature> VEGETATION =
            placed("legacy_vegetation");
    public static final ResourceKey<PlacedFeature> STRUCTURES =
            placed("legacy_structures");

    public static final ResourceKey<Biome> MAGICAL_FOREST =
            biome("magical_forest");
    public static final ResourceKey<Biome> TAINTED_LANDS =
            biome("tainted_lands");
    public static final ResourceKey<Biome> EERIE = biome("eerie");
    public static final ResourceKey<Biome> ELDRITCH = biome("eldritch");
    public static final TagKey<Biome> HAS_HILLTOP_STONES =
            TagKey.create(
                    Registries.BIOME,
                    new ResourceLocation(
                            ThaumcraftModern.MOD_ID,
                            "has_hilltop_stones"
                    )
            );

    private ModWorldgenKeys() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String id) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                new ResourceLocation(ThaumcraftModern.MOD_ID, id)
        );
    }

    private static ResourceKey<PlacedFeature> placed(String id) {
        return ResourceKey.create(
                Registries.PLACED_FEATURE,
                new ResourceLocation(ThaumcraftModern.MOD_ID, id)
        );
    }

    private static ResourceKey<Biome> biome(String id) {
        return ResourceKey.create(
                Registries.BIOME,
                new ResourceLocation(ThaumcraftModern.MOD_ID, id)
        );
    }
}
