package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.worldgen.AuraNodeFeature;
import com.thaumcraftmodern.worldgen.GreatwoodTreeFeature;
import com.thaumcraftmodern.worldgen.LegacyOreFeature;
import com.thaumcraftmodern.worldgen.LegacyStructuresFeature;
import com.thaumcraftmodern.worldgen.LegacyVegetationFeature;
import com.thaumcraftmodern.worldgen.SilverwoodTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>>
            AURA_NODE = FEATURES.register("aura_node", AuraNodeFeature::new);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>>
            LEGACY_ORES = FEATURES.register(
                    "legacy_ores",
                    LegacyOreFeature::new
            );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>>
            GREATWOOD_TREE = FEATURES.register(
                    "greatwood_tree",
                    GreatwoodTreeFeature::new
            );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>>
            SILVERWOOD_TREE = FEATURES.register(
                    "silverwood_tree",
                    SilverwoodTreeFeature::new
            );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>>
            LEGACY_VEGETATION = FEATURES.register(
                    "legacy_vegetation",
                    LegacyVegetationFeature::new
            );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>>
            LEGACY_STRUCTURES = FEATURES.register(
                    "legacy_structures",
                    LegacyStructuresFeature::new
            );

    private ModFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
