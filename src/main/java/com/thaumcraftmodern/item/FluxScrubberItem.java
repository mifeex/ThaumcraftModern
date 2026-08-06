package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.render.FluxScrubberItemClientExtensions;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class FluxScrubberItem extends BlockItem {
    public FluxScrubberItem(Block block,Properties properties){super(block,properties);}
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer){consumer.accept(FluxScrubberItemClientExtensions.create());}
}
