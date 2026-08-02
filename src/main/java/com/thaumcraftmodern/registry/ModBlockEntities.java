package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import com.thaumcraftmodern.world.block.entity.AlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import com.thaumcraftmodern.world.block.entity.CrucibleBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.AdvancedEssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaCentrifugeBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaCrystallizerBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaReservoirBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import com.thaumcraftmodern.world.block.entity.ThaumatoriumBlockEntity;
import com.thaumcraftmodern.world.block.entity.VoidJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EldritchAltarPartBlockEntity;
import com.thaumcraftmodern.world.block.entity.EtherealBloomBlockEntity;
import com.thaumcraftmodern.world.block.entity.ManaPodBlockEntity;
import com.thaumcraftmodern.world.block.entity.MnemonicMatrixBlockEntity;
import com.thaumcraftmodern.world.block.entity.NitorBlockEntity;
import com.thaumcraftmodern.world.block.entity.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE =
            BLOCK_ENTITIES.register("research_table",
                    () -> BlockEntityType.Builder.of(
                            ResearchTableBlockEntity::new,
                            ModBlocks.RESEARCH_TABLE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<ArcaneWorkbenchBlockEntity>>
            ARCANE_WORKBENCH = BLOCK_ENTITIES.register(
                    "arcane_workbench",
                    () -> BlockEntityType.Builder.of(
                            ArcaneWorkbenchBlockEntity::new,
                            ModBlocks.ARCANE_WORKBENCH.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>>
            CRUCIBLE = BLOCK_ENTITIES.register(
                    "crucible",
                    () -> BlockEntityType.Builder.of(
                            CrucibleBlockEntity::new,
                            ModBlocks.CRUCIBLE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<NitorBlockEntity>>
            NITOR = BLOCK_ENTITIES.register(
                    "nitor",
                    () -> BlockEntityType.Builder.of(
                            NitorBlockEntity::new,
                            ModBlocks.NITOR.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<AlchemicalFurnaceBlockEntity>>
            ALCHEMICAL_FURNACE = BLOCK_ENTITIES.register(
                    "alchemical_furnace",
                    () -> BlockEntityType.Builder.of(
                            AlchemicalFurnaceBlockEntity::new,
                            ModBlocks.ALCHEMICAL_FURNACE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<ArcaneAlembicBlockEntity>>
            ARCANE_ALEMBIC = BLOCK_ENTITIES.register(
                    "arcane_alembic",
                    () -> BlockEntityType.Builder.of(
                            ArcaneAlembicBlockEntity::new,
                            ModBlocks.ARCANE_ALEMBIC.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaJarBlockEntity>>
            ESSENTIA_JAR = BLOCK_ENTITIES.register(
                    "essentia_jar",
                    () -> BlockEntityType.Builder.of(
                            EssentiaJarBlockEntity::new,
                            ModBlocks.WARDED_JAR.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaTubeBlockEntity>>
            ESSENTIA_TUBE = BLOCK_ENTITIES.register(
                    "essentia_tube",
                    () -> BlockEntityType.Builder.of(
                            EssentiaTubeBlockEntity::new,
                            ModBlocks.ESSENTIA_TUBE.get(),
                            ModBlocks.FILTERED_ESSENTIA_TUBE.get(),
                            ModBlocks.RESTRICTED_ESSENTIA_TUBE.get(),
                            ModBlocks.ONE_WAY_ESSENTIA_TUBE.get(),
                            ModBlocks.ESSENTIA_VALVE.get(),
                            ModBlocks.REVERSIBLE_ESSENTIA_TUBE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<AdvancedEssentiaBufferBlockEntity>>
            ADVANCED_ESSENTIA_BUFFER = BLOCK_ENTITIES.register(
                    "advanced_essentia_buffer",
                    () -> BlockEntityType.Builder.of(
                            AdvancedEssentiaBufferBlockEntity::new,
                            ModBlocks.ADVANCED_ESSENTIA_BUFFER.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaBufferBlockEntity>>
            ESSENTIA_BUFFER = BLOCK_ENTITIES.register(
                    "essentia_buffer",
                    () -> BlockEntityType.Builder.of(EssentiaBufferBlockEntity::new,
                            ModBlocks.ESSENTIA_BUFFER.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<VoidJarBlockEntity>>
            VOID_JAR = BLOCK_ENTITIES.register(
                    "void_jar",
                    () -> BlockEntityType.Builder.of(VoidJarBlockEntity::new,
                            ModBlocks.VOID_JAR.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaCentrifugeBlockEntity>>
            ESSENTIA_CENTRIFUGE = BLOCK_ENTITIES.register(
                    "essentia_centrifuge",
                    () -> BlockEntityType.Builder.of(EssentiaCentrifugeBlockEntity::new,
                            ModBlocks.ESSENTIA_CENTRIFUGE.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaCrystallizerBlockEntity>>
            ESSENTIA_CRYSTALLIZER = BLOCK_ENTITIES.register(
                    "essentia_crystallizer",
                    () -> BlockEntityType.Builder.of(EssentiaCrystallizerBlockEntity::new,
                            ModBlocks.ESSENTIA_CRYSTALLIZER.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaReservoirBlockEntity>>
            ESSENTIA_RESERVOIR = BLOCK_ENTITIES.register(
                    "essentia_reservoir",
                    () -> BlockEntityType.Builder.of(EssentiaReservoirBlockEntity::new,
                            ModBlocks.ESSENTIA_RESERVOIR.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<MnemonicMatrixBlockEntity>>
            MNEMONIC_MATRIX = BLOCK_ENTITIES.register(
                    "mnemonic_matrix",
                    () -> BlockEntityType.Builder.of(MnemonicMatrixBlockEntity::new,
                            ModBlocks.MNEMONIC_MATRIX.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<ThaumatoriumBlockEntity>>
            THAUMATORIUM = BLOCK_ENTITIES.register(
                    "thaumatorium",
                    () -> BlockEntityType.Builder.of(ThaumatoriumBlockEntity::new,
                            ModBlocks.THAUMATORIUM.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<AuraNodeBlockEntity>>
            AURA_NODE = BLOCK_ENTITIES.register(
                    "aura_node",
                    () -> BlockEntityType.Builder.of(
                            ModBlockEntities::createAuraNode,
                            ModBlocks.AURA_NODE.get(),
                            ModBlocks.SILVERWOOD_NODE.get(),
                            ModBlocks.OBSIDIAN_TOTEM_NODE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<JarredAuraNodeBlockEntity>>
            JARRED_AURA_NODE = BLOCK_ENTITIES.register(
                    "jarred_aura_node",
                    () -> BlockEntityType.Builder.of(
                            ModBlockEntities::createJarredAuraNode,
                            ModBlocks.JARRED_AURA_NODE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EldritchAltarPartBlockEntity>>
            ELDRITCH_ALTAR_PART = BLOCK_ENTITIES.register(
                    "eldritch_altar_part",
                    () -> BlockEntityType.Builder.of(
                            EldritchAltarPartBlockEntity::new,
                            ModBlocks.ELDRITCH_ALTAR_PART.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EtherealBloomBlockEntity>>
            ETHEREAL_BLOOM = BLOCK_ENTITIES.register(
                    "ethereal_bloom",
                    () -> BlockEntityType.Builder.of(
                            EtherealBloomBlockEntity::new,
                            ModBlocks.ETHEREAL_BLOOM.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<ManaPodBlockEntity>>
            MANA_POD = BLOCK_ENTITIES.register(
                    "mana_pod",
                    () -> BlockEntityType.Builder.of(
                            ManaPodBlockEntity::new,
                            ModBlocks.MANA_POD.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private static AuraNodeBlockEntity createAuraNode(
            BlockPos position,
            BlockState state
    ) {
        return new AuraNodeBlockEntity(AURA_NODE.get(), position, state);
    }

    private static JarredAuraNodeBlockEntity createJarredAuraNode(
            BlockPos position,
            BlockState state
    ) {
        return new JarredAuraNodeBlockEntity(
                JARRED_AURA_NODE.get(),
                position,
                state
        );
    }
}
