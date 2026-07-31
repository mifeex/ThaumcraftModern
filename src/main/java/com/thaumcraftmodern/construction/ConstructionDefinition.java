package com.thaumcraftmodern.construction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ConstructionDefinition(
        String id,
        Handler handler,
        Trigger trigger,
        String research,
        Map<String, Integer> vis
) {
    public ConstructionDefinition {
        if (id == null || !id.matches("[a-z0-9_.-]+")) {
            throw new IllegalArgumentException("invalid construction id: " + id);
        }
        handler = Objects.requireNonNull(handler, "handler");
        trigger = Objects.requireNonNull(trigger, "trigger");
        research = research == null ? "" : research;
        LinkedHashMap<String, Integer> checkedVis = new LinkedHashMap<>();
        Objects.requireNonNull(vis, "vis").forEach((aspect, amount) -> {
            if (!aspect.matches("[a-z0-9_.-]+") || amount == null || amount < 0) {
                throw new IllegalArgumentException(
                        "invalid vis cost " + aspect + "=" + amount
                );
            }
            if (amount > 0) {
                checkedVis.put(aspect, amount);
            }
        });
        vis = Map.copyOf(checkedVis);
    }

    public boolean matchesItem(ItemStack stack) {
        if (trigger.type() != TriggerType.ITEM || stack.isEmpty()) {
            return false;
        }
        return trigger.item().equals(
                ForgeRegistries.ITEMS.getKey(stack.getItem())
        );
    }

    public enum Handler {
        RESEARCH_TABLE_PAIR,
        CRUCIBLE,
        INFERNAL_FURNACE,
        INFUSION_ALTAR,
        THAUMATORIUM,
        ADVANCED_ALCHEMICAL_FURNACE
    }

    public enum TriggerType {
        ITEM,
        WAND
    }

    public record Trigger(
            TriggerType type,
            ResourceLocation item,
            int consume
    ) {
        public Trigger {
            type = Objects.requireNonNull(type, "type");
            if (type == TriggerType.ITEM && item == null) {
                throw new IllegalArgumentException(
                        "item trigger requires an item id"
                );
            }
            if (type == TriggerType.WAND && item != null) {
                throw new IllegalArgumentException(
                        "wand trigger must not declare an item id"
                );
            }
            if (consume < 0) {
                throw new IllegalArgumentException(
                        "trigger consume cannot be negative"
                );
            }
        }
    }
}
