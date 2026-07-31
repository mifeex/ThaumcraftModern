package com.thaumcraftmodern.essentia;

import net.minecraft.core.Direction;

/** Shared placement and transport rules for the alembic's label panel. */
public final class ArcaneAlembicFacingRules {
    private ArcaneAlembicFacingRules() {
    }

    public static Direction facingPlayer(Direction playerFacing) {
        return playerFacing.getOpposite();
    }

    public static boolean isPipeConnectable(Direction panelFacing,
            Direction side) {
        return side != Direction.DOWN && side != panelFacing;
    }
}
