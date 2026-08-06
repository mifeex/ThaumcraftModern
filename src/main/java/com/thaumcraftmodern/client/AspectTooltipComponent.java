package com.thaumcraftmodern.client;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

/** Data for the compact icon-and-label aspect strip in item tooltips. */
public record AspectTooltipComponent(List<Entry> aspects)
        implements TooltipComponent {
    public AspectTooltipComponent {
        aspects = List.copyOf(aspects);
    }

    public record Entry(ResourceLocation icon, int color, Component label,
                        int amount) { }
}
