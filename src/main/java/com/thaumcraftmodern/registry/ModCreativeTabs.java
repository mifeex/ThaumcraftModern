package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBreakDrops;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.item.EssentiaPhialItem;
import com.thaumcraftmodern.item.JarLabelItem;
import com.thaumcraftmodern.nodejar.NodeJarFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register(
            "thaumcraft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumcraftmodern"))
                    .icon(() -> ModItems.THAUMONOMICON.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.THAUMONOMICON.get());
                        output.accept(ModItems.THAUMOMETER.get());
                        output.accept(ModItems.ETHEREAL_BLOOM.get());
                        output.accept(ModItems.SANITY_CHECKER.get());
                        output.accept(ModItems.SANITY_SOAP.get());
                        output.accept(ModItems.IRON_WAND_CAP.get());
                        output.accept(ModItems.GOLD_WAND_CAP.get());
                        output.accept(ModItems.COPPER_WAND_CAP.get());
                        output.accept(ModItems.SILVER_WAND_CAP.get());
                        output.accept(ModItems.THAUMIUM_WAND_CAP.get());
                        output.accept(ModItems.VOID_WAND_CAP.get());
                        output.accept(ModItems.WOODEN_WAND_ROD.get());
                        output.accept(ModItems.GREATWOOD_WAND_ROD.get());
                        output.accept(ModItems.OBSIDIAN_WAND_ROD.get());
                        output.accept(ModItems.BLAZE_WAND_ROD.get());
                        output.accept(ModItems.ICE_WAND_ROD.get());
                        output.accept(ModItems.QUARTZ_WAND_ROD.get());
                        output.accept(ModItems.REED_WAND_ROD.get());
                        output.accept(ModItems.BONE_WAND_ROD.get());
                        output.accept(ModItems.SILVERWOOD_WAND_ROD.get());
                        output.accept(ModItems.GREATWOOD_STAFF_ROD.get());
                        output.accept(ModItems.OBSIDIAN_STAFF_ROD.get());
                        output.accept(ModItems.BLAZE_STAFF_ROD.get());
                        output.accept(ModItems.ICE_STAFF_ROD.get());
                        output.accept(ModItems.QUARTZ_STAFF_ROD.get());
                        output.accept(ModItems.REED_STAFF_ROD.get());
                        output.accept(ModItems.BONE_STAFF_ROD.get());
                        output.accept(ModItems.SILVERWOOD_STAFF_ROD.get());
                        output.accept(ModItems.PRIMAL_STAFF_ROD.get());
                        output.accept(ModItems.BASIC_WAND.get().getDefaultInstance());
                        output.accept(ModItems.SILVERWOOD_WAND.get().getDefaultInstance());
                        output.accept(ModItems.CASTING_WAND.get().getDefaultInstance());
                        output.accept(
                                ModItems.CRAFTING_SCEPTRE.get()
                                        .getDefaultInstance()
                        );
                        output.accept(
                                ModItems.GREATWOOD_STAFF.get()
                                        .getDefaultInstance()
                        );
                        output.accept(
                                ModItems.SILVERWOOD_STAFF.get()
                                        .getDefaultInstance()
                        );
                        output.accept(ModItems.PRIMAL_STAFF.get().getDefaultInstance());
                        output.accept(ModItems.CODEX_WAND.get().getDefaultInstance());
                        output.accept(ModItems.GOGGLES_OF_REVEALING.get());
                        output.accept(NodeJarFactory.deterministicCreativeStack(
                                ModItems.JARRED_AURA_NODE.get()
                        ));
                        output.accept(ModItems.THAUMCRAFT_TABLE.get());
                        output.accept(ModItems.ARCANE_WORKBENCH.get());
                        output.accept(ModItems.ARCANE_STONE.get());
                        output.accept(ModItems.ARCANE_STONE_BRICK.get());
                        output.accept(ModItems.CRUCIBLE.get());
                        output.accept(ModItems.ALCHEMICAL_FURNACE.get());
                        output.accept(ModItems.RUNIC_MATRIX.get());
                        output.accept(ModItems.ARCANE_PEDESTAL.get());
                        output.accept(ModItems.ARCANE_ALEMBIC.get());
                        output.accept(ModItems.ESSENTIA_PHIAL.get());
                        for (PrimalAspect aspect : PrimalAspect.ordered()) {
                            output.accept(EssentiaPhialItem.filled(
                                    ModItems.ESSENTIA_PHIAL.get(), aspect.id()));
                        }
                        output.accept(ModItems.JAR_LABEL.get());
                        output.accept(JarLabelItem.tuned(
                                ModItems.JAR_LABEL.get(), PrimalAspect.AER.id()));
                        output.accept(ModItems.WARDED_JAR.get());
                        output.accept(ModItems.ESSENTIA_TUBE.get());
                        output.accept(ModItems.FILTERED_ESSENTIA_TUBE.get());
                        output.accept(ModItems.RESTRICTED_ESSENTIA_TUBE.get());
                        output.accept(ModItems.ONE_WAY_ESSENTIA_TUBE.get());
                        output.accept(ModItems.ESSENTIA_VALVE.get());
                        output.accept(ModItems.ALCHEMICAL_CONSTRUCT.get());
                        output.accept(
                                ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get()
                        );
                        output.accept(ModItems.SCRIBING_TOOLS.get());
                        output.accept(ModItems.RESEARCH_NOTES.get());
                        output.accept(ModItems.DISCOVERY.get());
                        output.accept(ModItems.AIR_SHARD.get());
                        output.accept(ModItems.FIRE_SHARD.get());
                        output.accept(ModItems.WATER_SHARD.get());
                        output.accept(ModItems.EARTH_SHARD.get());
                        output.accept(ModItems.ORDER_SHARD.get());
                        output.accept(ModItems.ENTROPY_SHARD.get());
                        output.accept(ModItems.BALANCED_SHARD.get());
                        output.accept(ModItems.SALIS_MUNDUS.get());
                        output.accept(ModItems.NITOR.get());
                        output.accept(ModItems.ALUMENTUM.get());
                        for (PrimalAspect aspect : PrimalAspect.ordered()) {
                            output.accept(EtherealEssenceItem.create(
                                    ModItems.ETHEREAL_ESSENCE.get(),
                                    aspect,
                                    AuraNodeBreakDrops.ESSENCE_ASPECT_AMOUNT
                            ));
                        }
                        output.accept(ModItems.KNOWLEDGE_FRAGMENT.get());
                        output.accept(ModItems.QUICKSILVER.get());
                        output.accept(ModItems.AMBER.get());
                        output.accept(ModItems.MANA_BEAN.get());
                        output.accept(ModItems.ZOMBIE_BRAIN.get());
                        output.accept(ModItems.TAINTED_GOO.get());
                        output.accept(ModItems.TAINT_TENDRIL.get());
                        output.accept(ModItems.GOLD_COIN.get());
                        output.accept(ModItems.VOID_SEED.get());
                        output.accept(ModItems.ELDRITCH_EYE.get());
                        output.accept(ModItems.CRIMSON_RITES.get());
                        output.accept(ModItems.RUNED_TABLET.get());
                        output.accept(ModItems.PRIMORDIAL_PEARL.get());
                        output.accept(ModItems.COMMON_LOOT_BAG.get());
                        output.accept(ModItems.UNCOMMON_LOOT_BAG.get());
                        output.accept(ModItems.RARE_LOOT_BAG.get());
                        output.accept(ModItems.CINNABAR_ORE.get());
                        output.accept(ModItems.AMBER_ORE.get());
                        output.accept(ModItems.AIR_INFUSED_STONE.get());
                        output.accept(ModItems.FIRE_INFUSED_STONE.get());
                        output.accept(ModItems.WATER_INFUSED_STONE.get());
                        output.accept(ModItems.EARTH_INFUSED_STONE.get());
                        output.accept(ModItems.ORDER_INFUSED_STONE.get());
                        output.accept(ModItems.ENTROPY_INFUSED_STONE.get());
                        output.accept(ModItems.GREATWOOD_LOG.get());
                        output.accept(ModItems.GREATWOOD_LEAVES.get());
                        output.accept(ModItems.GREATWOOD_SAPLING.get());
                        output.accept(ModItems.GREATWOOD_PLANKS.get());
                        output.accept(ModItems.SILVERWOOD_LOG.get());
                        output.accept(ModItems.SILVERWOOD_LEAVES.get());
                        output.accept(ModItems.SILVERWOOD_SAPLING.get());
                        output.accept(ModItems.SILVERWOOD_PLANKS.get());
                        output.accept(ModItems.SHIMMERLEAF.get());
                        output.accept(ModItems.CINDERPEARL.get());
                        output.accept(ModItems.VISHROOM.get());
                        output.accept(ModItems.MANA_POD.get());
                        output.accept(ModItems.CRUSTED_TAINT.get());
                        output.accept(ModItems.TAINTED_SOIL.get());
                        output.accept(ModItems.TAINT_FIBRES.get());
                        output.accept(ModItems.TAINTED_CAVE_MOSS_TEST.get());
                        output.accept(ModItems.TAINTED_CAVE_VINE_TEST.get());
                        output.accept(ModItems.TAINTED_GLOW_BERRY_VINE_TEST.get());
                        output.accept(ModItems.SHORT_TAINTED_GRASS.get());
                        output.accept(ModItems.TALL_TAINTED_GRASS.get());
                        output.accept(ModItems.SPORE_STALK.get());
                        output.accept(ModItems.MATURE_SPORE_STALK.get());
                        output.accept(ModItems.FLUX_GOO.get());
                        output.accept(ModItems.FLUX_GAS.get());
                        output.accept(ModItems.OBSIDIAN_TOTEM.get());
                        output.accept(ModItems.OBSIDIAN_TILE.get());
                        output.accept(ModItems.ANCIENT_STONE.get());
                        for (var spawnEgg : ModItems.SPAWN_EGGS.values()) {
                            output.accept(spawnEgg.get());
                        }
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
