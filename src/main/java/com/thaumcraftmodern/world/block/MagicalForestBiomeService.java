package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.worldgen.ModWorldgenKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

/**
 * Runtime Magical-Forest painting performed by TC4 pure Silverwood nodes.
 */
public final class MagicalForestBiomeService {
    private MagicalForestBiomeService() {
    }

    public static boolean isMagicalForest(
            ServerLevel level,
            BlockPos position
    ) {
        if (!level.isLoaded(position)) {
            return false;
        }
        return level.getChunkAt(position).getNoiseBiome(
                QuartPos.fromBlock(position.getX()),
                QuartPos.fromBlock(position.getY()),
                QuartPos.fromBlock(position.getZ())
        ).is(ModWorldgenKeys.MAGICAL_FOREST);
    }

    public static boolean makeColumnMagicalForest(
            ServerLevel level,
            BlockPos position
    ) {
        Holder<Biome> magicalForest = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ModWorldgenKeys.MAGICAL_FOREST);
        return BiomeColumnService.replace(
                level,
                position,
                quartY -> magicalForest
        );
    }
}
