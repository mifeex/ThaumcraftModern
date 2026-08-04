package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import com.thaumcraftmodern.particle.TubeVentParticleOptions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(
                    ForgeRegistries.PARTICLE_TYPES,
                    ThaumcraftModern.MOD_ID
            );

    public static final RegistryObject<SimpleParticleType> NODE_BURST =
            PARTICLES.register(
                    "node_burst",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> NITOR_WISP_LARGE =
            PARTICLES.register(
                    "nitor_wisp_large",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> NITOR_WISP_SMALL =
            PARTICLES.register(
                    "nitor_wisp_small",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> ELDRITCH_HEAL =
            PARTICLES.register(
                    "eldritch_heal",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> TRAVEL_SPARKLE =
            PARTICLES.register(
                    "travel_sparkle",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> WARDING_RUNE_ACTIVE =
            PARTICLES.register(
                    "warding_rune_active",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> WARDING_RUNE_DISABLED =
            PARTICLES.register(
                    "warding_rune_disabled",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> WARDING_RUNE_BLOCKED =
            PARTICLES.register(
                    "warding_rune_blocked",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> CRUCIBLE_BUBBLE =
            PARTICLES.register(
                    "crucible_bubble",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> CRUCIBLE_FROTH =
            PARTICLES.register(
                    "crucible_froth",
                    () -> new SimpleParticleType(false)
            );
    public static final RegistryObject<ParticleType<TubeVentParticleOptions>> TUBE_VENT =
            PARTICLES.register(
                    "tube_vent",
                    () -> new ParticleType<>(
                            false,
                            TubeVentParticleOptions.DESERIALIZER
                    ) {
                        @Override
                        public com.mojang.serialization.Codec<TubeVentParticleOptions> codec() {
                            return TubeVentParticleOptions.CODEC;
                        }
                    }
            );

    private ModParticles() {
    }

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
