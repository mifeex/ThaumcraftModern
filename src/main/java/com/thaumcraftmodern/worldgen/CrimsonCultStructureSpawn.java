package com.thaumcraftmodern.worldgen;

import java.util.List;

/**
 * Original TC4 Crimson Cult population parameters for an eldritch ring.
 *
 * <p>Kept independent of Minecraft registries so the probability and final
 * guard composition can be verified by ordinary unit tests.</p>
 */
final class CrimsonCultStructureSpawn {
    static final int VARIANT_COUNT = 10;

    private static final int FIRST_CULT_VARIANT = 1;
    private static final int LAST_CULT_VARIANT = 4;

    private static final List<Offset> CLERIC_OFFSETS = List.of(
            new Offset(-2, -2),
            new Offset(-2, 2),
            new Offset(2, -2),
            new Offset(2, 2)
    );
    private static final List<Offset> KNIGHT_OFFSETS = List.of(
            new Offset(-2, 0),
            new Offset(2, 0),
            new Offset(0, -2),
            new Offset(0, 2)
    );

    private CrimsonCultStructureSpawn() {
    }

    static boolean isCultVariant(int variant) {
        return variant >= FIRST_CULT_VARIANT
                && variant <= LAST_CULT_VARIANT;
    }

    static List<Offset> clericOffsets() {
        return CLERIC_OFFSETS;
    }

    static List<Offset> knightOffsets() {
        return KNIGHT_OFFSETS;
    }

    record Offset(int x, int z) {
    }
}
