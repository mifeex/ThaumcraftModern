package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Serializable placement unit for a procedural classic world site.
 */
public final class LegacyWorldStructurePiece extends StructurePiece {
    private static final String KIND_TAG = "LegacyKind";
    private static final String CENTER_X_TAG = "CenterX";
    private static final String CENTER_Y_TAG = "CenterY";
    private static final String CENTER_Z_TAG = "CenterZ";

    private final LegacyStructureKind kind;
    private final BlockPos center;

    public LegacyWorldStructurePiece(
            LegacyStructureKind kind,
            BlockPos center
    ) {
        super(
                ModStructures.LEGACY_WORLD_STRUCTURE_PIECE.get(),
                0,
                bounds(kind, center)
        );
        this.kind = kind;
        this.center = center.immutable();
    }

    public LegacyWorldStructurePiece(CompoundTag tag) {
        super(ModStructures.LEGACY_WORLD_STRUCTURE_PIECE.get(), tag);
        this.kind = LegacyStructureKind.fromSerializedName(
                tag.getString(KIND_TAG)
        );
        this.center = new BlockPos(
                tag.getInt(CENTER_X_TAG),
                tag.getInt(CENTER_Y_TAG),
                tag.getInt(CENTER_Z_TAG)
        );
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext context,
            CompoundTag tag
    ) {
        tag.putString(KIND_TAG, kind.serializedName());
        tag.putInt(CENTER_X_TAG, center.getX());
        tag.putInt(CENTER_Y_TAG, center.getY());
        tag.putInt(CENTER_Z_TAG, center.getZ());
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        /*
         * The legacy generators place their complete compact site in one
         * operation. Only the start chunk owns that operation, preventing
         * repeated chests, nodes, and inhabitants when adjacent chunks place
         * the same structure piece.
         */
        if (chunkPos.x != Math.floorDiv(center.getX(), 16)
                || chunkPos.z != Math.floorDiv(center.getZ(), 16)) {
            return;
        }
        int surfaceY = level.getHeight(
                Heightmap.Types.OCEAN_FLOOR_WG,
                center.getX(),
                center.getZ()
        );
        LegacyStructuresFeature.placeRegistered(
                kind,
                level,
                new BlockPos(center.getX(), surfaceY, center.getZ()),
                random
        );
    }

    @Override
    public BlockPos getLocatorPosition() {
        return center;
    }

    private static BoundingBox bounds(
            LegacyStructureKind kind,
            BlockPos center
    ) {
        int radius = kind == LegacyStructureKind.ANCIENT_MOUND
                ? kind.horizontalRadius() + 15
                : kind.horizontalRadius();
        int minimumY = kind == LegacyStructureKind.ANCIENT_MOUND
                ? center.getY() - 10
                : center.getY() - 4;
        return new BoundingBox(
                center.getX() - radius,
                minimumY,
                center.getZ() - radius,
                center.getX() + radius,
                center.getY() + kind.height(),
                center.getZ() + radius
        );
    }
}
