package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.ThaumcraftModern;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Generates and verifies nearby placement candidates one at a time. Unlike
 * vanilla locate, a candidate is accepted only after its marker blocks exist.
 */
public final class LegacyStructureMarkerSearch {
    private static final int MAX_REGION_RADIUS = 100;
    private static final int MAX_CANDIDATES = 512;

    private LegacyStructureMarkerSearch() {
    }

    public static CompletableFuture<Optional<BlockPos>> find(
            ServerLevel level,
            BlockPos origin,
            LegacyStructureKind kind
    ) {
        List<ChunkPos> candidates = candidates(level, origin, kind);
        CompletableFuture<Optional<BlockPos>> result = new CompletableFuture<>();
        checkNext(level, kind, candidates, 0, result);
        return result;
    }

    private static void checkNext(
            ServerLevel level,
            LegacyStructureKind kind,
            List<ChunkPos> candidates,
            int index,
            CompletableFuture<Optional<BlockPos>> result
    ) {
        if (index >= candidates.size()) {
            result.complete(Optional.empty());
            return;
        }
        ChunkPos candidate = candidates.get(index);
        level.getChunkSource().getChunkFuture(
                candidate.x,
                candidate.z,
                ChunkStatus.FULL,
                true
        ).thenAcceptAsync(loaded -> {
            LevelChunk chunk = loaded.left()
                    .filter(LevelChunk.class::isInstance)
                    .map(LevelChunk.class::cast)
                    .orElse(null);
            if (chunk != null) {
                Optional<BlockPos> marker =
                        LegacyStructureMarkerDetector.findMarker(
                                level,
                                chunk,
                                kind
                        );
                if (marker.isPresent()) {
                    result.complete(marker);
                    return;
                }
            }
            checkNext(level, kind, candidates, index + 1, result);
        }, level.getServer()).exceptionally(exception -> {
            result.completeExceptionally(exception);
            return null;
        });
    }

    private static List<ChunkPos> candidates(
            ServerLevel level,
            BlockPos origin,
            LegacyStructureKind kind
    ) {
        ResourceKey<Structure> key = ResourceKey.create(
                Registries.STRUCTURE,
                new ResourceLocation(
                        ThaumcraftModern.MOD_ID,
                        kind.serializedName()
                )
        );
        Holder<Structure> structure = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getHolder(key)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing structure " + key.location()
                ));
        ChunkGeneratorStructureState state =
                level.getChunkSource().getGeneratorState();
        Set<Long> unique = new HashSet<>();
        List<ChunkPos> candidates = new ArrayList<>();
        for (StructurePlacement placement
                : state.getPlacementsForStructure(structure)) {
            if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
                continue;
            }
            int originRegionX = Math.floorDiv(origin.getX() >> 4, spread.spacing());
            int originRegionZ = Math.floorDiv(origin.getZ() >> 4, spread.spacing());
            for (int radius = 0; radius <= MAX_REGION_RADIUS
                    && candidates.size() < MAX_CANDIDATES; radius++) {
                for (int dx = -radius; dx <= radius
                        && candidates.size() < MAX_CANDIDATES; dx++) {
                    for (int dz = -radius; dz <= radius
                            && candidates.size() < MAX_CANDIDATES; dz++) {
                        if (radius > 0
                                && Math.abs(dx) != radius
                                && Math.abs(dz) != radius) {
                            continue;
                        }
                        ChunkPos candidate = spread.getPotentialStructureChunk(
                                state.getLevelSeed(),
                                originRegionX + dx,
                                originRegionZ + dz
                        );
                        if (placement.isStructureChunk(
                                state,
                                candidate.x,
                                candidate.z
                        ) && unique.add(candidate.toLong())) {
                            candidates.add(candidate);
                        }
                    }
                }
            }
        }
        candidates.sort(Comparator.comparingLong(candidate -> {
            long x = candidate.getMiddleBlockX() - (long) origin.getX();
            long z = candidate.getMiddleBlockZ() - (long) origin.getZ();
            return x * x + z * z;
        }));
        return candidates;
    }
}
