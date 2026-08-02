package com.thaumcraftmodern.essentia.tube;

/** Isolated rules for a tube kind; the transport network never branches on it. */
public record TubePolicy(
        boolean filtered,
        boolean restrictedSuction,
        boolean directional,
        boolean redstoneValve,
        boolean reversibleController
) {
    public TubePolicy(boolean filtered, boolean restrictedSuction,
            boolean directional, boolean redstoneValve) {
        this(filtered, restrictedSuction, directional, redstoneValve,
                false);
    }
}
