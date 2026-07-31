package com.thaumcraftmodern.crucible;

import com.thaumcraftmodern.scan.AspectReward;
import com.thaumcraftmodern.scan.ScanRegistry;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ItemAspectRegistry {
    private ItemAspectRegistry() {
    }

    public static Optional<Map<String, Integer>> aspects(ItemStack stack) {
        return ScanRegistry.findExplicitForItem(stack)
                .map(definition -> merge(definition.aspects()))
                .filter(aspects -> !aspects.isEmpty());
    }

    static Map<String, Integer> merge(List<AspectReward> rewards) {
        LinkedHashMap<String, Integer> aspects = new LinkedHashMap<>();
        for (AspectReward reward : rewards) {
            aspects.merge(
                    reward.aspectId(),
                    reward.amount(),
                    Math::addExact
            );
        }
        return Map.copyOf(aspects);
    }
}
