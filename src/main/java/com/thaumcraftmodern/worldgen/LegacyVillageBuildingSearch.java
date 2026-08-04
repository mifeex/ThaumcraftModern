package com.thaumcraftmodern.worldgen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/** Locates actual TC4 profession buildings planned inside vanilla villages. */
public final class LegacyVillageBuildingSearch {
    private static final int MAX_REGION_RADIUS = 64;
    private static final int MAX_CANDIDATES = 128;

    private LegacyVillageBuildingSearch() {
    }

    public static CompletableFuture<Optional<BlockPos>> find(
            ServerLevel level,
            BlockPos origin,
            LegacyStructureKind kind
    ) {
        if (!kind.isVillageBuilding()) {
            throw new IllegalArgumentException(kind + " is not a village building");
        }
        CompletableFuture<Optional<BlockPos>> result = new CompletableFuture<>();
        checkNext(level, kind, candidates(level, origin), 0, result);
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
            Optional<PoolElementStructurePiece> planned = plannedPiece(
                    level,
                    candidate,
                    kind
            );
            if (planned.isEmpty()) {
                checkNext(level, kind, candidates, index + 1, result);
                return;
            }
            PoolElementStructurePiece piece = planned.get();
            BlockPos marker = piece.getPosition();
            ChunkPos placementChunk = new ChunkPos(marker);
            level.getChunkSource().getChunkFuture(
                    placementChunk.x,
                    placementChunk.z,
                    ChunkStatus.FULL,
                    true
            ).thenAcceptAsync(ignored -> {
                Optional<BlockPos> placed =
                        LegacyStructureMarkerIndex.get(level)
                                .nearest(kind, marker)
                                .filter(marker::equals);
                if (placed.isEmpty() && hasPhysicalSignature(
                        level,
                        piece,
                        kind
                )) {
                    LegacyStructureMarkerIndex.get(level).record(kind, marker);
                    placed = Optional.of(marker);
                }
                if (placed.isPresent()) {
                    result.complete(placed);
                } else {
                    checkNext(level, kind, candidates, index + 1, result);
                }
            }, level.getServer()).exceptionally(exception -> {
                result.completeExceptionally(exception);
                return null;
            });
        }, level.getServer()).exceptionally(exception -> {
            result.completeExceptionally(exception);
            return null;
        });
    }

    /** Reconstructs the index for buildings generated before markers existed. */
    private static boolean hasPhysicalSignature(
            ServerLevel level,
            PoolElementStructurePiece piece,
            LegacyStructureKind kind
    ) {
        BlockPos origin = piece.getPosition();
        var rotation = piece.getRotation();
        if (kind == LegacyStructureKind.WIZARD_TOWER) {
            return level.getBlockState(StructureSitePolicy.rotated(
                            origin, 3, 5, 3, rotation))
                    .is(Blocks.GLOWSTONE)
                    && level.getBlockState(StructureSitePolicy.rotated(
                            origin, 2, 6, 2, rotation))
                    .is(Blocks.CHEST);
        }
        return level.getBlockState(StructureSitePolicy.rotated(
                        origin, 0, 2, 2, rotation))
                .is(Blocks.IRON_BARS)
                && level.getBlockState(StructureSitePolicy.rotated(
                        origin, 3, 2, 2, rotation))
                .is(Blocks.IRON_BARS)
                && level.getBlockState(StructureSitePolicy.rotated(
                        origin, 1, 1, 0, rotation))
                .is(Blocks.OAK_DOOR);
    }

    private static Optional<PoolElementStructurePiece> plannedPiece(
            ServerLevel level,
            ChunkPos candidate,
            LegacyStructureKind kind
    ) {
        Registry<Structure> structures = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE);
        Set<Structure> villages = new HashSet<>();
        structures.getTagOrEmpty(StructureTags.VILLAGE)
                .forEach(holder -> villages.add(holder.value()));
        return level.structureManager()
                .startsForStructure(candidate, villages::contains)
                .stream()
                .flatMap(start -> start.getPieces().stream())
                .filter(PoolElementStructurePiece.class::isInstance)
                .map(PoolElementStructurePiece.class::cast)
                .filter(piece -> piece.getElement()
                        instanceof LegacyVillagePoolElement legacy
                        && legacy.kind() == kind)
                .findFirst();
    }

    private static List<ChunkPos> candidates(
            ServerLevel level,
            BlockPos origin
    ) {
        Registry<Structure> structures = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE);
        ChunkGeneratorStructureState state =
                level.getChunkSource().getGeneratorState();
        Set<StructurePlacement> placements = new HashSet<>();
        for (Holder<Structure> village
                : structures.getTagOrEmpty(StructureTags.VILLAGE)) {
            placements.addAll(state.getPlacementsForStructure(village));
        }

        Set<Long> unique = new HashSet<>();
        List<ChunkPos> candidates = new ArrayList<>();
        for (StructurePlacement placement : placements) {
            if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
                continue;
            }
            int originRegionX = Math.floorDiv(
                    origin.getX() >> 4,
                    spread.spacing()
            );
            int originRegionZ = Math.floorDiv(
                    origin.getZ() >> 4,
                    spread.spacing()
            );
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
