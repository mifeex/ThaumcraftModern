package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.render.VisDeviceItemClientExtensions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses the same original animated OBJ renderer in inventory and in-world. */
public final class VisDeviceBlockItem extends BlockItem {
    public enum Kind {
        STABILIZER,
        ADVANCED_STABILIZER,
        TRANSDUCER,
        RELAY,
        CHARGER
    }

    private final Kind kind;

    public VisDeviceBlockItem(Block block, Kind kind, Properties properties) {
        super(block, properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(VisDeviceItemClientExtensions.create());
    }
}
