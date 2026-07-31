package com.thaumcraftmodern.registry;

import com.mojang.serialization.Codec;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.worldgen.LegacyOverworldBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBiomeSources {
    public static final DeferredRegister<Codec<? extends BiomeSource>> SOURCES =
            DeferredRegister.create(
                    Registries.BIOME_SOURCE,
                    ThaumcraftModern.MOD_ID
            );

    public static final RegistryObject<Codec<? extends BiomeSource>>
            LEGACY_OVERWORLD = SOURCES.register(
                    "legacy_overworld",
                    () -> LegacyOverworldBiomeSource.CODEC
            );

    private ModBiomeSources() {
    }

    public static void register(IEventBus modBus) {
        SOURCES.register(modBus);
    }
}
