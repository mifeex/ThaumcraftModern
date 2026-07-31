package com.thaumcraftmodern.essentia.tube;

import net.minecraft.core.Direction;

/** Exact directional and suction rules used by TC4's tube subclasses. */
public final class TubeFlowRules {
    private TubeFlowRules() {
    }

    public static boolean acceptsSuctionFrom(
            TubePolicy policy, Direction facing, Direction side) {
        return !policy.directional() || facing == side.getOpposite();
    }

    public static boolean mayPullFrom(
            TubePolicy policy, Direction facing, Direction side) {
        return !policy.directional() || facing != side.getOpposite();
    }

    public static int propagatedSuction(TubePolicy policy, int remoteSuction) {
        return policy.restrictedSuction()
                ? remoteSuction / 2
                : remoteSuction - 1;
    }

    /** The selected facing points away from the side used to receive suction. */
    public static Direction controlledSide(Direction candidateFacing) {
        return candidateFacing.getOpposite();
    }
}
