package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

class LegacyStructureMarkerIndexTest {
    @Test
    void returnsNearestRealMarkerForRequestedKind() {
        LegacyStructureMarkerIndex index = new LegacyStructureMarkerIndex();
        BlockPos far = new BlockPos(160, 70, 160);
        BlockPos near = new BlockPos(32, 80, 48);
        index.replaceChunk(
                new ChunkPos(far),
                Map.of(LegacyStructureKind.AURA_TOTEM, List.of(far))
        );
        index.replaceChunk(
                new ChunkPos(near),
                Map.of(LegacyStructureKind.AURA_TOTEM, List.of(near))
        );

        assertEquals(
                near,
                index.nearest(LegacyStructureKind.AURA_TOTEM, BlockPos.ZERO)
                        .orElseThrow()
        );
        assertTrue(index.nearest(
                LegacyStructureKind.ELDRITCH_RING,
                BlockPos.ZERO
        ).isEmpty());
    }

    @Test
    void rescanningChunkRemovesDestroyedMarkerAndPersistsOthers() {
        LegacyStructureMarkerIndex index = new LegacyStructureMarkerIndex();
        BlockPos mound = new BlockPos(-17, 42, 31);
        ChunkPos chunk = new ChunkPos(mound);
        index.replaceChunk(
                chunk,
                Map.of(LegacyStructureKind.ANCIENT_MOUND, List.of(mound))
        );
        CompoundTag saved = index.save(new CompoundTag());
        LegacyStructureMarkerIndex restored =
                LegacyStructureMarkerIndex.load(saved);
        assertEquals(
                mound,
                restored.nearest(
                        LegacyStructureKind.ANCIENT_MOUND,
                        BlockPos.ZERO
                ).orElseThrow()
        );

        restored.replaceChunk(chunk, Map.of());
        assertTrue(restored.nearest(
                LegacyStructureKind.ANCIENT_MOUND,
                BlockPos.ZERO
        ).isEmpty());
    }
}
