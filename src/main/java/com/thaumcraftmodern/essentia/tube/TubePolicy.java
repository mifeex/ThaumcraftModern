package com.thaumcraftmodern.essentia.tube;

/** Isolated rules for a tube kind; the transport network never branches on it. */
public record TubePolicy(
        boolean filtered,
        boolean restrictedSuction,
        boolean directional,
        boolean redstoneValve
) {
}
