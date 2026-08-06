package com.thaumcraftmodern.client;

import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.item.RevealingGear;
import com.thaumcraftmodern.scan.AspectReward;
import com.thaumcraftmodern.scan.ScanTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves synchronized aspect-container contents for the shared revealing
 * goggles HUD. This mirrors TC4's common {@code drawTagsOnContainer} path:
 * nodes and crucibles provide different data, but share one presentation.
 */
final class ClientAspectContainerReadout {
    static final double DEFAULT_HUD_ANCHOR_HEIGHT = 1.22D;

    private ClientAspectContainerReadout() {
    }

    static Optional<Target> find(Minecraft minecraft, float partialTick) {
        if (minecraft.player == null
                || minecraft.level == null
                || minecraft.screen != null
                || minecraft.options.hideGui
                || !minecraft.options.getCameraType().isFirstPerson()) {
            return Optional.empty();
        }

        BlockHitResult hit = ScanTargeting.findBlock(
                minecraft.player,
                partialTick
        ).orElse(null);
        if (hit == null) {
            return Optional.empty();
        }

        BlockEntity blockEntity = minecraft.level.getBlockEntity(hit.getBlockPos());
        return AspectContainerHudRegistry.resolve(blockEntity, hit)
                .map(readout -> new Target(readout.anchor(), readout.aspects()));
    }

    static boolean wearingGoggles(Minecraft minecraft) {
        return minecraft.player != null
                && RevealingGear.equipped(minecraft.player
                        .getItemBySlot(EquipmentSlot.HEAD));
    }

    static List<AspectReward> nodeContents(AuraNodeState.Snapshot snapshot) {
        return snapshot.aspectsCurrent().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new AspectReward(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    static List<AspectReward> energizedNodeContents(
            Map<PrimalAspect, Integer> visBase
    ) {
        return PrimalAspect.ordered().stream()
                .filter(visBase::containsKey)
                .filter(aspect -> visBase.get(aspect) > 0)
                .map(aspect -> new AspectReward(
                        aspect.id(),
                        visBase.get(aspect)
                ))
                .toList();
    }

    static List<AspectReward> crucibleContents(Map<String, Integer> essentia) {
        return essentia.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new AspectReward(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    static List<AspectReward> singleAspectContents(String aspect, int amount) {
        return aspect == null || aspect.isBlank() || amount <= 0
                ? List.of() : List.of(new AspectReward(aspect, amount));
    }

    record Target(
            Vec3 anchor,
            List<AspectReward> aspects
    ) {
        Target {
            aspects = List.copyOf(aspects);
        }
    }
}
