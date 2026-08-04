package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.visnet.EnergizedAuraNodeBlockEntity;
import com.thaumcraftmodern.visnet.NodeTransducerBlockEntity;
import com.thaumcraftmodern.visnet.VisChargeRelayBlockEntity;
import com.thaumcraftmodern.visnet.VisNetworkNodeBlockEntity;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VisTamingGameTests {
    private VisTamingGameTests() {
    }

    @GameTest(template = "empty", batch = "visTaming")
    public static void transducerConvertsAndRevertsNodeAtExactThresholds(
            GameTestHelper helper
    ) {
        BlockPos stabilizer = new BlockPos(2, 1, 2);
        BlockPos nodePosition = stabilizer.above();
        BlockPos transducerPosition = nodePosition.above();
        helper.setBlock(stabilizer, ModBlocks.NODE_STABILIZER.get());
        helper.setBlock(nodePosition, ModBlocks.AURA_NODE.get());
        helper.setBlock(transducerPosition, ModBlocks.NODE_TRANSDUCER.get());
        helper.setBlock(transducerPosition.east(), Blocks.REDSTONE_BLOCK);

        AuraNodeBlockEntity node = (AuraNodeBlockEntity)
                helper.getBlockEntity(nodePosition);
        node.initializeOnce(classicNode(100));
        NodeTransducerBlockEntity transducer = (NodeTransducerBlockEntity)
                helper.getBlockEntity(transducerPosition);
        transducer.checkStatus();
        ServerLevel level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(transducerPosition);
        for (int tick = 0; tick < 1001; tick++) {
            NodeTransducerBlockEntity.serverTick(
                    level,
                    absolute,
                    level.getBlockState(absolute),
                    transducer
            );
        }
        helper.assertTrue(
                helper.getBlockEntity(nodePosition)
                        instanceof EnergizedAuraNodeBlockEntity,
                "Node did not energize at the original 1000-tick threshold"
        );

        helper.setBlock(transducerPosition.east(), Blocks.AIR);
        transducer.checkStatus();
        for (int tick = 0; tick < 951; tick++) {
            NodeTransducerBlockEntity.serverTick(
                    level,
                    absolute,
                    level.getBlockState(absolute),
                    transducer
            );
        }
        helper.assertTrue(
                helper.getBlockEntity(nodePosition)
                        instanceof AuraNodeBlockEntity restored
                        && restored.snapshotState().snapshot()
                        .aspectsCurrent().values().stream()
                        .allMatch(amount -> amount == 0),
                "Unpowered transducer did not restore a completely drained node"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "visTaming")
    public static void chargerDrawsFiveVisPerAspectFromEnergizedNode(
            GameTestHelper helper
    ) {
        BlockPos sourcePosition = new BlockPos(1, 2, 1);
        BlockPos workbenchPosition = new BlockPos(3, 1, 1);
        BlockPos chargerPosition = workbenchPosition.above();
        helper.setBlock(sourcePosition, ModBlocks.ENERGIZED_AURA_NODE.get());
        helper.setBlock(workbenchPosition, ModBlocks.ARCANE_WORKBENCH.get());
        helper.setBlock(chargerPosition, ModBlocks.VIS_CHARGE_RELAY.get());

        EnergizedAuraNodeBlockEntity source = (EnergizedAuraNodeBlockEntity)
                helper.getBlockEntity(sourcePosition);
        source.initialize(classicNode(25));
        ArcaneWorkbenchBlockEntity workbench = (ArcaneWorkbenchBlockEntity)
                helper.getBlockEntity(workbenchPosition);
        ItemStack wand = ModItems.BASIC_WAND.get().getDefaultInstance();
        workbench.wand().setItem(0, wand);
        VisChargeRelayBlockEntity charger = (VisChargeRelayBlockEntity)
                helper.getBlockEntity(chargerPosition);
        ServerLevel level = helper.getLevel();
        BlockPos sourceAbsolute = helper.absolutePos(sourcePosition);
        BlockPos chargerAbsolute = helper.absolutePos(chargerPosition);
        VisNetworkNodeBlockEntity.serverTick(
                level,
                sourceAbsolute,
                level.getBlockState(sourceAbsolute),
                source
        );
        VisNetworkNodeBlockEntity.serverTick(
                level,
                chargerAbsolute,
                level.getBlockState(chargerAbsolute),
                charger
        );

        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            helper.assertTrue(
                    WandVisService.visCentivis(wand, aspect.id()) == 5,
                    "Vis charger did not transfer exactly five "
                            + aspect.id() + " centivis"
            );
        }
        helper.succeed();
    }

    private static AuraNodeState classicNode(int amount) {
        Map<String, Integer> aspects = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            aspects.put(aspect.id(), amount);
        }
        return AuraNodeState.withAspects(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                aspects,
                aspects,
                0
        );
    }
}
