package com.thaumcraftmodern.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.registry.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * A registry-visible wrapper around one procedural classic world site.
 */
public final class LegacyWorldStructure extends Structure {
    public static final Codec<LegacyWorldStructure> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    settingsCodec(instance),
                    Codec.STRING.fieldOf("kind")
                            .xmap(
                                    LegacyStructureKind::fromSerializedName,
                                    LegacyStructureKind::serializedName
                            )
                            .forGetter(LegacyWorldStructure::kind)
            ).apply(instance, LegacyWorldStructure::new));

    private final LegacyStructureKind kind;

    public LegacyWorldStructure(
            StructureSettings settings,
            LegacyStructureKind kind
    ) {
        super(settings);
        this.kind = kind;
    }

    public LegacyStructureKind kind() {
        return kind;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(
            GenerationContext context
    ) {
        if (!ThaumcraftModernServerConfig.generateStructures()) {
            return Optional.empty();
        }
        /*
         * The original per-structure rarity is represented by the structure
         * set placement. Keeping that roll here made /locate run terrain
         * generation for every candidate region before it could reject it.
         * Only the user-configurable multiplier remains dynamic.
         */
        int rarityScale =
                ThaumcraftModernServerConfig.structureRarityScale();
        if (context.random().nextInt(rarityScale) != 0) {
            return Optional.empty();
        }

        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + context.random().nextInt(16);
        int z = chunk.getMinBlockZ() + context.random().nextInt(16);
        /*
         * Do not query terrain here. Vanilla calls this method synchronously
         * while /locate scans candidate regions; even one getBaseHeight call
         * per candidate can stall the integrated server for several seconds.
         * The piece resolves its real ground height only when its start chunk
         * is actually generated.
         */
        BlockPos center = new BlockPos(
                x,
                context.chunkGenerator().getSeaLevel(),
                z
        );
        return Optional.of(new GenerationStub(
                center,
                builder -> builder.addPiece(
                        new LegacyWorldStructurePiece(kind, center)
                )
        ));
    }

    @Override
    public BoundingBox adjustBoundingBox(BoundingBox box) {
        return box.inflatedBy(1);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.LEGACY_WORLD_STRUCTURE.get();
    }

}
