package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.render.CrystalClusterItemClientExtensions;
import com.thaumcraftmodern.crystal.CrystalClusterVariant;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class CrystalClusterItem extends BlockItem {
    private final CrystalClusterVariant variant;

    public CrystalClusterItem(
            Block block,
            CrystalClusterVariant variant,
            Properties properties
    ) {
        super(block, properties);
        this.variant = Objects.requireNonNull(variant, "variant");
    }

    public CrystalClusterVariant variant() {
        return variant;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        // Forge invokes this from Item's constructor, before this subclass's
        // fields have been assigned. The renderer therefore resolves the
        // variant from the rendered ItemStack instead of capturing it here.
        consumer.accept(CrystalClusterItemClientExtensions.create());
    }
}
