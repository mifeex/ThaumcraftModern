package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class VisRelayBlockEntity extends VisNetworkNodeBlockEntity {
    public VisRelayBlockEntity(BlockPos position, BlockState state) {
        this(ModBlockEntities.VIS_RELAY.get(), position, state);
    }

    protected VisRelayBlockEntity(
            net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            BlockPos position,
            BlockState state
    ) {
        super(type, position, state);
    }

    @Override
    public boolean isSource() {
        return false;
    }

    public void cycleAttunement() {
        setAttunement((byte) (attunement() >= 5 ? -1 : attunement() + 1));
    }
}
