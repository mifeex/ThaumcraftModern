package com.thaumcraftmodern.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thaumcraftmodern.registry.ModStructures;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * A classic TC4 village building exposed as a native 1.20.1 jigsaw element.
 */
public final class LegacyVillagePoolElement extends StructurePoolElement {
    public static final Codec<LegacyVillagePoolElement> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("kind")
                            .xmap(
                                    LegacyStructureKind::fromSerializedName,
                                    LegacyStructureKind::serializedName
                            )
                            .forGetter(LegacyVillagePoolElement::kind),
                    projectionCodec()
            ).apply(instance, LegacyVillagePoolElement::new));

    private final LegacyStructureKind kind;

    public LegacyVillagePoolElement(
            LegacyStructureKind kind,
            StructureTemplatePool.Projection projection
    ) {
        super(projection);
        if (!kind.isVillageBuilding()) {
            throw new IllegalArgumentException(
                    kind + " is not a village building"
            );
        }
        this.kind = kind;
    }

    public LegacyStructureKind kind() {
        return kind;
    }

    @Override
    public Vec3i getSize(
            StructureTemplateManager templateManager,
            Rotation rotation
    ) {
        int width = kind == LegacyStructureKind.WIZARD_TOWER ? 7 : 4;
        int height = kind == LegacyStructureKind.WIZARD_TOWER ? 12 : 6;
        int depth = kind == LegacyStructureKind.WIZARD_TOWER ? 6 : 5;
        return switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 ->
                    new Vec3i(depth, height, width);
            default -> new Vec3i(width, height, depth);
        };
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo>
            getShuffledJigsawBlocks(
                    StructureTemplateManager templateManager,
                    BlockPos position,
                    Rotation rotation,
                    RandomSource random
            ) {
        int entranceX = kind == LegacyStructureKind.WIZARD_TOWER ? 3 : 1;
        BlockPos connector = StructureSitePolicy.rotated(
                position,
                entranceX,
                1,
                0,
                rotation
        );
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", "minecraft:building_entrance");
        nbt.putString("target", "minecraft:empty");
        nbt.putString("pool", "minecraft:empty");
        nbt.putString("final_state", "minecraft:air");
        nbt.putString("joint", "rollable");
        return List.of(new StructureTemplate.StructureBlockInfo(
                connector,
                Blocks.JIGSAW.defaultBlockState().setValue(
                        JigsawBlock.ORIENTATION,
                        FrontAndTop.fromFrontAndTop(
                                rotation.rotate(
                                        net.minecraft.core.Direction.NORTH
                                ),
                                net.minecraft.core.Direction.UP
                        )
                ),
                nbt
        ));
    }

    @Override
    public BoundingBox getBoundingBox(
            StructureTemplateManager templateManager,
            BlockPos position,
            Rotation rotation
    ) {
        int maxX = kind == LegacyStructureKind.WIZARD_TOWER ? 6 : 3;
        int maxY = kind == LegacyStructureKind.WIZARD_TOWER ? 11 : 5;
        int maxZ = kind == LegacyStructureKind.WIZARD_TOWER ? 5 : 4;
        BlockPos opposite = StructureSitePolicy.rotated(
                position,
                maxX,
                maxY,
                maxZ,
                rotation
        );
        return BoundingBox.fromCorners(position, opposite);
    }

    @Override
    public boolean place(
            StructureTemplateManager templateManager,
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            BlockPos position,
            BlockPos pivot,
            Rotation rotation,
            BoundingBox chunkBox,
            RandomSource random,
            boolean keepJigsaws
    ) {
        if (!chunkBox.isInside(position)) {
            return true;
        }
        return LegacyStructuresFeature.placeVillageBuilding(
                kind,
                level,
                position,
                rotation,
                random
        );
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return ModStructures.LEGACY_VILLAGE_POOL_ELEMENT.get();
    }
}
