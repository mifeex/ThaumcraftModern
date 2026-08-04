package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalVis;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectDefinition;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WandRechargePedestalBlockEntityTest {
    @Test
    void poorWandMayDrainLastVisButQualityWandPreservesIt() {
        AuraNodeState.Snapshot oneAer = nodeWithAer(1);
        WandState poor = WandState.empty(1, "wood", "iron");
        WandState quality = WandState.empty(1, "greatwood", "gold");
        assertEquals(PrimalAspect.AER,
                WandRechargePedestalBlockEntity.selectTransfer(poor, 5_000, oneAer).orElseThrow());
        assertTrue(WandRechargePedestalBlockEntity
                .selectTransfer(quality, 5_000, oneAer).isEmpty());
    }

    @Test
    void fullPrimalPoolIsSkippedInClassicAspectOrder() {
        EnumMap<PrimalAspect, Integer> vis = new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) vis.put(aspect, 0);
        vis.put(PrimalAspect.AER, 5_000);
        WandState wand = new WandState(1, "wood", "iron", vis);
        assertEquals(PrimalAspect.TERRA,
                WandRechargePedestalBlockEntity.selectTransfer(
                        wand, 5_000, nodeWithEveryPrimal(2)).orElseThrow());
    }

    @Test
    void compoundVisRequiresFocusAndYieldsOnlyOneMatchingPrimal() {
        WandState wand = WandState.empty(1, "wood", "iron");
        AuraNodeState.Snapshot motus = nodeWithCompound("motus", 2);
        AspectCatalog catalog = classicTestCatalog();

        assertTrue(WandRechargePedestalBlockEntity
                .selectTransfer(wand, 5_000, motus, false, catalog).isEmpty());
        WandRechargePedestalBlockEntity.TransferSelection selected =
                WandRechargePedestalBlockEntity
                        .selectTransfer(wand, 5_000, motus, true, catalog)
                        .orElseThrow();
        assertEquals("motus", selected.sourceAspectId());
        assertEquals(PrimalAspect.AER, selected.targetPrimal());
        assertEquals(0xCDCCF4, selected.color());
    }

    @Test
    void directPrimalAlwaysPrecedesCompoundVis() {
        LinkedHashMap<String, Integer> current = emptyAspectMap();
        current.put("aer", 2);
        current.put("motus", 2);
        AuraNodeState.Snapshot node = AuraNodeState.withAspects(
                UUID.randomUUID(), AuraNodeType.NORMAL, AuraNodeModifier.NORMAL,
                current, current, 0).snapshot();

        WandRechargePedestalBlockEntity.TransferSelection selected =
                WandRechargePedestalBlockEntity.selectTransfer(
                        WandState.empty(1, "wood", "iron"), 5_000,
                        node, true, classicTestCatalog()).orElseThrow();
        assertEquals("aer", selected.sourceAspectId());
        assertEquals(PrimalAspect.AER, selected.targetPrimal());
    }

    @Test
    void compoundReductionRecursesToAllClassicPrimals() {
        assertEquals(java.util.EnumSet.of(PrimalAspect.AER, PrimalAspect.ORDO,
                        PrimalAspect.AQUA),
                WandRechargePedestalBlockEntity.reduceToPrimals(
                        "tempestas", classicTestCatalog()));
    }

    private static AuraNodeState.Snapshot nodeWithAer(int amount) {
        EnumMap<PrimalAspect, Integer> current = new EnumMap<>(PrimalAspect.class);
        EnumMap<PrimalAspect, Integer> maximum = new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            current.put(aspect, aspect == PrimalAspect.AER ? amount : 0);
            maximum.put(aspect, aspect == PrimalAspect.AER ? amount : 0);
        }
        return new AuraNodeState(UUID.randomUUID(), AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL, current, maximum, 0).snapshot();
    }

    private static AuraNodeState.Snapshot nodeWithEveryPrimal(int amount) {
        return new AuraNodeState(UUID.randomUUID(), AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL, PrimalVis.uniform(amount),
                PrimalVis.uniform(amount), 0).snapshot();
    }

    private static AuraNodeState.Snapshot nodeWithCompound(String id, int amount) {
        LinkedHashMap<String, Integer> current = emptyAspectMap();
        current.put(id, amount);
        return AuraNodeState.withAspects(UUID.randomUUID(), AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL, current, current, 0).snapshot();
    }

    private static LinkedHashMap<String, Integer> emptyAspectMap() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) result.put(aspect.id(), 0);
        return result;
    }

    private static AspectCatalog classicTestCatalog() {
        String icon = "minecraft:air";
        return new AspectCatalog(List.of(
                new AspectDefinition("aer", 0xFFFF7E, icon),
                new AspectDefinition("terra", 0x56C000, icon),
                new AspectDefinition("ignis", 0xFF5A01, icon),
                new AspectDefinition("aqua", 0x3CD4FC, icon),
                new AspectDefinition("ordo", 0xD5D4EC, icon),
                new AspectDefinition("perditio", 0x404040, icon),
                new AspectDefinition("motus", 0xCDCCF4, icon, "aer", "ordo"),
                new AspectDefinition("tempestas", 0xFFFFFF, icon, "motus", "aqua")
        ));
    }
}
