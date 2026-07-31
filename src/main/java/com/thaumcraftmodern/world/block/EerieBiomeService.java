package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.worldgen.ModWorldgenKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

/**
 * Runtime Eerie-biome painting performed by TC4 sinister aura nodes.
 */
public final class EerieBiomeService {
    private EerieBiomeService() {
    }

    public static boolean isEerie(
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
        ).is(ModWorldgenKeys.EERIE);
    }

    public static boolean makeColumnEerie(
            ServerLevel level,
            BlockPos position
    ) {
        Holder<Biome> eerie = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ModWorldgenKeys.EERIE);
        return BiomeColumnService.replace(
                level,
                position,
                quartY -> eerie
        );
    }
}
