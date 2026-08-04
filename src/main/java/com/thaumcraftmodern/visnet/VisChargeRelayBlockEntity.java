package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.wand.WandForm;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class VisChargeRelayBlockEntity extends VisRelayBlockEntity {
    public VisChargeRelayBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.VIS_CHARGE_RELAY.get(), position, state);
    }

    @Override
    protected void serverNetworkTick() {
        if (level == null || !(level.getBlockEntity(worldPosition.below())
                instanceof ArcaneWorkbenchBlockEntity workbench)) {
            return;
        }
        ItemStack wand = workbench.wand().getItem(0);
        if (!(wand.getItem() instanceof WandItem item)
                || item.form() == WandForm.STAFF) {
            return;
        }
        boolean changed = false;
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int room = WandVisService.capacityCentivis(wand)
                    - WandVisService.visCentivis(wand, aspect.id());
            // TC4 stores wand Vis in centivis here. The charger asks its
            // network for at most 5 CV per aspect and calls addRealVis.
            int requested = Math.min(5, room);
            if (requested <= 0) {
                continue;
            }
            int consumed = consumeVis(aspect, requested);
            if (consumed > 0) {
                changed |= WandVisService.addCentivisUnchecked(
                        wand,
                        aspect.id(),
                        consumed
                ) > 0;
            }
        }
        if (changed) {
            workbench.wand().setChanged();
        }
    }
}
