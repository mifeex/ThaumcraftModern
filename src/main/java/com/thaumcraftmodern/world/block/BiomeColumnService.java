package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

/**
 * Server-owned mutation of one modern 4x4 biome column.
 */
final class BiomeColumnService {
    private BiomeColumnService() {
    }

    static boolean replace(
            ServerLevel level,
            BlockPos position,
            BiomeAtY replacement
    ) {
        if (!level.isLoaded(position)) {
            return false;
        }
        LevelChunk chunk = level.getChunkAt(position);
        int quartX = QuartPos.fromBlock(position.getX());
        int quartZ = QuartPos.fromBlock(position.getZ());
        boolean changed = false;
        int minQuartY = QuartPos.fromBlock(level.getMinBuildHeight());
        int maxQuartY = QuartPos.fromBlock(level.getMaxBuildHeight() - 1);
        for (int quartY = minQuartY; quartY <= maxQuartY; quartY++) {
            if (!chunk.getNoiseBiome(quartX, quartY, quartZ)
                    .equals(replacement.at(quartY))) {
                changed = true;
                break;
            }
        }
        if (!changed) {
            return false;
        }
        chunk.fillBiomesFromNoise(
                (sampleX, sampleY, sampleZ, sampler) -> {
                    Holder<Biome> current = chunk.getNoiseBiome(
                            sampleX,
                            sampleY,
                            sampleZ
                    );
                    if (sampleX == quartX && sampleZ == quartZ) {
                        return replacement.at(sampleY);
                    }
                    return current;
                },
                level.getChunkSource().randomState().sampler()
        );
        chunk.setUnsaved(true);
        level.getChunkSource().chunkMap.resendBiomesForChunks(
                List.of(chunk)
        );
        return true;
    }

    @FunctionalInterface
    interface BiomeAtY {
        Holder<Biome> at(int quartY);
    }
}
