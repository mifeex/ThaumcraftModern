package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.worldgen.LegacyWorldStructure;
import com.thaumcraftmodern.worldgen.LegacyWorldStructurePiece;
import com.thaumcraftmodern.worldgen.LegacyVillagePoolElement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(
                    Registries.STRUCTURE_TYPE,
                    ThaumcraftModern.MOD_ID
            );
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(
                    Registries.STRUCTURE_PIECE,
                    ThaumcraftModern.MOD_ID
            );
    public static final DeferredRegister<StructurePoolElementType<?>>
            POOL_ELEMENT_TYPES = DeferredRegister.create(
                    Registries.STRUCTURE_POOL_ELEMENT,
                    ThaumcraftModern.MOD_ID
            );

    public static final RegistryObject<StructureType<LegacyWorldStructure>>
            LEGACY_WORLD_STRUCTURE = STRUCTURE_TYPES.register(
                    "legacy_world_structure",
                    () -> () -> LegacyWorldStructure.CODEC
            );
    public static final RegistryObject<StructurePieceType>
            LEGACY_WORLD_STRUCTURE_PIECE = PIECE_TYPES.register(
                    "legacy_world_structure",
                    () -> (context, tag) ->
                            new LegacyWorldStructurePiece(tag)
            );
    public static final RegistryObject<
            StructurePoolElementType<LegacyVillagePoolElement>>
            LEGACY_VILLAGE_POOL_ELEMENT = POOL_ELEMENT_TYPES.register(
                    "legacy_village_building",
                    () -> () -> LegacyVillagePoolElement.CODEC
            );

    private ModStructures() {
    }

    public static void register(IEventBus modBus) {
        STRUCTURE_TYPES.register(modBus);
        PIECE_TYPES.register(modBus);
        POOL_ELEMENT_TYPES.register(modBus);
    }
}
