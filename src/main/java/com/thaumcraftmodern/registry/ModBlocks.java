package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlock;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlock;
import com.thaumcraftmodern.world.block.ArcaneWorkbenchBlock;
import com.thaumcraftmodern.world.block.AlchemicalFurnaceBlock;
import com.thaumcraftmodern.world.block.ArcaneAlembicBlock;
import com.thaumcraftmodern.world.block.ClassicPartBlock;
import com.thaumcraftmodern.world.block.CinderpearlBlock;
import com.thaumcraftmodern.world.block.CrucibleBlock;
import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import com.thaumcraftmodern.world.block.EtherealBloomBlock;
import com.thaumcraftmodern.world.block.EssentiaJarBlock;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import com.thaumcraftmodern.essentia.tube.TubePolicyRegistry;
import com.thaumcraftmodern.world.block.FluxGasBlock;
import com.thaumcraftmodern.world.block.FluxGooBlock;
import com.thaumcraftmodern.world.block.InfusionPillarBlock;
import com.thaumcraftmodern.world.block.ManaPodBlock;
import com.thaumcraftmodern.world.block.NitorBlock;
import com.thaumcraftmodern.world.block.LootVesselBlock;
import com.thaumcraftmodern.world.block.ResearchTableBlock;
import com.thaumcraftmodern.world.block.RunicMatrixBlock;
import com.thaumcraftmodern.world.block.TaintFibresBlock;
import com.thaumcraftmodern.world.block.TaintedCaveVineBlock;
import com.thaumcraftmodern.world.block.TaintedGlowBerryVineBlock;
import com.thaumcraftmodern.world.block.TaintedPlantBlock;
import com.thaumcraftmodern.world.block.SpreadingTaintBlock;
import com.thaumcraftmodern.world.block.ThaumcraftTableBlock;
import com.thaumcraftmodern.world.block.ThaumatoriumBlock;
import com.thaumcraftmodern.world.block.VishroomBlock;
import com.thaumcraftmodern.world.tree.MagicalTreeGrower;
import com.thaumcraftmodern.worldgen.ModWorldgenKeys;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<Block> RESEARCH_TABLE = BLOCKS.register(
            "research_table",
            () -> new ResearchTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );
    public static final RegistryObject<Block> THAUMCRAFT_TABLE = BLOCKS.register(
            "thaumcraft_table",
            () -> new ThaumcraftTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );
    public static final RegistryObject<Block> ARCANE_WORKBENCH = BLOCKS.register(
            "arcane_workbench",
            () -> new ArcaneWorkbenchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );
    public static final RegistryObject<Block> ARCANE_STONE = BLOCKS.register(
            "arcane_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE))
    );
    public static final RegistryObject<Block> ARCANE_STONE_BRICK =
            BLOCKS.register(
                    "arcane_stone_brick",
                    () -> new Block(arcaneStoneProperties())
            );
    public static final RegistryObject<Block> LOOT_URN = BLOCKS.register(
            "loot_urn",
            () -> new LootVesselBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.TERRACOTTA_BROWN)
                            .strength(0.15F)
                            .sound(SoundType.DECORATED_POT)
                            .noOcclusion(),
                    true
            )
    );
    public static final RegistryObject<Block> LOOT_CRATE = BLOCKS.register(
            "loot_crate",
            () -> new LootVesselBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(0.15F)
                            .sound(SoundType.WOOD)
                            .noOcclusion(),
                    false
            )
    );
    public static final RegistryObject<Block> CRUCIBLE = BLOCKS.register(
            "crucible",
            () -> new CrucibleBlock(metalDeviceProperties().noOcclusion())
    );
    public static final RegistryObject<Block> NITOR = BLOCKS.register(
            "nitor",
            () -> new NitorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.FIRE)
                            .strength(0.0F)
                            .noCollission()
                            .noOcclusion()
                            .lightLevel(state -> 15)
                            .sound(SoundType.WOOL)
            )
    );
    public static final RegistryObject<Block> ALCHEMICAL_FURNACE =
            BLOCKS.register(
                    "alchemical_furnace",
                    () -> new AlchemicalFurnaceBlock(
                            arcaneStoneProperties().noOcclusion()
                    )
            );
    public static final RegistryObject<Block> RUNIC_MATRIX = BLOCKS.register(
            "runic_matrix",
            () -> new RunicMatrixBlock(
                    arcaneStoneProperties()
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(
                                    RunicMatrixBlock.ACTIVE
                            ) ? 10 : 6)
            )
    );
    public static final RegistryObject<Block> ARCANE_PEDESTAL =
            BLOCKS.register(
                    "arcane_pedestal",
                    () -> new Block(arcaneStoneProperties().noOcclusion())
            );
    public static final RegistryObject<Block> ARCANE_ALEMBIC =
            BLOCKS.register(
                    "arcane_alembic",
                    () -> new ArcaneAlembicBlock(
                            metalDeviceProperties().noOcclusion()
                    )
            );
    public static final RegistryObject<Block> WARDED_JAR = BLOCKS.register(
            "warded_jar",
            () -> new EssentiaJarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(0.3F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .noLootTable()
            )
    );
    public static final RegistryObject<Block> ESSENTIA_TUBE = essentiaTube(
            "essentia_tube", TubePolicyRegistry.PLAIN);
    public static final RegistryObject<Block> FILTERED_ESSENTIA_TUBE =
            essentiaTube("filtered_essentia_tube", TubePolicyRegistry.FILTERED);
    public static final RegistryObject<Block> RESTRICTED_ESSENTIA_TUBE =
            essentiaTube("restricted_essentia_tube", TubePolicyRegistry.RESTRICTED);
    public static final RegistryObject<Block> ONE_WAY_ESSENTIA_TUBE =
            essentiaTube("one_way_essentia_tube", TubePolicyRegistry.ONE_WAY);
    public static final RegistryObject<Block> ESSENTIA_VALVE = essentiaTube(
            "essentia_valve", TubePolicyRegistry.VALVE);
    public static final RegistryObject<Block> ALCHEMICAL_CONSTRUCT =
            BLOCKS.register(
                    "alchemical_construct",
                    () -> new Block(metalDeviceProperties())
            );
    public static final RegistryObject<Block> ADVANCED_ALCHEMICAL_CONSTRUCT =
            BLOCKS.register(
                    "advanced_alchemical_construct",
                    () -> new Block(metalDeviceProperties())
            );
    public static final RegistryObject<Block> INFUSION_PILLAR =
            BLOCKS.register(
                    "infusion_pillar",
                    () -> new InfusionPillarBlock(
                            arcaneStoneProperties()
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> INFERNAL_FURNACE =
            BLOCKS.register(
                    "infernal_furnace",
                    () -> new ClassicPartBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_BLACK)
                                    .strength(10.0F, 500.0F)
                                    .sound(SoundType.STONE)
                                    .lightLevel(state ->
                                            state.getValue(
                                                    ClassicPartBlock.PART
                                            ) == 0 ? 13 : 3)
                                    .noLootTable(),
                            10
                    )
            );
    public static final RegistryObject<Block> THAUMATORIUM =
            BLOCKS.register(
                    "thaumatorium",
                    () -> new ThaumatoriumBlock(
                            metalDeviceProperties()
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> ADVANCED_ALCHEMICAL_FURNACE =
            BLOCKS.register(
                    "advanced_alchemical_furnace",
                    () -> new ClassicPartBlock(
                            metalDeviceProperties()
                                    .noOcclusion()
                                    .noLootTable(),
                            4
                    )
            );
    public static final RegistryObject<Block> AURA_NODE = BLOCKS.register(
            "aura_node",
            () -> new AuraNodeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(2.0F, 200.0F)
                            .sound(SoundType.EMPTY)
                            .noCollission()
                            .noOcclusion()
                            .noLootTable()
                            .lightLevel(state -> 8),
                    () -> ModBlockEntities.AURA_NODE.get()
            )
    );
    public static final RegistryObject<Block> JARRED_AURA_NODE = BLOCKS.register(
            "jarred_aura_node",
            () -> new JarredAuraNodeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(0.3F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .noLootTable()
                            .lightLevel(state -> 7),
                    () -> ModBlockEntities.JARRED_AURA_NODE.get()
            )
    );

    public static final RegistryObject<Block> CINNABAR_ORE =
            ore("cinnabar_ore", UniformInt.of(1, 3));
    public static final RegistryObject<Block> AIR_INFUSED_STONE =
            ore("air_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> FIRE_INFUSED_STONE =
            ore("fire_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> WATER_INFUSED_STONE =
            ore("water_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> EARTH_INFUSED_STONE =
            ore("earth_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> ORDER_INFUSED_STONE =
            ore("order_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> ENTROPY_INFUSED_STONE =
            ore("entropy_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> AMBER_ORE =
            ore("amber_ore", UniformInt.of(1, 3));

    public static final RegistryObject<Block> GREATWOOD_LOG = BLOCKS.register(
            "greatwood_log",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                            .strength(2.0F)
            )
    );
    public static final RegistryObject<Block> SILVERWOOD_LOG = BLOCKS.register(
            "silverwood_log",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                            .strength(2.0F)
            )
    );
    public static final RegistryObject<Block> SILVERWOOD_NODE = BLOCKS.register(
            "silverwood_node",
            () -> new AuraNodeBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                            .strength(2.0F)
                            .noLootTable()
                            .lightLevel(state -> 7),
                    () -> ModBlockEntities.AURA_NODE.get(),
                    true
            )
    );
    public static final RegistryObject<Block> GREATWOOD_LEAVES = BLOCKS.register(
            "greatwood_leaves",
            () -> new LeavesBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)
                            .strength(0.2F)
                            .randomTicks()
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> SILVERWOOD_LEAVES = BLOCKS.register(
            "silverwood_leaves",
            () -> new LeavesBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)
                            .strength(0.2F)
                            .randomTicks()
                            .noOcclusion()
                            .lightLevel(state -> 7)
            )
    );
    public static final RegistryObject<Block> GREATWOOD_PLANKS = BLOCKS.register(
            "greatwood_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS))
    );
    public static final RegistryObject<Block> SILVERWOOD_PLANKS = BLOCKS.register(
            "silverwood_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS))
    );
    public static final RegistryObject<Block> GREATWOOD_SAPLING = BLOCKS.register(
            "greatwood_sapling",
            () -> new SaplingBlock(
                    new MagicalTreeGrower(ModWorldgenKeys.GREATWOOD_TREE),
                    BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)
            )
    );
    public static final RegistryObject<Block> SILVERWOOD_SAPLING = BLOCKS.register(
            "silverwood_sapling",
            () -> new SaplingBlock(
                    new MagicalTreeGrower(ModWorldgenKeys.SILVERWOOD_TREE),
                    BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)
            )
    );

    public static final RegistryObject<Block> SHIMMERLEAF =
            flower("shimmerleaf", MobEffects.REGENERATION, 5, 1);
    public static final RegistryObject<Block> CINDERPEARL =
            BLOCKS.register(
                    "cinderpearl",
                    () -> new CinderpearlBlock(
                            MobEffects.FIRE_RESISTANCE,
                            5,
                            flowerProperties(3)
                    )
            );
    public static final RegistryObject<Block> ETHEREAL_BLOOM =
            BLOCKS.register(
                    "ethereal_bloom",
                    () -> new EtherealBloomBlock(
                            MobEffects.REGENERATION,
                            8,
                            flowerProperties(7)
                    )
            );
    public static final RegistryObject<Block> VISHROOM =
            BLOCKS.register(
                    "vishroom",
                    () -> new VishroomBlock(
                            MobEffects.NIGHT_VISION,
                            5,
                            flowerProperties(2)
                    )
            );
    public static final RegistryObject<Block> CRUSTED_TAINT = BLOCKS.register(
            "crusted_taint",
            () -> new SpreadingTaintBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(1.5F, 3.0F)
                            .sound(SoundType.GRAVEL)
                            .randomTicks()
            )
    );
    public static final RegistryObject<Block> TAINTED_SOIL = BLOCKS.register(
            "tainted_soil",
            () -> new SpreadingTaintBlock(
                    BlockBehaviour.Properties.copy(Blocks.DIRT)
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(1.5F, 3.0F)
                            .sound(SoundType.GRAVEL)
                            .randomTicks()
                    )
    );
    public static final RegistryObject<Block> TAINTED_LEAVES = BLOCKS.register(
            "tainted_leaves",
            () -> new LeavesBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)
                            .mapColor(MapColor.COLOR_PURPLE)
                            .randomTicks()
            )
    );
    public static final RegistryObject<Block> TAINT_FIBRES = BLOCKS.register(
            "taint_fibres",
            () -> new TaintFibresBlock(
                    BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN)
                            .strength(1.0F, 5.0F)
                            .noCollission()
                            .randomTicks()
            )
    );
    /** Visual-only block for reviewing the second tainted-moss stage in game. */
    public static final RegistryObject<Block> TAINTED_CAVE_MOSS_TEST = BLOCKS.register(
            "tainted_cave_moss_test",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK))
    );
    /** Visual-only block for reviewing the tainted cave-vine texture in game. */
    public static final RegistryObject<Block> TAINTED_CAVE_VINE_TEST = BLOCKS.register(
            "tainted_cave_vine_test",
            () -> new TaintedCaveVineBlock(
                    BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN)
                            .noCollission()
            )
    );
    /** Visual-only cave-vine head with tainted glow berries. */
    public static final RegistryObject<Block> TAINTED_GLOW_BERRY_VINE_TEST = BLOCKS.register(
            "tainted_glow_berry_vine_test",
            () -> new TaintedGlowBerryVineBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .replaceable()
                            .noCollission()
                            .noOcclusion()
                            .instabreak()
                            .sound(SoundType.VINE)
                            .lightLevel(state -> state.getValue(CaveVines.BERRIES) ? 12 : 0)
            )
    );
    public static final RegistryObject<Block> SHORT_TAINTED_GRASS =
            taintedPlant("short_tainted_grass", 0);
    public static final RegistryObject<Block> TALL_TAINTED_GRASS =
            taintedPlant("tall_tainted_grass", 8);
    public static final RegistryObject<Block> SPORE_STALK =
            taintedPlant("spore_stalk", 0);
    public static final RegistryObject<Block> MATURE_SPORE_STALK =
            taintedPlant("mature_spore_stalk", 10);
    public static final RegistryObject<Block> FLUX_GOO = BLOCKS.register(
            "flux_goo",
            () -> new FluxGooBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(0.0F)
                            .noCollission()
                            .noOcclusion()
                            .randomTicks()
                    )
    );
    public static final RegistryObject<Block> FLUX_GAS = BLOCKS.register(
            "flux_gas",
            () -> new FluxGasBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(0.0F)
                            .noCollission()
                            .noOcclusion()
                            .randomTicks()
                    )
    );
    public static final RegistryObject<Block> MANA_POD = BLOCKS.register(
            "mana_pod",
            () -> new ManaPodBlock(
                    BlockBehaviour.Properties.copy(Blocks.WHEAT)
                            .noCollission()
                            .randomTicks()
                            .lightLevel(state -> state.getValue(ManaPodBlock.AGE))
            )
    );

    public static final RegistryObject<Block> OBSIDIAN_TOTEM = BLOCKS.register(
            "obsidian_totem",
            () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                            .strength(30.0F, 1200.0F)
            )
    );
    public static final RegistryObject<Block> OBSIDIAN_TOTEM_NODE =
            BLOCKS.register(
                    "obsidian_totem_node",
                    () -> new AuraNodeBlock(
                            BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                                    .strength(30.0F, 1200.0F)
                                    .noLootTable()
                                    .lightLevel(state -> 9),
                            () -> ModBlockEntities.AURA_NODE.get(),
                            true
                    )
            );
    public static final RegistryObject<Block> OBSIDIAN_TILE = BLOCKS.register(
            "obsidian_tile",
            () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                            .strength(30.0F, 1200.0F)
                            .noLootTable()
            )
    );
    public static final RegistryObject<Block> ELDRITCH_ALTAR_PART =
            BLOCKS.register(
                    "eldritch_altar_part",
                    () -> new EldritchAltarPartBlock(
                            BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                                    .strength(30.0F, 1200.0F)
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> ANCIENT_STONE = BLOCKS.register(
            "ancient_stone",
            () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS)
                            .strength(4.0F, 40.0F)
            )
    );

    private ModBlocks() {
    }

    private static RegistryObject<Block> taintedPlant(
            String name,
            int light
    ) {
        return BLOCKS.register(
                name,
                () -> new TaintedPlantBlock(
                        BlockBehaviour.Properties.copy(Blocks.DEAD_BUSH)
                                .mapColor(MapColor.COLOR_PURPLE)
                                .noCollission()
                                .randomTicks()
                                .lightLevel(state -> light)
                )
        );
    }

    private static BlockBehaviour.Properties arcaneStoneProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties metalDeviceProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 17.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    private static RegistryObject<Block> ore(
            String name,
            UniformInt experience
    ) {
        return BLOCKS.register(
                name,
                () -> new DropExperienceBlock(
                        BlockBehaviour.Properties.copy(Blocks.STONE)
                                .strength(3.0F, 5.0F)
                                .requiresCorrectToolForDrops(),
                        experience
                )
        );
    }

    private static RegistryObject<Block> essentiaTube(
            String name,
            net.minecraft.resources.ResourceLocation policy
    ) {
        return BLOCKS.register(
                name,
                () -> new EssentiaTubeBlock(
                        metalDeviceProperties().noOcclusion(),
                        policy
                )
        );
    }

    private static RegistryObject<Block> flower(
            String name,
            net.minecraft.world.effect.MobEffect effect,
            int duration,
            int light
    ) {
        return BLOCKS.register(
                name,
                () -> new FlowerBlock(
                        effect,
                        duration,
                        flowerProperties(light)
                )
        );
    }

    private static BlockBehaviour.Properties flowerProperties(int light) {
        return BlockBehaviour.Properties.copy(Blocks.DANDELION)
                .noCollission()
                .instabreak()
                .noOcclusion()
                .lightLevel(state -> light);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
