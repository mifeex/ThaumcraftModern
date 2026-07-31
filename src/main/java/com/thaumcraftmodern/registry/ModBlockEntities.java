package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import com.thaumcraftmodern.world.block.entity.AlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import com.thaumcraftmodern.world.block.entity.CrucibleBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import com.thaumcraftmodern.world.block.entity.EldritchAltarPartBlockEntity;
import com.thaumcraftmodern.world.block.entity.EtherealBloomBlockEntity;
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
                            ModBlocks.ESSENTIA_VALVE.get()
                    ).build(null)
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
