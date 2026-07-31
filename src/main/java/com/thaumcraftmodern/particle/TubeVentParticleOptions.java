package com.thaumcraftmodern.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thaumcraftmodern.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

/** Carries TC4's aspect RGB value to the client vent particle. */
public record TubeVentParticleOptions(int color) implements ParticleOptions {
    public static final Codec<TubeVentParticleOptions> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(
                            TubeVentParticleOptions::color
                    )
            ).apply(instance, TubeVentParticleOptions::new));

    public static final Deserializer<TubeVentParticleOptions> DESERIALIZER =
            new Deserializer<>() {
                @Override
                public TubeVentParticleOptions fromCommand(
                        ParticleType<TubeVentParticleOptions> type,
                        StringReader reader
                ) throws CommandSyntaxException {
                    reader.expect(' ');
                    return new TubeVentParticleOptions(reader.readInt());
                }

                @Override
                public TubeVentParticleOptions fromNetwork(
                        ParticleType<TubeVentParticleOptions> type,
                        FriendlyByteBuf buffer
                ) {
                    return new TubeVentParticleOptions(buffer.readInt());
                }
            };

    @Override
    public ParticleType<?> getType() {
        return ModParticles.TUBE_VENT.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeInt(color);
    }

    @Override
    public String writeToString() {
        return String.format(
                Locale.ROOT,
                "%s %d",
                BuiltInRegistries.PARTICLE_TYPE.getKey(getType()),
                color
        );
    }
}
