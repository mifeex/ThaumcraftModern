package com.thaumcraftmodern.world.block.entity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ArcaneCraftingInventory extends SimpleContainer implements CraftingContainer {
    public static final int WIDTH = 3;
    public static final int HEIGHT = 3;

    public ArcaneCraftingInventory() {
        super(WIDTH * HEIGHT);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public List<ItemStack> getItems() {
        return java.util.stream.IntStream.range(0, getContainerSize())
                .mapToObj(this::getItem)
                .toList();
    }
}
