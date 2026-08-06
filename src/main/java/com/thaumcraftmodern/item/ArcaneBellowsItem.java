package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.render.ArcaneBellowsItemClientExtensions;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class ArcaneBellowsItem extends BlockItem {
    public ArcaneBellowsItem(Block block, Properties properties) { super(block, properties); }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ArcaneBellowsItemClientExtensions.create());
    }
}
