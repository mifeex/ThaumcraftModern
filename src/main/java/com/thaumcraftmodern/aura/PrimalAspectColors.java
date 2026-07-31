package com.thaumcraftmodern.aura;

import java.util.Objects;

/**
 * Exact TC4 4.2.3.5 primal-aspect colors shared by nodes and tinted items.
 */
public final class PrimalAspectColors {
    private PrimalAspectColors() {
    }

    public static int color(PrimalAspect aspect) {
        return switch (Objects.requireNonNull(aspect, "aspect")) {
            case AER -> 0xFFFF7E;
            case TERRA -> 0x56C000;
            case IGNIS -> 0xFF5A01;
            case AQUA -> 0x3CD4FC;
            case ORDO -> 0xD5D4EC;
            case PERDITIO -> 0x404040;
        };
    }
}
